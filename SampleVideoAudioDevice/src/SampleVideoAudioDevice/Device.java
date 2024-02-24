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
import jpos.config.*;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.bplaced.conrad.log4jpos.*;

/**
 * JposDevice based dummy implementation for JavaPOS GraphicDisplay and SoundPlayer, based on Videolan VLC player.
 * No real hardware access, uses VLC media player instead for sound and video playing and displaying images.<br>
 * This service supports limited error handling: In error case, setting ErrorResponse of an ErrorEvent object to ER_RETRY
 * will not work as expected: Instead of working from the point where the error occurred, operation restarts from beginning.<br>
 * The following properties can be set via jpos.xml:
 * <ul>
 *     <li>AssociatedHardTotalsDevice: Name of a HardTotals device to be supported in addition to file system. Default:
 *     an empty string (no HardTotals device). </li>
 *     <li>AudioBitRate: Bit rate for audio recording, commonly used values are 96, 128, 160, 192 or 256. Default: 128.
 *     Property must be specified with attribute type="Integer".</li>
 *     <li>AudioChannel<i>n</i>: Name of the <i>n</i>'th Audio channel, one of the texts shown as "audio device name" in
 *     VLC device recording open dialog. Several names may be specified, but with consecutive numbers <i>n</i>, always
 *     starting with 1.<br
 *     Keep in mind:
 *         <ul>
 *             <li>SoundRecorder will work only if at least <b>AudioChannel1</b> has been specified.</li>
 *             <li>If specified, VideoCapture will capture video with tone using property <b>Channel</b> of the
 *             corresponding SoundRecorder device, if opened and enabled.</li>
 *         </ul>
 *     </li>
 *     <li>BufferSize: Size of byte buffer to be used to retrieve data from storage or to send data to the storage, in byte.
 *     Default: 4096. Property must be specified with attribute type="Integer".</li>
 *     <li>MaximumTimeForImageLoadFailure: For GraphicDisplay only: Maximum time between process start of vlc and
 *     process exit after image load failure. Used in method imageLoad to verify if imageLoad failed: If vlc exits
 *     within MaximumTimeForImageLoadFailure milliseconds, imageLoad assumes there was a failure. Otherwise imageLoad
 *     will finish successfully. Default: 500. Property must be specified with attribute type="Integer".<br>
 *     This value should always be as low as possible because imageLoad needs always at least the specified
 *     milliseconds to finish successfully. If it is too low, loading errors are not reliably detected.</li>
 *     <li>TempFileFolder: Folder for temporary files. Will be used only in SoundRecorder and VideoCapture device
 *     service whenever Storage is the HardTotals device and in GraphicDisplay device service whenever images shall be
 *     loaded. Default: null (the temp folder).</li>
 *     <li>VideoBitRate: Bit rate for video recording, commonly used values are between 8000 and 40000, depending on
 *     camera resolution. Default: 8000. Property must be specified with attribute type="Integer".</li>
 *     <li>VideoChannel: Name of the Video device, one of the texts shown as "video device name" in
 *     VLC device recording open dialog.<br>
 *     <li>VLCPath: Path to VLC media player.</li>
 * </ul>
 * Method <b>DirectIO</b> of VideoCapture device can be used to capture a video with sound by calling
 * <i>directIO(1, null, "WithAudio")</i>. To deactivate VideoCapture with sound call <i>directIO(1, null, "NoAudio")</i>.
 * While video capture with audio has been activated, the following restrictions take place: <ul>
 *     <li>VideoRecording with sound is only possible if the corresponding SoundRecorder service has been opened,
 *     claimed and enabled. The corresponding properties for RecordingLevel, SamplingRate as well as the configured
 *     AudioBitRate will be used during VideoCapture. In this case, VideoCapture and SoundRecording at the same time
 *     will not be possible.</li>
 * </ul>
 */
public class Device extends JposDevice {
    private File VlcPath = null;
    private List<String> AudioChannel = new ArrayList<>();
    private String VideoChannel = null;
    private Integer AudioBitRate = null;
    private int VideoBitRate = 8000;
    private int BufferSize = 4096;
    private String AssociatedHardTotalsDevice = "";
    private String TempFileFolder = null;
    private int MaximumTimeForImageLoadFailure = 500;

    private boolean CommonRecording = false;

    public Device(String id) {
        super(id);
        soundPlayerInit( 1);
        soundRecorderInit(1);
        graphicDisplayInit(1);
        videoCaptureInit(1);
        CapPowerReporting = JposConst.JPOS_PR_NONE;
        PhysicalDeviceName = "Sample Sound And Video Device";
        PhysicalDeviceDescription = "Sample sound and video device based on usage of Videolan's vlx";
    }

    @Override
    public void checkProperties(JposEntry entries) throws JposException {
        super.checkProperties(entries);
        Object o = entries.getPropertyValue("VLCPath");
        String deviceclass = entries.getPropertyValue("deviceCategory").toString();
        check(o == null, JposConst.JPOS_E_NOSERVICE, "VLCPath not specified");
        check(!(VlcPath = new File(o.toString())).isFile(), JposConst.JPOS_E_NOSERVICE, "Invalid VLC path: " + o.toString());
        AssociatedHardTotalsDevice = "";
        if ((o = entries.getPropertyValue("AssociatedHardTotalsDevice")) != null)
            AssociatedHardTotalsDevice = o.toString();
        BufferSize = 4096;
        if ((o = entries.getPropertyValue("BufferSize")) != null)
            BufferSize = (Integer) o;
        if (member(deviceclass, new String[]{"SoundRecorder", "VideoCapture", "GraphicDisplay"})) {
            if ((o = entries.getPropertyValue("TempFileFolder")) != null) {
                JposDevice.check(!new File(o.toString()).isDirectory(), JposConst.JPOS_E_NOSERVICE, "Invalid TempFileFolder: " + o);
                TempFileFolder = o.toString();
            }
            if (deviceclass.equals("GraphicDisplay")) {
                MaximumTimeForImageLoadFailure = 500;
                if ((o = entries.getPropertyValue("MaximumTimeForImageLoadFailure")) != null)
                    MaximumTimeForImageLoadFailure = (Integer) o;
            } else {
                AudioBitRate = null;
                if ((o = entries.getPropertyValue("AudioBitRate")) != null)
                    AudioBitRate = Integer.parseInt(o.toString());
                if (deviceclass.equals("SoundRecorder")) {
                    AudioChannel.clear();
                    for (int i = 1; (o = entries.getPropertyValue("AudioChannel" + i)) != null; i++)
                        AudioChannel.add(o.toString());
                    check(AudioChannel.size() == 0, JposConst.JPOS_E_NOSERVICE, "No audio channel specified");
                } else if (deviceclass.equals("VideoCapture")) {
                    check((o = entries.getPropertyValue("VideoChannel")) == null, JposConst.JPOS_E_NOSERVICE, "No video channel specified");
                    VideoChannel = o.toString();
                    VideoBitRate = 8000;
                    if ((o = entries.getPropertyValue("VideoBitRate")) != null)
                        VideoBitRate = Integer.parseInt(o.toString());
                }
            }
        }
    }

    @Override
    public void changeDefaults(SoundPlayerProperties props) {
        MySoundPlayerProperties data = (MySoundPlayerProperties) props;
        data.BufferSize = BufferSize;
        super.changeDefaults(data);
        data.DeviceServiceVersion += 1;
        data.DeviceServiceDescription = "VLC based sound player";
        data.CapSoundTypeList = "mp3,mp2,aac,wav";
        data.CapAssociatedHardTotalsDevice = AssociatedHardTotalsDevice;
        data.CapStorage = data.CapAssociatedHardTotalsDevice.length() == 0 ? SoundPlayerConst.SPLY_CST_HOST_ONLY
                : SoundPlayerConst.SPLY_CST_ALL;
        data.Storage = SoundPlayerConst.SPLY_ST_HOST;
        data.CapVolume = true;
        data.Volume = 30;
        data.CurrentCommands = new ArrayList<>();
    }

    @Override
    public void changeDefaults(SoundRecorderProperties props) {
        MySoundRecorderProperties data = (MySoundRecorderProperties) props;
        data.AudioDeviceNames = AudioChannel.toArray(new String[AudioChannel.size()]);
        data.AudioBitRate = AudioBitRate;
        data.TempFileFolder = TempFileFolder;
        super.changeDefaults(data);
        data.DeviceServiceVersion += 1;
        data.DeviceServiceDescription = "VLC based sound recorder";
        data.CapSoundType = true;
        data.CapAssociatedHardTotalsDevice = AssociatedHardTotalsDevice;
        data.CapStorage = data.CapAssociatedHardTotalsDevice.length() == 0 ? SoundRecorderConst.SREC_CST_HOST_ONLY
                : SoundRecorderConst.SREC_CST_ALL;
        data.Storage = SoundRecorderConst.SREC_ST_HOST;
        data.CapSamplingRate = true;
        data.SamplingRateList = "11250,22500,44100,48000";
        data.CapSoundType = true;
        data.SoundTypeList = "mp3,flac,wav";
        data.TypeArgs.put("mp3", new String[]{"mp3", "mp3"});
        data.TypeArgs.put("flac", new String[]{"flac", "flac"});
        data.TypeArgs.put("wav", new String[]{"s16l", "wav"});
        if (AudioChannel.size() > 1)
            data.CapChannel = true;
        data.ChannelList = "1";
        for (int i = 2; i <= AudioChannel.size(); i++)
            data.ChannelList += "," + i;
        data.CurrentCommands = new ArrayList<>();
    }

    @Override
    public void changeDefaults(GraphicDisplayProperties props) {
        MyGraphicDisplayProperties data = (MyGraphicDisplayProperties) props;
        data.BufferSize = BufferSize;
        data.TempFileFolder = TempFileFolder;
        data.MaximumTimeForImageLoadFailure = MaximumTimeForImageLoadFailure;
        super.changeDefaults(data);
        data.DeviceServiceVersion += 1;
        data.DeviceServiceDescription = "VLC based Graphic Display for Video";
        data.CapVolume = true;
        data.Volume = 30;
        data.CapAssociatedHardTotalsDevice = AssociatedHardTotalsDevice;
        data.CapStorage = data.CapAssociatedHardTotalsDevice.length() == 0 ? GraphicDisplayConst.GDSP_CST_HOST_ONLY
                : GraphicDisplayConst.GDSP_CST_ALL;
        data.Storage = GraphicDisplayConst.GDSP_ST_HOST;
        data.CapVideoType = true;
        data.CapImageType = true;
        data.ImageTypeList = "jpg,bmp,png";
        data.VideoTypeList = "mkv,mpg,mp4";
        data.CurrentCommands = new ArrayList<>();
    }

    @Override
    public void changeDefaults(VideoCaptureProperties props) {
        MyVideoCaptureProperties data = (MyVideoCaptureProperties) props;
        data.VideoDeviceName = VideoChannel;
        data.VideoBitRate = VideoBitRate;
        data.AudioBitRate = AudioBitRate;
        data.BufferSize = BufferSize;
        data.TempFileFolder = TempFileFolder;
        super.changeDefaults(data);
        data.DeviceServiceVersion += 1;
        data.DeviceServiceDescription = "VLC based video capture device";
        data.CapAssociatedHardTotalsDevice = AssociatedHardTotalsDevice;
        data.CapPhoto = true;
        data.CapPhotoType = true;
        data.PhotoTypeList = "jpg,png,bmp";
        data.CapVideo = true;
        data.CapVideoType = true;
        data.VideoTypeList = "mpg-ac3,mpg-mp2" +
                ",mkv-mpeg2-aac,mkv-mpeg2-mp3,mkv-mpeg2-mp2,mkv-mpeg2-vorbis,mkv-mpeg2-ac3" +
                ",mkv-mpeg4-aac,mkv-mpeg4-mp3,mkv-mpeg4-mp2,mkv-mpeg4-vorbis,mkv-mpeg4-ac3" +
                ",mkv-h264-aac,mkv-h264-mp3,mkv-h264-mp2,mkv-h264-vorbis,mkv-h264-ac3" +
                ",mkv-avc1-aac,mkv-avc1-mp3,mkv-avc1-mp2,mkv-avc1-vorbis,mkv-avc1-ac3" +
                ",mkv-divx-aac,mkv-divx-mp3,mkv-divx-mp2,mkv-divx-vorbis,mkv-divx-ac3";
        data.TypeArgs.put("mkv-mpeg2-aac", new String[]{"mkv", "mpgv", "mp4a"});
        data.TypeArgs.put("mkv-mpeg2-mp3", new String[]{"mkv", "mpgv", "mp3"});
        data.TypeArgs.put("mkv-mpeg2-mp2", new String[]{"mkv", "mpgv", "mpga"});
        data.TypeArgs.put("mkv-mpeg2-vorbis", new String[]{"mkv", "mpgv", "vorb"});
        data.TypeArgs.put("mkv-mpeg2-ac3", new String[]{"mkv", "mpgv", "a52"});
        data.TypeArgs.put("mkv-mpeg4-aac", new String[]{"mkv", "mp4v", "mp4a"});
        data.TypeArgs.put("mkv-mpeg4-mp3", new String[]{"mkv", "mp4v", "mp3"});
        data.TypeArgs.put("mkv-mpeg4-mp2", new String[]{"mkv", "mp4v", "mpga"});
        data.TypeArgs.put("mkv-mpeg4-vorbis", new String[]{"mkv", "mp4v", "vorb"});
        data.TypeArgs.put("mkv-mpeg4-ac3", new String[]{"mkv", "mp4v", "a52"});
        data.TypeArgs.put("mkv-h264-aac", new String[]{"mkv", "x264", "mp4a"});
        data.TypeArgs.put("mkv-h264-mp3", new String[]{"mkv", "x264", "mp3"});
        data.TypeArgs.put("mkv-h264-mp2", new String[]{"mkv", "x264", "mpga"});
        data.TypeArgs.put("mkv-h264-vorbis", new String[]{"mkv", "x264", "vorb"});
        data.TypeArgs.put("mkv-h264-ac3", new String[]{"mkv", "x264", "a52"});
        data.TypeArgs.put("mkv-divx-aac", new String[]{"mkv", "div3", "mp4a"});
        data.TypeArgs.put("mkv-divx-mp3", new String[]{"mkv", "div3", "mp3"});
        data.TypeArgs.put("mkv-divx-mp2", new String[]{"mkv", "div3", "mpga"});
        data.TypeArgs.put("mkv-divx-vorbis", new String[]{"mkv", "div3", "vorb"});
        data.TypeArgs.put("mkv-divx-ac3", new String[]{"mkv", "div3", "a52"});
        data.TypeArgs.put("mkv-avc1-aac", new String[]{"mkv", "avc1", "mp4a"});
        data.TypeArgs.put("mkv-avc1-mp3", new String[]{"mkv", "avc1", "mp3"});
        data.TypeArgs.put("mkv-avc1-mp2", new String[]{"mkv", "avc1", "mpga"});
        data.TypeArgs.put("mkv-avc1-vorbis", new String[]{"mkv", "avc1", "vorb"});
        data.TypeArgs.put("mkv-avc1-ac3", new String[]{"mkv", "avc1", "a52"});
        data.TypeArgs.put("mpg-ac3", new String[]{"ps", "mpgv", "a52"});
        data.TypeArgs.put("mpg-mp2", new String[]{"ps", "mpgv", "mpga"});
        data.VideoFrameRate = props.VideoMaxFrameRate = 25;
        data.CurrentCommands = new ArrayList<>();
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
    public Boolean concurrentProcessingSupported(JposOutputRequest request) {
        return request instanceof PlaySound ? true : null;
    }

    private class MySoundPlayerProperties extends SoundPlayerProperties {
        protected MySoundPlayerProperties() {
            super(0);
        }

        HardTotals TheStorage = null;
        int BufferSize = 4096;

        @Override
        public void open() throws JposException {
            if (CapAssociatedHardTotalsDevice.length() > 0) {
                TheStorage = new HardTotals();
                try {
                    TheStorage.open(CapAssociatedHardTotalsDevice);
                } catch (JposException e) {
                    throw new JposException(JposConst.JPOS_E_NOSERVICE, e.getErrorCode(), e.getMessage(), e);
                }
            }
            super.open();
        }

        @Override
        public void close() throws JposException {
            if (TheStorage != null)
                TheStorage.close();
            super.close();
        }

        @Override
        public void release() throws JposException {
            clearOutput();
            super.release();
        }

        @Override
        public void clearOutput() throws JposException {
            super.clearOutput();
            synchronized (OutputIdListSync) {
                if (OutputIDList.length() > 0) {
                    OutputIDList = "";
                    EventSource.logSet("OutputIDList");
                }
            }
            if (TheStorage != null && TheStorage.getDeviceEnabled())
                TheStorage.setDeviceEnabled(false);
        }

        @Override
        public PlaySound playSound(String filename, boolean loop) throws JposException {
            File file = new File(filename);
            StorageIO storage;

            if (Storage == SoundPlayerConst.SPLY_ST_HOST)
                storage = new StorageIO(file.getParent());
            else {
                if (!TheStorage.getDeviceEnabled())
                    TheStorage.setDeviceEnabled(true);
                storage = new StorageIO(TheStorage);
            }
            String name = file.getName();
            check(!storage.checkFileExists(name), JposConst.JPOS_E_NOEXIST, "File " + filename + " does not exist.");
            PlaySound request = super.playSound(name, loop);
            request.AdditionalData = storage;
            return request;
        }

        @Override
        public void playSound(PlaySound request) throws JposException {
            try {
                BigDecimal volumefactor = new BigDecimal(Volume).divide(new BigDecimal("12.5"));
                String cmd[] = {
                        VlcPath.getCanonicalPath(), "-", "--qt-start-minimized", "--qt-minimal-view", "--qt-notification=0",
                        "--no-video", "--gain=" + volumefactor.toString(), "--play-and-exit"
                };
                handleEvent(new SoundPlayerStatusUpdateEvent(EventSource, SoundPlayerConst.SPLY_SUE_START_PLAY_SOUND, request.OutputID));
                Process proc = Runtime.getRuntime().exec(cmd);
                ProcessWaiter pw = new ProcessWaiter("PlaySoundWaiter" + request.OutputID, proc, request.Waiting,
                        (StorageIO) request.AdditionalData, request.getFileName(), BufferSize, request.getLoop());
                pw.start();
                request.Waiting.suspend(SyncObject.INFINITE);
                if (proc.isAlive())
                    proc.destroy();
                if (pw.Exception != null)
                    throw pw.Exception;
                handleEvent(new SoundPlayerStatusUpdateEvent(EventSource, SoundPlayerConst.SPLY_SUE_STOP_PLAY_SOUND, request.OutputID));
                if (pw.Storage.getOpenFileSize() != 0)
                    pw.Storage.getStorageData(null, -1);
                synchronized (OutputIdListSync) {
                    if (OutputIDList.length() == 0 && Storage != SoundPlayerConst.SPLY_ST_HOST && TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(false);
                }
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
        private  int AudioBitRate = 128;
        private String[] AudioDeviceNames = null;
        private String TempFileFolder = null;
        private Map<String, String[]> TypeArgs = new HashMap<>();

        HardTotals TheStorage = null;

        protected MySoundRecorderProperties() {
            super(0);
        }

        @Override
        public void open() throws JposException {
            if (CapAssociatedHardTotalsDevice.length() > 0) {
                TheStorage = new HardTotals();
                try {
                    TheStorage.open(CapAssociatedHardTotalsDevice);
                } catch (JposException e) {
                    throw new JposException(JposConst.JPOS_E_NOSERVICE, e.getErrorCode(), e.getMessage(), e);
                }
            }
            super.open();
        }

        @Override
        public void close() throws JposException {
            if (TheStorage != null)
                TheStorage.close();
            super.close();
        }

        @Override
        public void release() throws JposException {
            clearInput();
            super.release();
        }

        @Override
        public void retryInput() throws JposException {
            super.retryInput();
            new JposInputRequest(this).reactivate(true);
            Device.log(Level.DEBUG, LogicalName + ": Enter Retry input...");
        }

        /**
         * DirectIO can be used to enable or disable common use of audio device for sound recording and video capture
         * and to set the audio bit rate.
         *
         * @param cmd     Must be 1
         * @param data    int[1] containing the new bit rate or one of JPOS_TRUE or JPOS_FALSE, depending on detail. On
         *                return, data[0] will contain the previous value.
         * @param detail  If "SetCommonAudio", data[0] specifies whether the audio device shall be shared with the VideoCapture
         *                device (JPOS_TRUE) or not (JPOS_FALSE).<br>
         *                If "SetAudioBitRate", data[0] specifies the bit rate to be used for audio recording.
         * @return  Since the supported methods will be performed synchronously, device specific directIO calls return null.
         * @throws JposException
         */
        @Override
        public DirectIO directIO(int cmd, int[]data, Object detail) throws JposException {
            String det = detail == null ? ""
                    : ((detail.getClass().isArray() && Array.getLength(detail) == 1 ? Array.get(detail, 0) : detail).toString());
            if (cmd == 1) {
                if ("SetCommonAudio".equals(det) && data != null && data.length == 1 &&
                        member(data[0], new long[]{JposConst.JPOS_FALSE, JposConst.JPOS_TRUE}))
                {
                    boolean newvalue = data[0] == JposConst.JPOS_TRUE;
                    data[0] = CommonRecording ? JposConst.JPOS_TRUE : JposConst.JPOS_FALSE;
                    CommonRecording = newvalue;
                } else if ("SetAudioBitRate".equals(det) && data != null && data.length == 1) {
                    int newrate = data[0];
                    data[0] = AudioBitRate;
                    this.AudioBitRate = newrate;
                } else
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid detail: " + det);
                return null;
            }
            return super.directIO(cmd, data, detail);
        }

        File[] TempFile = {null};
        
        @Override
        public StartRecording startRecording(String fileName, boolean overWrite, int recordingTime) throws JposException {
            checkAsyncInputActive();
            File f = new File(fileName);
            try {
                String name = f.getName();
                Object obj = f.getParent();
                try {
                    resetTempFile(TempFile);
                    TempFile[0] = TempFileFolder == null ? File.createTempFile(this.getClass().getName(), "." + SoundType)
                            : File.createTempFile(this.getClass().getName(), "." + SoundType, new File(TempFileFolder));
                } catch (IOException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, 0, e.getMessage(), e);
                }
                if (Storage == SoundRecorderConst.SREC_ST_HARDTOTALS) {
                    obj = TheStorage;
                    if (!TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(true);
                } else if (Storage == SoundRecorderConst.SREC_ST_HOST_HARDTOTALS) {
                    obj = new Object[]{TheStorage, obj};
                    if (!TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(true);
                }
                StorageIO storage = new StorageIO(obj, SoundRecorderConst.ESREC_NOROOM);
                Boolean exists = storage.checkFileExists(name);
                check(!overWrite && (exists == null || exists), JposConst.JPOS_E_EXISTS, "File exists: " + fileName);
                int  maxsize = storage.getAvailableSpace(name);
                if (TempFile[0].getFreeSpace() < maxsize)
                    maxsize = (int) TempFile[0].getFreeSpace();
                StartRecording ret = super.startRecording(name, overWrite, recordingTime);
                ret.AdditionalData = new Object[]{
                        storage,
                        null,
                        startRecordingProcess(ret, getAudioRecordingCommand(maxsize, recordingTime), "SoundRecordingWaiter"),
                        System.currentTimeMillis(),
                        maxsize
                };
                return ret;
            } catch (JposException e) {
                resetTempFile(TempFile);
                throw e;
            }
        }

        @Override
        public void startRecording(StartRecording request) throws JposException {
            StorageIO storage = (StorageIO) Array.get(request.AdditionalData, 0);
            byte[] retrievedData = (byte[]) Array.get(request.AdditionalData, 1);
            Process proc = (Process) Array.get(request.AdditionalData, 2);
            long starttime = (long) Array.get(request.AdditionalData, 3);
            try {
                if (retrievedData == null) {
                    int maxsize = storage.getAvailableSpace(request.FileName);
                    if (TempFile[0].getFreeSpace() < maxsize)
                        maxsize = (int) TempFile[0].getFreeSpace();
                    handleEvent(new SoundRecorderStatusUpdateEvent(EventSource, SoundRecorderConst.SREC_SUE_START_SOUND_RECORDING));
                    recordingWithSizeCheck(request, proc, TempFile[0], starttime, Long.MAX_VALUE, maxsize);
                    JposDevice.check(!TempFile[0].exists() || TempFile[0].length() == 0, JposConst.JPOS_E_FAILURE, "Sound capture failed");
                    JposDevice.check(TempFile[0].length() > Integer.MAX_VALUE, JposConst.JPOS_E_FAILURE, "Capture more than 2GB data");
                    retrievedData = readDataFromTempFile(request);
                }
                storage.setStorageData(request.FileName, retrievedData, retrievedData.length);
                if (storage.getOpenFileSize() > 0)
                    storage.setStorageData(null, null, 0);
                if (Storage != SoundRecorderConst.SREC_ST_HOST) {
                    TheStorage.setDeviceEnabled(false);
                }
                handleEvent(new SoundRecorderStatusUpdateEvent(EventSource, SoundRecorderConst.SREC_SUE_STOP_SOUND_RECORDING));
                handleEvent(new SoundRecorderDataEvent(EventSource, 0, retrievedData));
                retrievedData = null;
            } catch (Exception e) {
                if (retrievedData == null)
                    retrievedData = new byte[0];
                Array.set(request.AdditionalData, 1, retrievedData);
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            } finally {
                if (retrievedData == null)
                    resetTempFile(TempFile);
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

        private String[] getAudioRecordingCommand(int maxsize, int recordingTime) throws JposException {
            int bitrate = SoundType.toLowerCase().equals("wav") ? Integer.parseInt(SamplingRate)  / 32 : AudioBitRate;
            int recordingtime = getRecordingtime(recordingTime, maxsize, bitrate);
            int devindex = Integer.parseInt(Channel) - 1;
            String[] typeArgs = TypeArgs.get(SoundType);
            try {
                String cmd[] = {
                        VlcPath.getCanonicalPath(), "--qt-start-minimized", "--qt-minimal-view", "--qt-notification=0", "dshow://",
                        ":dshow-vdev=none", ":dshow-adev=" + AudioDeviceNames[devindex], ":live-caching=300",
                        ":sout=#transcode{vcodec=none,acodec=" + typeArgs[0] +
                                ",ab=" + AudioBitRate +
                                ",samplerate=" + SamplingRate +
                                "}:standard{access=file,mux=" + typeArgs[1] + ",dst=\"" + TempFile[0].getCanonicalPath() + "\"}",
                        "--stop-time=" + recordingtime,
                        "vlc://quit"
                };
                return cmd;
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, e.getMessage(), e);
            }
        }

        private byte[] readDataFromTempFile(StartRecording request) throws IOException, JposException {
            byte[] retrievedData;
            retrievedData = new byte[(int) TempFile[0].length()];
            int len;
            FileInputStream in = new FileInputStream(TempFile[0]);
            try {
                for (int pos = 0; pos < retrievedData.length; pos += len) {
                    if ((len = in.read(retrievedData, pos, retrievedData.length - pos)) <= 0) {
                        retrievedData = Arrays.copyOf(retrievedData, pos);
                        throw new JposException(JposConst.JPOS_E_FAILURE, 0, "Access to all captured data failed at byte " + pos);
                    }
                }
            } finally {
                ((Object[]) request.AdditionalData)[1] = retrievedData;
                in.close();
                resetTempFile(TempFile);
            }
            return retrievedData;
        }
    }

    private class MyGraphicDisplayProperties extends GraphicDisplayProperties {
        protected MyGraphicDisplayProperties() {
            super(0);
        }

        HardTotals TheStorage = null;
        int BufferSize = 4096;
        String TempFileFolder = null;
        int MaximumTimeForImageLoadFailure = 500;

        @Override
        public void open() throws JposException {
            if (CapAssociatedHardTotalsDevice.length() > 0) {
                TheStorage = new HardTotals();
                try {
                    TheStorage.open(CapAssociatedHardTotalsDevice);
                } catch (JposException e) {
                    throw new JposException(JposConst.JPOS_E_NOSERVICE, e.getErrorCode(), e.getMessage(), e);
                }
            }
            super.open();
        }

        @Override
        public void close() throws JposException {
            if (TheStorage != null)
                TheStorage.close();
            super.close();
        }

        @Override
        public void release() throws JposException {
            clearOutput();
            destroyImageProcess();
            super.release();
        }

        @Override
        public void clearOutput() throws JposException {
            super.clearOutput();
            if (TheStorage != null && TheStorage.getDeviceEnabled())
                TheStorage.setDeviceEnabled(false);
            PlayingVideo[0] = null;
        }

        private Integer[] PlayingVideo = {null};
        private File[] TempFile = {null};

        @Override
        public PlayVideo playVideo(String filename, boolean loop) throws JposException {
            File file = new File(filename);
            StorageIO storage;

            if (Storage == GraphicDisplayConst.GDSP_ST_HOST)
                storage = new StorageIO(file.getParent());
            else {
                if (!TheStorage.getDeviceEnabled())
                    TheStorage.setDeviceEnabled(true);
                storage = new StorageIO(TheStorage);
            }
            String name = file.getName();
            check(!storage.checkFileExists(name), JposConst.JPOS_E_NOEXIST, "File " + filename + " does not exist.");
            PlayVideo request = super.playVideo(name, loop);
            request.AdditionalData = storage;
            return request;
        }

        @Override
        public void playVideo(PlayVideo request) throws JposException {
            synchronized (PlayingVideo) {
                synchronized (request) {
                    PlayingVideo[0] = request.OutputID;
                }
                try {
                    BigDecimal volumefactor = new BigDecimal(Volume).divide(new BigDecimal("12.5"));
                    resetTempFile(TempFile);
                    StorageIO storage[] = {(StorageIO) request.AdditionalData};
                    String[] cmd;
                    cmd = getVideoPlayingCommand(request, volumefactor, storage);
                    handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_START_PLAY_VIDEO));
                    for (boolean again = true; again; again = request.getLoop() && (request.Abort == null)) {
                        Process proc = Runtime.getRuntime().exec(cmd);
                        ProcessWaiter pw = new ProcessWaiter("GraphicDisplayWaiter" + request.OutputID, proc, request.Waiting,
                                storage[0], request.getFileName(), storage[0] == null ? 0 : BufferSize, false);
                        pw.TempFile = TempFile[0];
                        pw.start();
                        request.Waiting.suspend(SyncObject.INFINITE);
                        if (proc.isAlive())
                            proc.destroy();
                        if (pw.Exception != null)
                            throw pw.Exception;
                        if (storage[0] != null && storage[0].getOpenFileSize() != 0)
                            storage[0].getStorageData(null, -1);
                    }
                    handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_STOP_PLAY_VIDEO));
                    if (Storage != GraphicDisplayConst.GDSP_ST_HOST && TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(false);
                } catch (Exception e) {
                    if (e instanceof JposException)
                        throw (JposException) e;
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
                } finally {
                    synchronized (request) {
                        PlayingVideo[0] = null;
                    }
                    resetTempFile(TempFile);
                }
            }
        }

        private String[] getVideoPlayingCommand(PlayVideo request, BigDecimal volumefactor, StorageIO[] storage) throws JposException, IOException {
            String[] cmd;
            if (request.getLoop()) {
                if (storage[0].getStorageObject() instanceof HardTotals) {
                    TempFile[0] = TempFileFolder == null ? File.createTempFile(this.getClass().getName(), "." + ImageType)
                            : File.createTempFile(this.getClass().getName(), "." + ImageType, new File(TempFileFolder));
                    cmd = new String[]{
                            VlcPath.getCanonicalPath(), "-", "--qt-notification=0", "--qt-minimal-view", "--freetype-opacity=0",
                            "-f", "--gain=" + volumefactor.toString(), TempFile[0].getCanonicalPath(), ":input-repeat=65535",
                            DisplayMode == GraphicDisplayConst.GDSP_DMODE_VIDEO_FULL ? "--autoscale" : "--no-autoscale"
                    };
                } else {
                    File file = new File(storage[0].getStorageObject().toString(), request.getFileName());
                    cmd = new String[]{
                            VlcPath.getCanonicalPath(), file.getCanonicalPath(), "--qt-notification=0", "--qt-minimal-view",
                            "--freetype-opacity=0", "-f", "--gain=" + volumefactor.toString(), "--repeat",
                            DisplayMode == GraphicDisplayConst.GDSP_DMODE_VIDEO_FULL ? "--autoscale" : "--no-autoscale"
                    };
                    storage[0] = null;
                }
            } else {
                cmd = new String[]{
                        VlcPath.getCanonicalPath(), "-", "--qt-notification=0", "--qt-minimal-view", "--freetype-opacity=0",
                        "-f", "--gain=" + volumefactor.toString(), "--play-and-exit",
                        DisplayMode == GraphicDisplayConst.GDSP_DMODE_VIDEO_FULL ? "--autoscale" : "--no-autoscale"
                };
            }
            return cmd;
        }

        @Override
        public void stopVideo() throws JposException {
            JposOutputRequest request;
            synchronized (AsyncProcessorRunning) {
                request = getRequestRunnersRequest(SerializedRequestRunner);
                check (!(request instanceof PlayVideo), JposConst.JPOS_E_ILLEGAL, "No video playing");
            }
            request.abortCommand(true);
        }

        @Override
        public LoadImage loadImage(String filename) throws JposException {
            File file = new File(filename);
            StorageIO storage;

            if (Storage == GraphicDisplayConst.GDSP_ST_HOST)
                storage = new StorageIO(file.getParent());
            else {
                if (!TheStorage.getDeviceEnabled())
                    TheStorage.setDeviceEnabled(true);
                storage = new StorageIO(TheStorage);
            }
            String name = file.getName();
            check(!storage.checkFileExists(name), JposConst.JPOS_E_NOEXIST, "File " + filename + " does not exist.");
            LoadImage request = super.loadImage(name);
            request.AdditionalData = Storage == GraphicDisplayConst.GDSP_ST_HOST ? file : storage;
            return request;
        }

        Process ImageProcess = null;

        @Override
        public void loadImage(LoadImage request) throws JposException {
            File imagefile;
            try {
                handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_START_IMAGE_LOAD));
                destroyImageProcess();
                imagefile = getFileFromStorage(request);
                if (imagefile != null) {
                    String cmd[] = {
                            VlcPath.getCanonicalPath(), imagefile.getCanonicalPath(), "--qt-minimal-view", "-f",
                            "--image-duration=-1", "--no-qt-error-dialogs", "--freetype-opacity=0", "--qt-notification=0",
                            DisplayMode == GraphicDisplayConst.GDSP_DMODE_IMAGE_CENTER ? "--no-autoscale" : "--autoscale"
                    };
                    ImageProcess = Runtime.getRuntime().exec(cmd);
                    ProcessWaiter pw = new ProcessWaiter("LoadImageWaiter" + request.OutputID, ImageProcess,
                            request.Waiting, null, request.getFileName(), MaximumTimeForImageLoadFailure, false);
                    pw.start();
                    request.Waiting.suspend(SyncObject.INFINITE);
                    if (pw.Exception != null)
                        throw pw.Exception;
                    handleEvent(new GraphicDisplayStatusUpdateEvent(EventSource, GraphicDisplayConst.GDSP_SUE_END_IMAGE_LOAD));
                }
            } catch (Exception e) {
                resetTempFile(TempFile);
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            }
        }

        @Override
        public void displayMode(int mode) throws JposException {
            check (mode == GraphicDisplayConst.GDSP_DMODE_WEB, JposConst.JPOS_E_FAILURE, "Service does not support WEB mode");
            long[] imagemodi = {GraphicDisplayConst.GDSP_DMODE_IMAGE_FIT, GraphicDisplayConst.GDSP_DMODE_IMAGE_FILL, GraphicDisplayConst.GDSP_DMODE_IMAGE_CENTER};
            if (!member(mode, imagemodi) && member(DisplayMode, imagemodi)) {
                destroyImageProcess();
            }
            super.displayMode(mode);
        }

        private File getFileFromStorage(LoadImage request) throws Exception {
            File imagefile;
            if (request.AdditionalData instanceof StorageIO) {
                try {
                    resetTempFile(TempFile);
                    TempFile[0] = imagefile = TempFileFolder == null ? File.createTempFile(this.getClass().getName(), "." + ImageType)
                            : File.createTempFile(this.getClass().getName(), "." + ImageType, new File(TempFileFolder));
                    StorageIO storage = (StorageIO) request.AdditionalData;
                    byte[] buffer = storage.getStorageData(request.getFileName(), BufferSize, true);
                    try (FileOutputStream out = new FileOutputStream(imagefile)) {
                        while (!request.Waiting.suspend(0)) {
                            out.write(buffer);
                            if (buffer.length < BufferSize)
                                break;
                            buffer = storage.getStorageData(null, BufferSize);
                        }
                        if (buffer.length == BufferSize)
                            return null;
                    }
                } finally {
                    TheStorage.setDeviceEnabled(false);
                }
            } else
                imagefile = (File)(request.AdditionalData);
            return imagefile;
        }

        private void destroyImageProcess() {
            if (ImageProcess != null) {
                if (ImageProcess.isAlive())
                    ImageProcess.destroy();
                ImageProcess = null;
            }
            resetTempFile(TempFile);
        }
    }

    private class MyVideoCaptureProperties extends VideoCaptureProperties {
        public Integer AudioBitRate;
        private String VideoDeviceName;
        private String TempFileFolder;
        private int VideoBitRate;
        int BufferSize;
        private Map<String, String[]> TypeArgs = new HashMap<>();

        protected MyVideoCaptureProperties() {
            super(0);
        }

        HardTotals TheStorage = null;

        @Override
        public void open() throws JposException {
            if (CapAssociatedHardTotalsDevice.length() > 0) {
                TheStorage = new HardTotals();
                try {
                    TheStorage.open(CapAssociatedHardTotalsDevice);
                } catch (JposException e) {
                    throw new JposException(JposConst.JPOS_E_NOSERVICE, e.getErrorCode(), e.getMessage(), e);
                }
            }
            super.open();
        }

        @Override
        public void close() throws JposException {
            if (TheStorage != null)
                TheStorage.close();
            super.close();
        }

        @Override
        public void release() throws JposException {
            clearInput();
            super.release();
        }

        /**
         * DirectIO can be used to enable or disable common use of audio device for sound recording and video capture
         * and to set the audio and video bit rates.
         *
         * @param cmd     Must be 1
         * @param data    int[1] containing the new bit rate or one of JPOS_TRUE or JPOS_FALSE, depending on detail. On
         *                return, data[0] will contain the previous value.
         * @param detail  If "SetCommonAudio", data[0] specifies whether the audio device shall be shared with the VideoCapture
         *                device (JPOS_TRUE) or not (JPOS_FALSE).<br>
         *                If "SetAudioBitRate", data[0] specifies the bit rate to be used for audio recording. Keep in
         *                mind: Audio recording will be performed only if CommonAudio has been enabled.<br>
         *                If "SetVideoBitRate", data[0] specifies the bit rate to be used for video recording.
         * @return  Since the supported methods will be performed synchronously, device specific directIO calls return null.
         * @throws JposException
         */
        @Override
        public DirectIO directIO(int cmd, int[]data, Object detail) throws JposException {
            String det = detail == null ? ""
                    : ((detail.getClass().isArray() && Array.getLength(detail) == 1 ? Array.get(detail, 0) : detail).toString());
            if (cmd == 1) {
                if ("SetCommonAudio".equals(det) && data != null && data.length == 1 &&
                        member(data[0], new long[]{JposConst.JPOS_FALSE, JposConst.JPOS_TRUE}))
                {
                    boolean newvalue = data[0] == JposConst.JPOS_TRUE;
                    data[0] = CommonRecording ? JposConst.JPOS_TRUE : JposConst.JPOS_FALSE;
                    CommonRecording = newvalue;
                } else if ("SetVideoBitRate".equals(det) && data != null && data.length == 1) {
                    int newrate = data[0];
                    data[0] = VideoBitRate;
                    VideoBitRate = newrate;
                } else if ("SetAudioBitRate".equals(det) && data != null && data.length == 1) {
                    int newrate = data[0];
                    data[0] = AudioBitRate;
                    AudioBitRate = newrate;
                } else
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid detail: " + det);
                return null;
            }
            return super.directIO(cmd, data, detail);
        }

        File[] TempFile = {null};

        @Override
        public StartVideo startVideo(String fileName, boolean overWrite, int recordingTime) throws JposException {
            MySoundRecorderProperties sound = checkAsyncInputActive();
            File f = new File(fileName);
            try {
                String name = f.getName();
                Object obj = f.getParent();
                try {
                    resetTempFile(TempFile);
                    String extension = VideoType.split("-")[0];
                    TempFile[0] = TempFileFolder == null ? File.createTempFile(this.getClass().getName(), "." + extension)
                            : File.createTempFile(this.getClass().getName(), "." + extension, new File(TempFileFolder));
                } catch (IOException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, 0, e.getMessage(), e);
                }
                if (Storage == VideoCaptureConst.VCAP_ST_HARDTOTALS) {
                    obj = TheStorage;
                    if (!TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(true);
                } else if (Storage == VideoCaptureConst.VCAP_ST_HOST_HARDTOTALS) {
                    obj = new Object[]{TheStorage, obj};
                    if (!TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(true);
                }
                StorageIO storage = new StorageIO(obj, VideoCaptureConst.EVCAP_NOROOM);
                Boolean exists = storage.checkFileExists(name);
                check(!overWrite && (exists == null || exists), JposConst.JPOS_E_EXISTS, "File exists: " + fileName);
                int  maxsize = storage.getAvailableSpace(name);
                if (TempFile[0].getFreeSpace() < maxsize)
                    maxsize = (int) TempFile[0].getFreeSpace();
                StartVideo ret = super.startVideo(name, overWrite, recordingTime);
                ret.AdditionalData = new Object[] {
                        storage,
                        startRecordingProcess(ret, getVideoRecordingCommand(ret, sound, maxsize), "VideoCaptureWaiter"),
                        System.currentTimeMillis(),
                        maxsize,
                        false
                };
                return ret;
            } catch (JposException e) {
                resetTempFile(TempFile);
                throw e;
            }
        }

        @Override
        public void startVideo(StartVideo request) throws JposException {
            StorageIO storage = (StorageIO) Array.get(request.AdditionalData, 0);
            Process proc = (Process) Array.get(request.AdditionalData, 1);
            long starttime = (long) Array.get(request.AdditionalData, 2);
            int  maxsize = (int) Array.get(request.AdditionalData, 3);
            boolean retry = (boolean) Array.get(request.AdditionalData, 4);
            try {
                if (!retry) {
                    handleEvent(new VideoCaptureStatusUpdateEvent(EventSource, VideoCaptureConst.VCAP_SUE_START_VIDEO));
                    recordingWithSizeCheck(request, proc, TempFile[0], starttime, Long.MAX_VALUE, maxsize);
                }
                checkError(starttime);
                long remainder = TempFile[0].length();
                JposDevice.check(remainder > Integer.MAX_VALUE, JposConst.JPOS_E_FAILURE, "Capture more than 2GB data");
                tempFileToStorage(request.FileName, storage, (int) remainder);
                handleEvent(new VideoCaptureStatusUpdateEvent(EventSource, VideoCaptureConst.VCAP_SUE_STOP_VIDEO));
                retry = false;
            } catch (Exception e) {
                Array.set(request.AdditionalData, 4, retry = true);
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            } finally {
                if (!retry)
                    resetTempFile(TempFile);
            }
        }

        @Override
        public void retryInput() throws JposException {
            super.retryInput();
            new JposInputRequest(this).reactivate(true);
            Device.log(Level.DEBUG, LogicalName + ": Enter Retry input...");
        }

        private void checkError(long starttime) throws JposException {
            String check = System.currentTimeMillis() - starttime < 9000 ? ", recording time too low?" : "";
            JposDevice.check(!TempFile[0].exists() || TempFile[0].length() == 0, JposConst.JPOS_E_FAILURE,
                    "Video capture failed" + check);
        }

        @Override
        public void stopVideo() throws JposException {
            JposOutputRequest request = null;
            synchronized (AsyncProcessorRunning) {
                for (JposOutputRequest req : CurrentCommands) {
                    if (req instanceof  StartVideo && req.Props == this) {
                        request = req;
                        break;
                    }
                }
            }
            if (request != null)
                request.abortCommand();
        }

        @Override
        public TakePhoto takePhoto(String fileName, boolean overWrite, int timeout) throws JposException {
            File f = new File(fileName);
            try {
                String name = f.getName();
                Object obj = f.getParent();
                try {
                    resetTempFile(TempFile);
                    TempFile[0] = TempFileFolder == null ? File.createTempFile(this.getClass().getName(), "." + PhotoType)
                            : File.createTempFile(this.getClass().getName(), "." + PhotoType, new File(TempFileFolder));
                } catch (IOException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, 0, e.getMessage(), e);
                }
                if (Storage == VideoCaptureConst.VCAP_ST_HARDTOTALS) {
                    obj = TheStorage;
                    if (!TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(true);
                } else if (Storage == VideoCaptureConst.VCAP_ST_HOST_HARDTOTALS) {
                    obj = new Object[]{TheStorage, obj};
                    if (!TheStorage.getDeviceEnabled())
                        TheStorage.setDeviceEnabled(true);
                }
                StorageIO storage = new StorageIO(obj, VideoCaptureConst.EVCAP_NOROOM);
                Boolean exists = storage.checkFileExists(name);
                check(!overWrite && (exists == null || exists), JposConst.JPOS_E_EXISTS, "File exists: " + fileName);
                int  maxsize = storage.getAvailableSpace(name);
                if (TempFile[0].getFreeSpace() < maxsize)
                    maxsize = (int) TempFile[0].getFreeSpace();
                TakePhoto ret = super.takePhoto(name, overWrite, timeout);
                ret.AdditionalData = new Object[]{
                        storage,
                        startRecordingProcess(ret, getTakingPhotoCommand(ret), "TakePhotoWaiter"),
                        System.currentTimeMillis(),
                        maxsize,
                        false
                };
                return ret;
            } catch (JposException e) {
                resetTempFile(TempFile);
                throw e;
            }
        }

        @Override
        public void takePhoto(TakePhoto request) throws JposException {
            StorageIO storage = (StorageIO) Array.get(request.AdditionalData, 0);
            Process proc = (Process) Array.get(request.AdditionalData, 1);
            long starttime = (long) Array.get(request.AdditionalData, 2);
            int  maxsize = (int) Array.get(request.AdditionalData, 3);
            boolean retry = (boolean) Array.get(request.AdditionalData, 4);
            try {
                if (!retry) {
                    handleEvent(new VideoCaptureStatusUpdateEvent(EventSource, VideoCaptureConst.VCAP_SUE_START_PHOTO));
                    recordingWithSizeCheck(request, proc, TempFile[0], starttime,
                            request.Timeout == JposConst.JPOS_FOREVER ? Long.MAX_VALUE : request.Timeout, maxsize);
                }
                JposDevice.check(!TempFile[0].exists() || TempFile[0].length() == 0, JposConst.JPOS_E_FAILURE, "Taking photo failed");
                long remainder = TempFile[0].length();
                JposDevice.check(remainder > Integer.MAX_VALUE, JposConst.JPOS_E_FAILURE, "Capture more than 2GB data");
                tempFileToStorage(request.FileName, storage, (int) remainder);
                handleEvent(new VideoCaptureStatusUpdateEvent(EventSource, VideoCaptureConst.VCAP_SUE_STOP_VIDEO));
                retry = false;
            } catch (Exception e) {
                Array.set(request.AdditionalData, 4, retry = true);
                if (e instanceof JposException)
                    throw (JposException) e;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected error: " + e.getMessage(), e);
            } finally {
                if (!retry)
                    resetTempFile(TempFile);
            }
        }

        private MySoundRecorderProperties getAssociatedAudioProperties() {
            if (CommonRecording && ClaimedSoundRecorder != null)
                return (MySoundRecorderProperties) ClaimedSoundRecorder[0];
            return null;
        }

        private String[] getVideoRecordingCommand(StartVideo request, MySoundRecorderProperties sound, int maxsize) throws JposException {
            try {
                int bitrate = VideoBitRate;
                String adev, acodec;
                String[] typeArgs = TypeArgs.get(VideoType);
                if (sound == null) {
                    acodec = adev = "none";
                } else {
                    adev = sound.AudioDeviceNames[(Integer.parseInt(sound.Channel) - 1)];
                    acodec = typeArgs[2] + ",ab=" + AudioBitRate + ",samplerate=" + sound.SamplingRate + ",audio-sync";
                    bitrate += AudioBitRate;
                }
                int recordingtime = getRecordingtime(request.RecordingTime, maxsize, bitrate);
                String cmd[] = {
                        VlcPath.getCanonicalPath(), "--qt-start-minimized", "--qt-notification=0", "--qt-minimal-view", "dshow://",
                        ":dshow-vdev=" + VideoDeviceName, ":dshow-adev=" + adev, ":live-caching=300",
                        ":sout=#transcode{vcodec=" + typeArgs[1] + ",vb=" + VideoBitRate + ",fps=" + VideoFrameRate + ",acodec=" +
                                acodec + "}:standard{access=file,mux=" + typeArgs[0] + ",dst=\"" + TempFile[0].getCanonicalPath() + "\"}",
                        "--stop-time=" + recordingtime,
                        "vlc://quit"
                };
                return cmd;
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Start VideoRecording Error: " + e.getMessage(), e);
            }
        }

        private String[] getTakingPhotoCommand(TakePhoto request) throws JposException {
            try {
                String prefix = TempFile[0].getName().substring(0, TempFile[0].getName().length() - PhotoType.length() - 1);
                String cmd[] = {
                        VlcPath.getCanonicalPath(), "--qt-start-minimized", "--qt-notification=0", "--qt-minimal-view", "dshow://",
                        ":dshow-vdev=" + VideoDeviceName, ":dshow-adev=none", "--scene-replace", "--video-filter=scene",
                        "--scene-format=" + PhotoType, "--scene-prefix=" + prefix, "--scene-path=" + TempFile[0].getParent(),
                        "--scene-ratio=24", "--stop-time=0.05", "vlc://quit"
                };
                return cmd;
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Start TakePhoto Error: " + e.getMessage(), e);
            }
        }

        private void tempFileToStorage(String filename, StorageIO storage, int remainder) throws IOException, JposException {
            byte[] buffer = new byte[BufferSize];
            try (FileInputStream in = new FileInputStream(TempFile[0])){
                for (int len = 0; remainder > 0 && (len = in.read(buffer)) >= 0; filename = null)
                    remainder = storage.setStorageData(filename, buffer, remainder);
            }
            if (storage.getOpenFileSize() > 0)
                storage.setStorageData(null, null, 0);
            if (Storage != VideoCaptureConst.VCAP_ST_HOST) {
                TheStorage.setDeviceEnabled(false);
            }
        }
    }

    private MySoundRecorderProperties checkAsyncInputActive() throws JposException {
        if (CommonRecording) {
            JposCommonProperties props;
            if (ClaimedVideoCapture != null && (props = ClaimedVideoCapture[0]) != null) {
                if (((MyVideoCaptureProperties)props).VideoCaptureMode == VideoCaptureConst.VCAP_VCMODE_VIDEO)
                    check(props.AsyncInputActive, JposConst.JPOS_E_BUSY, "Video capture is busy");
            }
            if (ClaimedSoundRecorder != null && (props = ClaimedSoundRecorder[0]) != null) {
                check(props.AsyncInputActive, JposConst.JPOS_E_BUSY, "Sound Recorder is busy");
                return (MySoundRecorderProperties) props;
            }
        }
        return null;
    }

    private void resetTempFile(File[] tempfile) {
        try {
            if (tempfile[0] != null && tempfile[0].exists()) {
                tempfile[0].delete();
                tempfile[0] = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getRecordingtime(int recordingtime, int maxsize, long bytepersecond) {
        bytepersecond *= 125;   // 1/8 of 1000 because
        int maxtime = (int)((maxsize + bytepersecond - 1) / bytepersecond);
        return recordingtime == JposConst.JPOS_FOREVER || recordingtime > maxtime ? maxtime : recordingtime;
    }

    private Process startRecordingProcess(JposOutputRequest request,String[] cmd, String procname) throws JposException {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            new ProcessWaiter(procname, proc, request.Waiting, null, null, 0, false).start();
            return proc;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "StartRecording error: " + e.getMessage(), e);
        }
    }

    private void recordingWithSizeCheck(JposOutputRequest request, Process proc, File file, long starttime, long timeout, int maxsize) {
        for (long passed = 0; passed < timeout; passed = System.currentTimeMillis() - starttime) {
            request.Waiting.suspend(timeout - passed < 500 ? timeout - passed : 500);
            if ((file.exists() && file.length() > maxsize) || request.Abort != null || !proc.isAlive())
                break;
        }
        if (proc.isAlive())
            proc.destroy();
    }

    private class ProcessWaiter extends Thread {
        private Process Proc;
        private SyncObject Obj;
        private StorageIO Storage;
        private String Filename;
        File TempFile;
        private int BufferSize;
        private boolean Loop;
        private JposException Exception = null;
        private byte[][] Data = null;   // If set, 2nd target for retrieved data

        ProcessWaiter(String name, Process proc, SyncObject waiter, StorageIO storage, String filename, int buffersizeOrTimeout, boolean loop) {
            super(name);
            Proc = proc;
            Obj = waiter;
            Storage = storage;
            Filename = filename;
            BufferSize = buffersizeOrTimeout;
            Loop = loop;
        }

        @Override
        public void run() {
            if (Storage != null) {  // With storage I/O
                OutputStream output = Proc.getOutputStream();
                try {
                    eof:
                    do {
                        byte[] buffer = Storage.getStorageData(Filename, BufferSize);
                        FileOutputStream out = TempFile == null ? null : new FileOutputStream(TempFile);
                        while (buffer.length > 0) {
                            try {
                                output.write(buffer);
                                output.flush();
                                if (out != null)
                                    out.write(buffer);
                                if (buffer.length < BufferSize)
                                    break;
                            } catch (IOException e) {
                                break eof;
                            }
                            buffer = Storage.getStorageData(null, BufferSize);
                        }
                        if (out != null)
                            out.close();
                    } while (Loop);
                } catch (Throwable e) {
                    if (e instanceof JposException)
                        Exception = (JposException) e;
                    else if (e instanceof Exception)
                        Exception = new JposException(JposConst.JPOS_E_FAILURE, 0, e.getMessage(), (Exception) e);
                    else
                        Exception = new JposException(JposConst.JPOS_E_FAILURE, 0, e.getMessage());
                } finally {
                    try {
                        output.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    BufferSize = 0;
                }
            }
            long starttime = System.currentTimeMillis();
            long currenttime = starttime;
            while (BufferSize <= 0 || currenttime - starttime < BufferSize) {
                try {
                    if (BufferSize <= 0)
                        Proc.waitFor();
                    else if (Proc.waitFor(BufferSize - (int)(currenttime - starttime), TimeUnit.MILLISECONDS))
                        Exception = new JposException(JposConst.JPOS_E_FAILURE, "Could not load " + Filename);
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    currenttime = System.currentTimeMillis();
                }
            }
            BufferSize = (int)(System.currentTimeMillis() - starttime);
            Obj.signal();
        }
    }
}
