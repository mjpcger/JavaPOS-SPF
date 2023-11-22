/*
 * Copyright 2023 Martin Conrad
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.graphicdisplay.*;
import jpos.*;
import jpos.config.*;
import java.util.*;

import static java.lang.Math.max;

/**
 * JposDevice based dummy implementation for JavaPOS GraphicDisplay device service implementation.
 * No real hardware. All read data with dummy values, operator interaction via OptionDialog boxes.<br>
 * Supported configuration values for GraphicDisplay in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>CapAssociatedHardTotalsDevice: Valid values are HardTotals device names as specified in jpos.xml. Default
 *     is an empty string, specifying no HardTotals support. If an empty string, CapStorage becomes CST_HOST_ONLY,
 *     otherwise CST_ALL.<br>
 *     In this implementation, this name will not be checked in any way. Access to image or video data will only be
 *     simulated.</li>
 *     <li>CapBrightness: Valid values are TRUE and FALSE, default is TRUE.</li>
 *     <li>CapVolume: Valid values are TRUE and FALSE, default is TRUE.</li>
 *     <li>ImageTypeList: Comma separated list of image types, e.g. "BMP,JPG", default is an empty string. If empty,
 *     CapImageType becomes false, otherwise true.</li>
 *     <li>VideoTypeList: Comma separated list of video types, e.g. "MP4,MKV", default is an empty string. If empty,
 *     CapVideoType becomes false, otherwise true.</li>
 * </ul>
 * Keep in mind: This implementation does nor show any video, image or web page, it shows only status information.<br>
 * To control timing and error handling, file names and URL strings should be used as follows:<ul>
 *     <li>File names shall have the format PPTT[.SS], where PP is one of "OK" or "KO", TT the play or load time in units
 *     of 0.1 seconds and SS an optional suffix, starting with a dot (.). If starting with "OK", the method
 *     call will be successful, if starting with "KO", an error will be generated.</li>
 *     <li>Valid URLs have the form "http://PP:TT[/SS], where PP is one of "OK" or "KO", TT the load time in units
 *     of 0.1 seconds and SS an optional suffix, starting with a slash (/).</li>
 * </ul>
 * All other file names and URL strings will result in an error event or an error exception.
 * <b>SPECIAL REMARKS:</b> This sample does not implement any really existing GraphicDisplay system and shall not be
 * used in any really existing cash register application.
 */
public class GraphicDisplayDevice extends JposDevice {
    protected GraphicDisplayDevice(String id) {
        super(id);
        graphicDisplayInit(1);
        PhysicalDeviceDescription = "Dummy GraphicDisplay simulator";
        PhysicalDeviceName = "Dummy GraphicDisplay Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
        CurrentCommands = new ArrayList<>();
        }

    private String CapAssociatedHardTotalsDevice = "";
    private String ImageTypeList = "";
    private String VideoTypeList = "";
    private boolean CapBrightness = true;
    private boolean CapVolume = true;

    private class RequestThread extends Thread {
        JposOutputRequest Request;

        RequestThread(JposOutputRequest request, String name) {
            super(name);
            Request = request;
        }

        @Override
        public void run() {
            Request.catchedInvocation();
            boolean processed = Request.finishAsyncProcessing();
            synchronized (AsyncProcessorRunning) {
                if (processed)
                    CurrentCommands.remove(Request);
            }
        }
    }

    @Override
    public void checkProperties(JposEntry entries) throws JposException{
        super.checkProperties(entries);
        Object o;
        for (Iterator it = entries.getProps(); it.hasNext(); ) {
            JposEntry.Prop entry = (JposEntry.Prop)it.next();
            try {
                if (entry.getName().equals("CapAssociatedHardTotalsDevice")) {
                    if ((CapAssociatedHardTotalsDevice = entry.getValue().toString()).length() > 0) {
                        HardTotals hd = new HardTotals();
                        hd.open(CapAssociatedHardTotalsDevice);
                        hd.close();
                    }
                }
                if (entry.getName().equals("ImageTypeList"))
                    ImageTypeList = entry.getValue().toString();
                if (entry.getName().equals("VideoTypeList"))
                    VideoTypeList = entry.getValue().toString();
                if (entry.getName().equals("CapBrightness"))
                    CapBrightness = Boolean.parseBoolean(entry.getValue().toString());
                if (entry.getName().equals("CapVolume"))
                    CapVolume = Boolean.parseBoolean(entry.getValue().toString());
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Invalid Property " + entry.getName() + ": " + entry.getValue().toString());
            }
        }
    }

    @Override
    public void changeDefaults(GraphicDisplayProperties p) {
        SampleProperties props = (SampleProperties) p;
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Graphic Display service for sample dummy device";
        if ((props.CapAssociatedHardTotalsDevice = CapAssociatedHardTotalsDevice).length() == 0)
            props.CapStorage = GraphicDisplayConst.GDSP_CST_HOST_ONLY;
        else
            props.CapStorage = GraphicDisplayConst.GDSP_CST_ALL;
        props.CapImageType = (props.ImageTypeList = ImageTypeList).length() > 0;
        props.CapVideoType = (props.VideoTypeList = VideoTypeList).length() > 0;
        props.CapBrightness = CapBrightness;
        props.CapVolume = CapVolume;
    }

    @Override
    public void invokeConcurrentMethod(JposOutputRequest request) {
        if (!(request instanceof PlayVideo)) {
            super.invokeConcurrentMethod(request);
        }
        else {
            synchronized (AsyncProcessorRunning) {
                new RequestThread(request, "VideoPlayer").start();
            }
        }
    }

    @Override
    public GraphicDisplayProperties getGraphicDisplayProperties(int index) {
        return new SampleProperties();
    }

    private class SampleProperties extends GraphicDisplayProperties {
        private String PreviousURL = null;
        private String NextURL = null;
        private String CurrentURL = null;
        protected SampleProperties() {
            super(0);
        }

        @Override
        public boolean initOnFirstEnable() {
            if (super.initOnFirstEnable()) {
                String[] parts = ImageTypeList.split(",");
                ImageType = parts.length > 0 ? parts[0] : "";
                parts = VideoTypeList.split(",");
                VideoType = parts[0];
                Storage = CapStorage == GraphicDisplayConst.GDSP_CST_ALL  ? GraphicDisplayConst.GDSP_ST_HOST_HARDTOTALS : GraphicDisplayConst.GDSP_ST_HOST;
                Volume = Brightness = 50;
                DisplayMode = GraphicDisplayConst.GDSP_DMODE_HIDDEN;
                AsyncMode = true;
                return true;
            }
            return false;
        }

        @Override
        public void initOnEnable(boolean flag) {
            if (flag) {
                CurrentURL = PreviousURL = NextURL = null;
                if (CapURLBack) {
                    CapURLBack = false;
                    EventSource.logSet("CapURLBack");
                }
                if (CapURLForward) {
                    CapURLForward = false;
                    EventSource.logSet("CapURLForward");
                }
            }
            super.initOnEnable(flag);
        }

        @Override
        public void release() throws JposException {
            clearOutput();
            super.release();
        }

        private int getFileOperationTime(String filename) throws JposException {
            Integer time;
            String file = filename;
            int index = max(file.lastIndexOf('\\'), file.lastIndexOf('/'));
            if (index >= 0)
                file = file.substring(index + 1);
            if ((index = file.indexOf('.')) > 0)
                file = file.substring(0, index);
            try {
                index = Integer.parseInt(file.substring(2));
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid Filename: " + filename);
            }
            time = file.substring(0, 2).toUpperCase().equals("KO") ? -index : (file.substring(0, 2).toUpperCase().equals("OK") ? index : null);
            check(time == null, JposConst.JPOS_E_ILLEGAL, "Invalid File: " + filename);
            return time * 100;
        }

        private int getUrlOperationTime(String url) throws JposException {
            Integer time;
            String uri = url;
            try {
                check(!uri.substring(0, "http://".length()).equals("http://"), 0, "");
                uri = uri.substring("http://".length());
                check(uri.charAt(2) != ':', 0, "");
                if (uri.indexOf('/') > 0)
                    uri = uri.substring(0, uri.indexOf('/'));
                int index = Integer.parseInt(uri.substring(3));
                time = uri.substring(0, 2).toUpperCase().equals("KO") ? -index : (uri.substring(0, 2).toUpperCase().equals("OK") ? index : null);
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid URL: " + url);
            }
            check(time == null, JposConst.JPOS_E_ILLEGAL, "Invalid URL: " + url);
            return time * 100;
        }

        @Override
        public void displayMode(int mode) throws JposException {
            if (mode == GraphicDisplayConst.GDSP_DMODE_HIDDEN) {
                List<JposOutputRequest> imgorvideos = new ArrayList<>();
                synchronized (AsyncProcessorRunning) {
                    for (JposOutputRequest req : CurrentCommands) {
                        if (req instanceof LoadImage || req instanceof PlayVideo)
                            imgorvideos.add(req);
                    }
                }
                while(imgorvideos.size() > 0) {
                    imgorvideos.get(0).abortCommand();
                }
            }
            super.displayMode(mode);
        }

        @Override
        public LoadImage loadImage(String filename) throws JposException {
            getFileOperationTime(filename);
            return new LoadImage(this, filename);
        }

        @Override
        public void loadImage(LoadImage request) throws JposException {
            int time = getFileOperationTime(request.getFileName());
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_START_IMAGE_LOAD));
            request.Waiting.suspend(time > 0 ? time : -time);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_END_IMAGE_LOAD));
            check(time < 0, JposConst.JPOS_E_FAILURE, "Bad File");
        }

        @Override
        public PlayVideo playVideo(String filename, boolean loop) throws JposException {
            GraphicDisplayService srv = (GraphicDisplayService) EventSource;
            synchronized (AsyncProcessorRunning) {
                check(State == JposConst.JPOS_S_ERROR, JposConst.JPOS_E_ILLEGAL, "Leave Error Condition First");
            }
            getFileOperationTime(filename);
            return new PlayVideo(this, filename, loop);
        }

        @Override
        public void playVideo(PlayVideo request) throws JposException {
            int time = getFileOperationTime(request.getFileName());
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_START_PLAY_VIDEO));
            request.Waiting.suspend(time > 0 ? (request.getLoop() ? SyncObject.INFINITE : time) : -time);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_STOP_PLAY_VIDEO));
            check(time < 0, JposConst.JPOS_E_FAILURE, "Bad File");
        }

        @Override
        public void stopVideo() throws JposException {
            List<PlayVideo> videos = new ArrayList<PlayVideo>();
            synchronized (AsyncProcessorRunning) {
                for (JposOutputRequest req : CurrentCommands) {
                    if (req instanceof PlayVideo)
                        videos.add((PlayVideo) req);
                }
            }
            for (PlayVideo req : videos) {
                req.abortCommand(true);
                JposOutputCompleteEvent ev = req.createOutputEvent();
                if (ev!= null)
                    handleEvent(ev);
            }
        }

        @Override
        public void cancelURLLoading() throws JposException {
            List<JposOutputRequest> reqs = new ArrayList<JposOutputRequest>();
            synchronized (AsyncProcessorRunning) {
                for (JposOutputRequest req : CurrentCommands) {
                    if (!(req instanceof PlayVideo || req instanceof LoadImage))
                        reqs.add((PlayVideo) req);
                }
            }
            for (JposOutputRequest req : reqs) {
                req.abortCommand();
            }
        }

        @Override
        public LoadURL loadURL(String url) throws JposException {
            getUrlOperationTime(url);
            return new LoadURL(this, url);
        }

        @Override
        public void loadURL(LoadURL request) throws JposException {
            int time = getUrlOperationTime(request.getURL());
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE, request.getURL()));
            boolean cancelled = request.Waiting.suspend(time > 0 ? time : -time);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    cancelled || time < 0 ? GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE : GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE,
                    request.getURL()));
            PreviousURL = CurrentURL;
            NextURL = null;
            CurrentURL = request.getURL();
            boolean changed = PreviousURL != null;
            if (CapURLBack != changed) {
                CapURLBack = changed;
                EventSource.logSet("CapURLBack");
            }
            changed = false;
            if (CapURLForward != changed) {
                CapURLForward = changed;
                EventSource.logSet("CapURLForward");
            }
            check(time < 0, JposConst.JPOS_E_FAILURE, "URL Load Error");
        }

        @Override
        public UpdateURLPage updateURLPage() throws JposException {
            return new UpdateURLPage(this);
        }

        @Override
        public void updateURLPage(UpdateURLPage request) throws JposException {
            check(CurrentURL == null, JposConst.JPOS_E_ILLEGAL, "No Current URL");
            int time = getUrlOperationTime(CurrentURL);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE, CurrentURL));
            boolean cancelled = request.Waiting.suspend(time > 0 ? time : -time);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    cancelled || time < 0 ? GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE : GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE,
                    CurrentURL));
            check(time < 0, JposConst.JPOS_E_FAILURE, "URL Load Error");
        }

        @Override
        public GoURLBack goURLBack() throws JposException {
            return new GoURLBack(this);
        }

        @Override
        public void goURLBack(GoURLBack request) throws JposException {
            check(PreviousURL == null, JposConst.JPOS_E_ILLEGAL, "No Previous URL");
            int time = getUrlOperationTime(PreviousURL);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE, PreviousURL));
            boolean cancelled = request.Waiting.suspend(time > 0 ? time : -time);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    cancelled || time < 0 ? GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE : GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE,
                    PreviousURL));
            NextURL = CurrentURL;
            CurrentURL = PreviousURL;
            PreviousURL = null;
            boolean changed = false;
            if (CapURLBack != changed) {
                CapURLBack = changed;
                EventSource.logSet("CapURLBack");
            }
            changed = NextURL != null;
            if (CapURLForward != changed) {
                CapURLForward = changed;
                EventSource.logSet("CapURLForward");
            }
            check(time < 0, JposConst.JPOS_E_FAILURE, "URL Load Error");
        }

        @Override
        public GoURLForward goURLForward() throws JposException {
            return new GoURLForward(this);
        }

        @Override
        public void goURLForward(GoURLForward request) throws JposException {
            check(NextURL == null, JposConst.JPOS_E_ILLEGAL, "No Next URL");
            int time = getUrlOperationTime(NextURL);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    GraphicDisplayConst.GDSP_SUE_START_LOAD_WEBPAGE, NextURL));
            boolean cancelled = request.Waiting.suspend(time > 0 ? time : -time);
            handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource,
                    cancelled || time < 0 ? GraphicDisplayConst.GDSP_SUE_CANCEL_LOAD_WEBPAGE : GraphicDisplayConst.GDSP_SUE_FINISH_LOAD_WEBPAGE,
                    NextURL));
            PreviousURL = CurrentURL;
            CurrentURL = NextURL;
            NextURL = null;
            CapURLForward = false;
            CapURLBack = PreviousURL != null;
            boolean changed = PreviousURL != null;
            if (CapURLBack != changed) {
                CapURLBack = changed;
                EventSource.logSet("CapURLBack");
            }
            changed = false;
            if (CapURLForward != changed) {
                CapURLForward = changed;
                EventSource.logSet("CapURLForward");
            }
            check(time < 0, JposConst.JPOS_E_FAILURE, "URL Load Error");
        }
    }
}
