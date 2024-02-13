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

package de.gmxhome.conrad.jpos.jpos_base.graphicdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

/**
 * GraphicDisplay service implementation. For more details about getter, setter and method implementations,
 * see JposBase.<br>
 * Even if the UPOS specification allows to implement concurrent asynchronous processing for GraphicDisplay, this
 * implementation does not support concurrent video playback because playing more than one video at the same time,
 * centered on the display or in full screen mode, seems to be meaningless.
 */
public class GraphicDisplayService extends JposBase implements GraphicDisplayService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public GraphicDisplayService(GraphicDisplayProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the GraphicDisplayInterface for graphic display specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public GraphicDisplayInterface GraphicDisplay;

    private GraphicDisplayProperties Data;

    @Override
    public int getBrightness() throws JposException {
        checkEnabled();
        logGet("Brightness");
        return Data.Brightness;
    }

    @Override
    public void setBrightness(int brightness) throws JposException {
        logPreSet("Brightness");
        checkOpened();
        JposDevice.check(!Data.CapBrightness && brightness != Data.Brightness, JposConst.JPOS_E_ILLEGAL, "Changing Brightness Illegal");
        JposDevice.check(brightness < 0 || brightness > 100, JposConst.JPOS_E_ILLEGAL, "Brightness Must Be Between 0 And 100: " + brightness);
        GraphicDisplay.brightness(brightness);
        logSet("Brightness");
    }

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        checkOpened();
        logGet("CapAssociatedHardTotalsDevice");
        return Data.CapAssociatedHardTotalsDevice;
    }

    @Override
    public boolean getCapBrightness() throws JposException {
        checkOpened();
        logGet("CapBrightness");
        return Data.CapBrightness;
    }

    @Override
    public boolean getCapImageType() throws JposException {
        checkOpened();
        logGet("CapImageType");
        return Data.CapImageType;
    }

    @Override
    public int getCapStorage() throws JposException {
        checkOpened();
        logGet("CapStorage");
        return Data.CapStorage;
    }

    @Override
    public boolean getCapURLBack() throws JposException {
        checkOpened();
        logGet("CapURLBack");
        return Data.CapURLBack;
    }

    @Override
    public boolean getCapURLForward() throws JposException {
        checkOpened();
        logGet("CapURLForward");
        return Data.CapURLForward;
    }

    @Override
    public boolean getCapVideoType() throws JposException {
        checkOpened();
        logGet("CapVideoType");
        return Data.CapVideoType;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        checkOpened();
        logGet("CapVolume");
        return Data.CapVolume;
    }

    @Override
    public int getDisplayMode() throws JposException {
        checkEnabled();
        logGet("DisplayMode");
        return Data.DisplayMode;
    }

    @Override
    public void setDisplayMode(int displayMode) throws JposException {
        logPreSet("DisplayMode");
        long[] valid = {
                GraphicDisplayConst.GDSP_DMODE_HIDDEN,
                GraphicDisplayConst.GDSP_DMODE_WEB
        };
        long[] validImage = {
                GraphicDisplayConst.GDSP_DMODE_IMAGE_FIT,
                GraphicDisplayConst.GDSP_DMODE_IMAGE_FILL,
                GraphicDisplayConst.GDSP_DMODE_IMAGE_CENTER
        };
        long[] validVideo = {
                GraphicDisplayConst.GDSP_DMODE_VIDEO_NORMAL,
                GraphicDisplayConst.GDSP_DMODE_VIDEO_FULL
        };
        checkEnabled();
        if (!JposDevice.member(displayMode, valid)) {
            if (!Data.CapImageType || !JposDevice.member(displayMode, validImage)) {
                if (!Data.CapVideoType || !JposDevice.member(displayMode, validVideo))
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "DisplayMode Invalid: " + displayMode);
            }
        }
        GraphicDisplay.displayMode(displayMode);
        logSet("DisplayMode");
    }

    @Override
    public String getImageType() throws JposException {
        checkEnabled();
        logGet("ImageType");
        return Data.ImageType;
    }

    @Override
    public void setImageType(String imageType) throws JposException {
        logPreSet("ImageType");
        checkEnabled();
        JposDevice.check(!Data.CapImageType, JposConst.JPOS_E_ILLEGAL, "Image Mode Not Supported");
        JposDevice.check(!JposDevice.member(imageType, Data.ImageTypeList.split(",")), JposConst.JPOS_E_ILLEGAL, "ImageType Illegal: " + imageType);
        GraphicDisplay.imageType(imageType);
        logSet("ImageType");
    }

    @Override
    public String getImageTypeList() throws JposException {
        checkOpened();
        logGet("ImageTypeList");
        return Data.ImageTypeList;
    }

    @Override
    public int getLoadStatus() throws JposException {
        checkOpened();
        JposDevice.check(Data.LoadStatus == null, JposConst.JPOS_E_ILLEGAL, "Load Status Not Available");
        logGet("LoadStatus");
        return Data.LoadStatus;
    }

    @Override
    public int getStorage() throws JposException {
        checkEnabled();
        logGet("Storage");
        return Data.Storage;
    }

    @Override
    public void setStorage(int storage) throws JposException {
        logPreSet("Storage");
        checkEnabled();
        long[] valid = {
                GraphicDisplayConst.GDSP_ST_HOST, GraphicDisplayConst.GDSP_ST_HARDTOTALS
        };
        boolean[] condition = {
                Data.CapStorage == GraphicDisplayConst.GDSP_CST_HOST_ONLY && storage != GraphicDisplayConst.GDSP_ST_HOST,
                Data.CapStorage == GraphicDisplayConst.GDSP_CST_HARDTOTALS_ONLY && storage != GraphicDisplayConst.GDSP_ST_HARDTOTALS,
                !JposDevice.member(storage, valid)
        };
        JposDevice.check(condition[0] || condition[1] || condition[2], JposConst.JPOS_E_ILLEGAL, "Storage Invalid: " + storage);
        GraphicDisplay.storage(storage);
        logSet("Storage");
    }

    @Override
    public String getURL() throws JposException {
        checkOpened();
        JposDevice.check(Data.URL == null, JposConst.JPOS_E_ILLEGAL, "Load Status Not Available");
        logGet("URL");
        return Data.URL;
    }

    @Override
    public String getVideoType() throws JposException {
        checkEnabled();
        logGet("VideoType");
        return Data.VideoType;
    }

    @Override
    public void setVideoType(String videoType) throws JposException {
        logPreSet("VideoType");
        checkEnabled();
        JposDevice.check(!Data.CapVideoType, JposConst.JPOS_E_ILLEGAL, "Video Mode Not Supported.");
        String[] valid = Data.VideoTypeList.split(",");
        JposDevice.check(!JposDevice.member(videoType, valid), JposConst.JPOS_E_ILLEGAL, "VideoType Not Supported: " + videoType);
        GraphicDisplay.videoType(videoType);
        logSet("VideoType");
    }

    @Override
    public String getVideoTypeList() throws JposException {
        checkOpened();
        logGet("VideoTypeList");
        return Data.VideoTypeList;
    }

    @Override
    public int getVolume() throws JposException {
        checkEnabled();
        logGet("Volume");
        return Data.Volume;
    }

    @Override
    public void setVolume(int volume) throws JposException {
        logPreSet("Volume");
        checkEnabled();
        JposDevice.check(!Data.CapVolume && volume != Data.Volume, JposConst.JPOS_E_ILLEGAL, "Volume Change Not Supported");
        JposDevice.check(volume < 0 || volume > 100, JposConst.JPOS_E_ILLEGAL, "Volume Must Be Between 0 And 100: " + volume);
        GraphicDisplay.volume(volume);
        logSet("Volume");
    }

    private void callIt(JposOutputRequest request, String name) throws JposException {
        if (request != null)
            request.enqueue();
        logAsyncCall(name);
    }

    /**
     * Specifies whether a URL is currently loading.
     */
    public boolean UrlLoading = false;

    /**
     * Specifies whether a video is currently playing.
     */
    public boolean VideoPlaying = false;

    @Override
    public void cancelURLLoading() throws JposException {
        logPreCall("CancelURLLoading");
        checkEnabled();
        JposDevice.check(!UrlLoading, JposConst.JPOS_E_ILLEGAL, "No URL Loading");
        GraphicDisplay.cancelURLLoading();
        logCall("CancelURLLoading");
    }

    @Override
    public void goURLBack() throws JposException {
        logPreCall("GoURLBack");
        checkEnabled();
        JposDevice.check(Data.DisplayMode == GraphicDisplayConst.GDSP_DMODE_HIDDEN, JposConst.JPOS_E_ILLEGAL, "Bad Mode");
        callIt(GraphicDisplay.goURLBack(), "GoURLBack");
    }

    @Override
    public void goURLForward() throws JposException {
        logPreCall("GoURLForward");
        checkEnabled();
        JposDevice.check(Data.DisplayMode == GraphicDisplayConst.GDSP_DMODE_HIDDEN, JposConst.JPOS_E_ILLEGAL, "Bad Mode");
        callIt(GraphicDisplay.goURLForward(), "GoURLForward");
    }

    @Override
    public void loadImage(String s) throws JposException {
        if (s == null)
            s = "";
        long[] valid = { GraphicDisplayConst.GDSP_DMODE_IMAGE_FIT, GraphicDisplayConst.GDSP_DMODE_IMAGE_FILL, GraphicDisplayConst.GDSP_DMODE_IMAGE_CENTER };
        logPreCall("LoadImage", s);
        checkEnabled();
        JposDevice.check(!Data.CapImageType, JposConst.JPOS_E_ILLEGAL, "No Image Mode Support");
        JposDevice.checkMember(Data.DisplayMode, valid, JposConst.JPOS_E_ILLEGAL, "Invalid DisplayMode For LoadImage: " + Data.DisplayMode);
        callIt(GraphicDisplay.loadImage(s), "LoadImage");
    }

    @Override
    public void loadURL(String s) throws JposException {
        if (s == null)
            s = "";
        logPreCall("LoadURL", s);
        checkEnabled();
        JposDevice.check(Data.DisplayMode == GraphicDisplayConst.GDSP_DMODE_HIDDEN, JposConst.JPOS_E_ILLEGAL, "Bad Mode");
        JposDevice.check(s.length() <= 0, JposConst.JPOS_E_ILLEGAL, "Empty URL");
        callIt(GraphicDisplay.loadURL(s), "LoadURL");
    }

    @Override
    public void playVideo(String s, boolean b) throws JposException {
        if (s == null)
            s = "";
        long[] valid = { GraphicDisplayConst.GDSP_DMODE_VIDEO_NORMAL, GraphicDisplayConst.GDSP_DMODE_VIDEO_FULL };
        logPreCall("PlayVideo", s + ", " + b);
        checkEnabled();
        JposDevice.check(!Data.CapVideoType, JposConst.JPOS_E_ILLEGAL, "No Video Mode Support");
        JposDevice.checkMember(Data.DisplayMode, valid, JposConst.JPOS_E_ILLEGAL, "Invalid DisplayMode For PlayVideo: " + Data.DisplayMode);
        callIt(GraphicDisplay.playVideo(s, b), "PlayVideo");
    }

    @Override
    public void stopVideo() throws JposException {
        logPreCall("StopVideo");
        checkEnabled();
        JposDevice.check(!VideoPlaying, JposConst.JPOS_E_ILLEGAL, "No Video Playing");
        GraphicDisplay.stopVideo();
        logCall("StopVideo");
    }

    @Override
    public void updateURLPage() throws JposException {
        logPreCall("UpdateURLPage");
        checkEnabled();
        JposDevice.check(Data.DisplayMode == GraphicDisplayConst.GDSP_DMODE_HIDDEN, JposConst.JPOS_E_ILLEGAL, "Bad Mode");
        JposDevice.check(UrlLoading, JposConst.JPOS_E_ILLEGAL, "URL Loading");
        callIt(GraphicDisplay.updateURLPage(), "UpdateURLPage");
    }
}
