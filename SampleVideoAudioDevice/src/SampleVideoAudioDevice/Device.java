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
import de.gmxhome.conrad.jpos.jpos_base.soundrecorder.*;
import de.gmxhome.conrad.jpos.jpos_base.videocapture.*;
import jpos.*;
import jpos.config.JposEntry;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JposDevice based dummy implementation for JavaPOS GraphicDisplay and SoundPlayer, based on Videolan VLC player.
 * No real hardware, uses VLC media player insted for sound and video playing and displaying images.<br>
 * The following properties can be set via jpos.xml:
 * <ul>
 *     <li>AudioBitRate: Bit rate for audio recording, commonly used values are 96, 128, 160, 192 or 256. Default: 128</li>
 *     <li>AudioChannel<i>n</i>: Name of the <i>n</i>'th Audio channel, one of the texts shown as "audio device name" in
 *     VLC device recording open dialog. Several names may be specified, but with consecutive numbers <i>n</i>, always
 *     starting with 1.<br
 *     Keep in mind:
 *         <ul>
 *             <li>SoundRecorder will work only if at least <b>AudioChannel1</b> has been specified.</li>
 *             <li>If specified, VideoCapture will capture video with tone using <b>AudioChannel1</b>. Otherwise, video will be
 *             captured without tone.</li>
 *         </ul>
 *     </li>
 *     <li>MaxSoundDataSize: Maximum sound data size in byte. Default is 0 (sound data will be stored in file system
 *     only). Property must be specified with attribute type="Integer".</li>
 *     <li>VideoBitRate: Bit rate for video recording, commonly used values are between 8000 and 40000, depending on
 *     camera resolution. Default: 8000.</li>
 *     <li>VideoChannel: Name of the Video device, one of the texts shown as "video device name" in
 *     VLC device recording open dialog.<br
 *     Keep in mind: VideoCapture will work only if at least <b>VideoChannel</b> has been specified. To capture video
 *     with tone, AudioChannel1 must be specified as well.</li>
 *     <li>VLCPath: Path to VLC media player.</li>
 * </ul>
 */
public class Device extends JposDevice {
    private File VlcPath = null;
    private List<String> AudioChannel = new ArrayList<>();
    private String VideoChannel = null;
    private int MaxSoundDataSize = 0;
    private int AudioBitRate = 128;
    private int VideoBitRate = 8000;

    public Device(String id) {
        super(id);
        soundPlayerInit( 1);
        soundRecorderInit(1);
        graphicDisplayInit(1);
        videoCaptureInit(1);
        CapPowerReporting = JposConst.JPOS_PR_NONE;
        PhysicalDeviceName = "Sample Sound And Video Device";
        PhysicalDeviceDescription = "Sample sound and video device based on usage of Videolan's vlx";
        CurrentCommands = new ArrayList<>();
    }

    @Override
    public void checkProperties(JposEntry entries) throws JposException {
        super.checkProperties(entries);
        Object o = entries.getPropertyValue("VLCPath");
        String deviceclass = entries.getPropertyValue("deviceCategory").toString();
        check(o == null, JposConst.JPOS_E_NOSERVICE, "VLCPath not specified");
        check(!(VlcPath = new File(o.toString())).isFile(), JposConst.JPOS_E_NOSERVICE, "Invalid VLC path: " + o.toString());
        if (member(deviceclass, new String[]{"SoundRecorder", "VideoCapture"})) {
            AudioChannel.clear();
            for (int i = 1; (o = entries.getPropertyValue("AudioChannel" + i)) != null; i++)
                AudioChannel.add(o.toString());
            AudioBitRate = 128;
            if ((o = entries.getPropertyValue("AudioBitRate")) != null)
                AudioBitRate = Integer.parseInt(o.toString());
            if (deviceclass.equals("SoundRecorder")) {
                check(AudioChannel.size() == 0, JposConst.JPOS_E_NOSERVICE, "No audio channel specified");
                MaxSoundDataSize = (o = entries.getPropertyValue("MaxSoundDataSize")) != null ? (int) o : 0;
            } else {
                check((o = entries.getPropertyValue("VideoChannel")) != null, JposConst.JPOS_E_NOSERVICE, "No video channel specified");
                check(AudioChannel.size() > 1, JposConst.JPOS_E_NOSERVICE, "Multiple audio channels not supported");
                VideoChannel = o.toString();
                if (AudioChannel.size() == 0)
                    AudioChannel.add("none");
                VideoBitRate = 8000;
                if ((o = entries.getPropertyValue("VideoBitRate")) != null)
                    VideoBitRate = Integer.parseInt(o.toString());
            }
        }
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
    public void changeDefaults(SoundRecorderProperties props) {
        ((MySoundRecorderProperties) props).MaxSoundDataSize = MaxSoundDataSize;
        ((MySoundRecorderProperties) props).AudioDeviceNames = AudioChannel.toArray(new String[AudioChannel.size()]);
        ((MySoundRecorderProperties) props).AudioBitRate = AudioBitRate;
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "VLC based sound recorder";
        props.CapSoundType = true;
        props.CapStorage = SoundPlayerConst.SPLY_CST_HOST_ONLY;
        props.Storage = SoundPlayerConst.SPLY_ST_HOST;
        props.CapSamplingRate = true;
        props.SamplingRateList = "11250,22500,44100,48000";
        props.SamplingRate = props.SamplingRateList.split(",")[0];
        props.SoundType = props.SoundTypeList = "mp3";
        if (AudioChannel.size() > 1)
            props.CapChannel = true;
        props.Channel = props.ChannelList = "1";
        for (int i = 2; i <= AudioChannel.size(); i++)
            props.ChannelList += "," + i;
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
    public void changeDefaults(VideoCaptureProperties props) {
        ((MyVideoCaptureProperties) props).VideoDeviceName = VideoChannel;
        ((MyVideoCaptureProperties) props).AudioDeviceName = AudioChannel.get(0);
        ((MyVideoCaptureProperties) props).AudioBitRate = AudioBitRate;
        ((MyVideoCaptureProperties) props).VideoBitRate = VideoBitRate;
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "VLC based video capture device";
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
    public SoundRecorderProperties getSoundRecorderProperties(int index) {
        return new MySoundRecorderProperties();
    }

    @Override
    public VideoCaptureProperties getVideoCaptureProperties(int index) {
        return new MyVideoCaptureProperties();
    }

    @Override
    public boolean concurrentProcessingSupported(JposOutputRequest request) {
        return request instanceof PlaySound || request instanceof StartRecording;
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
                    if (req instanceof  PlaySound && req.OutputID == outputID && req.Props == this) {
                        request = req;
                        break;
                    }
                }
            }
            if (request != null)
                request.abortCommand(true);
        }
    }

    private class MySoundRecorderProperties extends SoundRecorderProperties {
        public int AudioBitRate = 128;
        private int MaxSoundDataSize = 0;
        private String[] AudioDeviceNames = null;

        protected MySoundRecorderProperties() {
            super(0);
        }

        @Override
        public void release() throws JposException {
            clearInput();
            super.release();
        }

        @Override
        public void clearInput() throws JposException {
            super.clearInput();
            RecordingIsActive = false;
        }

        private Boolean RecordingIsActive = false;

        @Override
        public StartRecording startRecording(String fileName, boolean overWrite, int recordingTime) throws JposException {
            File f = new File(fileName);
            try {
                if (f.exists()) {
                    check(!f.isFile() || !overWrite, JposConst.JPOS_E_EXISTS, "File exists: " + fileName);
                    f.delete();
                }
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, 0, "Cannot delete " + fileName + ": " + e.getMessage(), e);
            }
            return super.startRecording(fileName, overWrite, recordingTime);
        }

        @Override
        public void startRecording(StartRecording request) throws JposException {
            /* vlc command for audio capture from microphone:
             * vlc dshow:// :dshow-vdev=none ":dshow-adev=[AudioDeviceNames[Channel - 1]]"  :live-caching=300
             *      :sout=#transcode{vcodec=none,acodec=[SoundType],ab=[AudioBitRate],samplerate=[SamplingRate],scodec=none}:file{dst="[FileName]"}
             *       --stop-time [RecordingTime] vlc://quit
             */
            try {
                File f = new File(request.FileName);
                MySoundRecorderProperties props = (MySoundRecorderProperties) request.Props;
                int devindex = Integer.parseInt(request.Channel) - 1;
                String cmd[] = {
                        VlcPath.getCanonicalPath(), "--qt-start-minimized", "--qt-minimal-view", "dshow://",
                        ":dshow-vdev=none", ":dshow-adev=" + props.AudioDeviceNames[devindex], ":live-caching=300",
                        ":sout=#transcode{vcodec=none,acodec=" + props.SoundType.toLowerCase() +
                                ",ab=" + props.AudioBitRate +
                                ",samplerate=" + request.SamplingRate +
                                ",scodec=none}:file{dst=\"" + f.getAbsolutePath() + "\"}",
                        "--stop-time=" + request.RecordingTime,
                        "vlc://quit"
                };
                handleEvent(new SoundRecorderStatusUpdateEvent(EventSource, SoundRecorderConst.SREC_SUE_START_SOUND_RECORDING));
                Process proc = Runtime.getRuntime().exec(cmd);
                new ProcessWaiter("SoundRecordingWaiter" + request.OutputID, proc, request.Waiting);
                request.Waiting.suspend(SyncObject.INFINITE);
                if (proc.isAlive())
                    proc.destroy();
                handleEvent(new SoundRecorderStatusUpdateEvent(EventSource, SoundRecorderConst.SREC_SUE_STOP_SOUND_RECORDING));
                byte[] data = new byte[f.length() > props.MaxSoundDataSize ? props.MaxSoundDataSize : (int)f.length()];
                if (data.length > 0) {
                    try (InputStream instream = new FileInputStream(f)) {
                        int pos = 0;
                        while (pos < data.length) {
                            int len = instream.read(data, pos, data.length - pos);
                            if (len >= 0)
                                pos += len;
                            else
                                break;
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                handleEvent(new SoundRecorderDataEvent(props.EventSource, 0, data));
            } catch (Exception e) {
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            }
        }

        @Override
        public void stopRecording() throws JposException {
            JposOutputRequest request = null;
            synchronized (AsyncProcessorRunning) {
                for (JposOutputRequest req : CurrentCommands) {
                    if (req instanceof  StartRecording && req.Props == this) {
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

    private class MyVideoCaptureProperties extends VideoCaptureProperties {
        private String AudioDeviceName = null;
        private String VideoDeviceName = null;
        private int AudioBitRate = 128;
        private int VideoBitRate = 8000;
        protected MyVideoCaptureProperties() {
            super(0);
        }
        /*
         * vlc command for a photo from camera:
         * vlc  dshow:// :dshow-adev=none ":dshow-vdev=USB2.0 HD UVC WebCam"
         *      --video-filter=scene --scene-format=jpg --scene-prefix=xxx --scene-path=e:\ --scene-ratio=24 --stop-time 1 vlc://quit
         * vlc command for video inclusive audio (minimum length 8 seconds for usable video):
         * vlc dshow:// ":dshow-vdev=USB2.0 HD UVC WebCam" ":dshow-adev=Mikrofon (Realtek(R) Audio)"  :live-caching=300
         *      :sout=#transcode{vcodec=x264,vb=10000,fps=25,acodec=aac,ab=128,samplerate=48000,audio-sync}:duplicate{dst=standard{access=file,mux=mkv,dst="E:\xxx.mkv"}}
         * vlc command for video capture from camera without audio (minimum length 8 seconds for usable video):
         * vlc dshow:// ":dshow-vdev=USB2.0 HD UVC WebCam" :live-caching=300
         *      :sout=#transcode{vcodec=x264,vb=10000,fps=25,acodec=none}:duplicate{dst=standard{access=file,mux=mkv,dst="E:\xxx.mkv"}}
         */
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
