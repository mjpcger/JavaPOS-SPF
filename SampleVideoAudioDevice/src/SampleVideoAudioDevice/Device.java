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
package SampleVideoAudioDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.graphicdisplay.*;
import de.gmxhome.conrad.jpos.jpos_base.soundplayer.*;
import jpos.*;
import jpos.config.JposEntry;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JposDevice based dummy implementation for JavaPOS GraphicDisplay and SoundPlayer, based on Videolan VLC player.
 * No real hardware, uses VLC media player insted for sound and video playing and displaying images.<br>
 * The following properties can be set via jpos.xml:
 * <ul>
 *     <li>VLCPath: Path to VLC media player.</li>
 * </ul>
 * vlc command for a photo from camera:
 * vlc  dshow:// :dshow-adev=none ":dshow-vdev=USB2.0 HD UVC WebCam"
 *      --video-filter=scene --scene-format=jpg --scene-prefix=xxx --scene-path=e:\ --scene-ratio=24 --stop-time 1 vlc://quit
 * vlc command for audio capture from microphone:
 * vlc dshow:// :dshow-vdev=none ":dshow-adev=Mikrofon (Realtek(R) Audio)"  :live-caching=300
 *      :sout=#transcode{vcodec=none,acodec=mp3,ab=128,samplerate=44100,scodec=none}:file{dst="E:\xxyx.mp3"}
 * vlc command for video inclusive audio (minimum length 8 seconds for usable video):
 * vlc dshow:// ":dshow-vdev=USB2.0 HD UVC WebCam" ":dshow-adev=Mikrofon (Realtek(R) Audio)"  :live-caching=300
 *      :sout=#transcode{vcodec=x264,vb=10000,fps=25,acodec=aac,ab=128,samplerate=48000,audio-sync}:duplicate{dst=standard{access=file,mux=mkv,dst="E:\xxx.mkv"}}
 * vlc command for video capture from camera without audio (minimum length 8 seconds for usable video):
 * vlc dshow:// ":dshow-vdev=USB2.0 HD UVC WebCam" :live-caching=300
 *      :sout=#transcode{vcodec=x264,vb=10000,fps=25,acodec=none}:duplicate{dst=standard{access=file,mux=mkv,dst="E:\xxx.mkv"}}
 */
public class Device extends JposDevice {
    private File VlcPath = null;
    public Device(String id) {
        super(id);
        soundPlayerInit( 1);
        graphicDisplayInit(1);
        CapPowerReporting = JposConst.JPOS_PR_NONE;
        PhysicalDeviceName = "Sample Sound And Video Device";
        PhysicalDeviceDescription = "Sample sound and video device based on usage of Videolan's vlx";
        CurrentCommands = new ArrayList<>();
    }

    @Override
    public void checkProperties(JposEntry entries) throws JposException {
        super.checkProperties(entries);
        Object o = entries.getPropertyValue("VLCPath");
        check(o == null, JposConst.JPOS_E_NOSERVICE, "VLCPath not specified");
        check(!(VlcPath = new File(o.toString())).isFile(), JposConst.JPOS_E_NOSERVICE, "Invalid VLC path: " + o.toString());
    }

    @Override
    public void changeDefaults(SoundPlayerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "VLC based sound player";
        props.CapSoundTypeList = "wav,mp3,mp4";
        props.CapStorage = SoundPlayerConst.SPLY_CST_HOST_ONLY;
        props.Storage = SoundPlayerConst.SPLY_ST_HOST;
        props.CapVolume = true;
        props.Volume = 30;
    }

    @Override
    public void changeDefaults(GraphicDisplayProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "VLC based Graphic Display for Video";
        props.CapVolume = true;
        props.Volume = 30;
        props.CapStorage = GraphicDisplayConst.GDSP_CST_HOST_ONLY;
        props.Storage = GraphicDisplayConst.GDSP_ST_HOST;
        props.CapVideoType = true;
        props.CapImageType = true;
        props.ImageType = "JPG";
        props.ImageTypeList = "BMP,JPG";
        props.VideoType = "MP4";
        props.VideoTypeList = "MP4,MPG,AVI";
    }

    @Override
    public SoundPlayerProperties getSoundPlayerProperties(int index) {
        return new MySoundPlayerProperties();
    }

    @Override
    public GraphicDisplayProperties getGraphicDisplayProperties(int index) {
        return new MyGraphicDisplayProperties();
    }

    @Override
    public boolean concurrentProcessingSupported(JposOutputRequest request) {
        return request instanceof PlaySound;
    }
    private class MySoundPlayerProperties extends SoundPlayerProperties {
        protected MySoundPlayerProperties() {
            super(0);
        }

        @Override
        public void release() throws JposException {
            clearOutput();
            super.release();
        }

        @Override
        public void clearOutput() throws JposException {
            super.clearOutput();
            if (OutputIDList.length() > 0) {
                OutputIDList = "";
                EventSource.logSet("OutputIDList");
            }
        }

        @Override
        public PlaySound playSound(String filename, boolean loop) throws JposException {
            check(!new File(filename).isFile(), JposConst.JPOS_E_NOEXIST, "File " + filename + " does not exist.");
            return super.playSound(filename, loop);
        }

        @Override
        public void playSound(PlaySound request) throws JposException {
            // Always: --qt-start-minimized --qt-minimal-view --no-video
            // Loop Audio: -L
            // Audio playing once: --play-and-exit
            // Specify volume (0.0 - 8.0, e.g. 4 = 50%): --gain=4.0
            try {
                File f = new File(request.getFileName());
                BigDecimal volumefactor = new BigDecimal(Volume).divide(new BigDecimal("12.5"));
                String cmd[] = {
                        VlcPath.getCanonicalPath(), request.getFileName(), "--qt-start-minimized", "--qt-minimal-view",
                        "--no-video", "--gain=" + volumefactor.toString(), request.getLoop() ? "-L" : "--play-and-exit"
                };
                handleEvent(new SoundPlayerStatusUpdateEvent(EventSource, SoundPlayerConst.SPLY_SUE_START_PLAY_SOUND, request.OutputID));
                Process proc = Runtime.getRuntime().exec(cmd);
                new ProcessWaiter("PlaySoundWaiter" + request.OutputID, proc, request.Waiting);
                request.Waiting.suspend(SyncObject.INFINITE);
                if (proc.isAlive())
                    proc.destroy();
                handleEvent(new SoundPlayerStatusUpdateEvent(EventSource, SoundPlayerConst.SPLY_SUE_STOP_PLAY_SOUND, request.OutputID));
            } catch (Exception e) {
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            }
        }

        @Override
        public void stopSound(int outputID) throws JposException {
            JposOutputRequest request = null;
            synchronized (AsyncProcessorRunning) {
                for (JposOutputRequest req : CurrentCommands) {
                    if (req instanceof  PlaySound && req.OutputID == outputID) {
                        request = req;
                        break;
                    }
                }
            }
            if (request != null)
                request.abortCommand(true);
        }
    }

    private class MyGraphicDisplayProperties extends GraphicDisplayProperties {
        protected MyGraphicDisplayProperties() {
            super(0);
        }
        private Process LastProc;
        private Process setLastProc(Process proc) {
            if (LastProc != null && LastProc.isAlive())
                LastProc.destroy();
            return LastProc = proc;
        }

        @Override
        public void release() throws JposException {
            clearOutput();
            super.release();
        }

        @Override
        public PlayVideo playVideo(String filename, boolean loop) throws JposException {
            check(!new File(filename).isFile(), JposConst.JPOS_E_ILLEGAL, "No valid file: " + filename);
            return super.playVideo(filename, loop);
        }

        @Override
        public void playVideo(PlayVideo request) throws JposException {
            // Always: --qt-start-minimized --qt-minimal-view --no-video-title-show -f            // Loop Video: -L
            // Show Video once: --play-and-exit
            // In original size: --no_autoscale
            // With zoom factor (e.g. 1,1): --zoom=1.1

            try {
                File f = new File(request.getFileName());
                BigDecimal volumefactor = new BigDecimal(Volume).divide(new BigDecimal("12.5"));
                String cmd[] = {
                        VlcPath.getCanonicalPath(), request.getFileName(), "--qt-start-minimized", "--qt-minimal-view",
                        "-f", "--gain=" + volumefactor.toString(), request.getLoop() ? "-L" : "--play-and-exit",
                        DisplayMode == GraphicDisplayConst.GDSP_DMODE_VIDEO_FULL ? "--autoscale" : "--no-autoscale"
                };
                handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_START_PLAY_VIDEO));
                new ProcessWaiter("PlayVideoWaiter" + request.OutputID, setLastProc(Runtime.getRuntime().exec(cmd)), request.Waiting);
                request.Waiting.suspend(SyncObject.INFINITE);
                setLastProc(null);
                handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_STOP_PLAY_VIDEO));
            } catch (Exception e) {
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            }
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
        public LoadImage loadImage(String filename) throws JposException {
            check(!new File(filename).isFile(), JposConst.JPOS_E_ILLEGAL, "No valid file: " + filename);
            return super.loadImage(filename);
        }

        @Override
        public void loadImage(LoadImage request) throws JposException {
            // Always: --qt-start-minimized --qt-minimal-view --no-video-title-show -no-audio -f
            // Show image full-screen: --play-and-pause
            // Show image original size: --no-autoscale
            // With zoom factor (e.g. 1,1): --zoom=1.1


            try {
                File f = new File(request.getFileName());
                String cmd[] = {
                        VlcPath.getCanonicalPath(), request.getFileName(), "--qt-start-minimized", "--qt-minimal-view",
                        "--no-audio", "-f", DisplayMode == GraphicDisplayConst.GDSP_DMODE_IMAGE_CENTER ? "--no-autoscale" : "--autoscale",
                        "--play-and-pause"
                };
                handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_START_PLAY_VIDEO));
                new ProcessWaiter("PlayVideoWaiter" + request.OutputID, setLastProc(Runtime.getRuntime().exec(cmd)), request.Waiting);
                handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_STOP_PLAY_VIDEO));
            } catch (Exception e) {
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            }
        }

        @Override
        public void displayMode(int mode) throws JposException {
            check (mode == GraphicDisplayConst.GDSP_DMODE_WEB, JposConst.JPOS_E_FAILURE, "Service does not support WEB mode");
            long[] imagemodi = {GraphicDisplayConst.GDSP_DMODE_IMAGE_FIT, GraphicDisplayConst.GDSP_DMODE_IMAGE_FILL, GraphicDisplayConst.GDSP_DMODE_IMAGE_CENTER};
            if (member(DisplayMode, imagemodi) && !member(mode, imagemodi)) {
                setLastProc(null);
            }
            super.displayMode(mode);
        }
    }

    private class ProcessWaiter extends Thread {
        private Process Proc;
        private SyncObject Obj;
        ProcessWaiter(String name, Process proc, SyncObject waiter) {
            super(name);
            Proc = proc;
            Obj = waiter;
            start();
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Proc.waitFor();
                    break;
                } catch (InterruptedException e) {}
            }
            Obj.signal();
        }
    }
}
