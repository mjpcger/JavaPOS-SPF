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

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.GraphicDisplayConst.*;
import static jpos.JposConst.*;

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

    private final GraphicDisplayProperties Data;

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
        check(!Data.CapBrightness && brightness != Data.Brightness, JPOS_E_ILLEGAL, "Changing Brightness Illegal");
        check(brightness < 0 || brightness > 100, JPOS_E_ILLEGAL, "Brightness Must Be Between 0 And 100: " + brightness);
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
        long[] valid = { GDSP_DMODE_HIDDEN, GDSP_DMODE_WEB };
        long[] validImage = { GDSP_DMODE_IMAGE_FIT, GDSP_DMODE_IMAGE_FILL, GDSP_DMODE_IMAGE_CENTER };
        long[] validVideo = { GDSP_DMODE_VIDEO_NORMAL, GDSP_DMODE_VIDEO_FULL };
        checkEnabled();
        if (!member(displayMode, valid)) {
            if (!Data.CapImageType || !member(displayMode, validImage)) {
                if (!Data.CapVideoType || !member(displayMode, validVideo))
                    throw new JposException(JPOS_E_ILLEGAL, "DisplayMode Invalid: " + displayMode);
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
        check(!Data.CapImageType, JPOS_E_ILLEGAL, "Image Mode Not Supported");
        check(!member(imageType, Data.ImageTypeList.split(",")), JPOS_E_ILLEGAL, "ImageType Illegal: " + imageType);
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
        check(Data.LoadStatus == null, JPOS_E_ILLEGAL, "Load Status Not Available");
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
        long[] valid = { GDSP_ST_HOST, GDSP_ST_HARDTOTALS };
        check(Data.CapStorage == GDSP_CST_HOST_ONLY && storage != GDSP_ST_HOST, JPOS_E_ILLEGAL, "Storage not host: " + storage);
        check(Data.CapStorage == GDSP_CST_HARDTOTALS_ONLY && storage != GDSP_ST_HARDTOTALS, JPOS_E_ILLEGAL, "Storage not hard total: " + storage);
        checkMember(storage, valid, JPOS_E_ILLEGAL, "Unsupported storage: " + storage);
        GraphicDisplay.storage(storage);
        logSet("Storage");
    }

    @Override
    public String getURL() throws JposException {
        checkOpened();
        check(Data.URL == null, JPOS_E_ILLEGAL, "Load Status Not Available");
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
        check(!Data.CapVideoType, JPOS_E_ILLEGAL, "Video Mode Not Supported.");
        String[] valid = Data.VideoTypeList.split(",");
        check(!member(videoType, valid), JPOS_E_ILLEGAL, "VideoType Not Supported: " + videoType);
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
        check(!Data.CapVolume && volume != Data.Volume, JPOS_E_ILLEGAL, "Volume Change Not Supported");
        check(volume < 0 || volume > 100, JPOS_E_ILLEGAL, "Volume Must Be Between 0 And 100: " + volume);
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
        check(!UrlLoading, JPOS_E_ILLEGAL, "No URL Loading");
        GraphicDisplay.cancelURLLoading();
        logCall("CancelURLLoading");
    }

    @Override
    public void goURLBack() throws JposException {
        logPreCall("GoURLBack");
        checkEnabled();
        check(Data.DisplayMode == GDSP_DMODE_HIDDEN, JPOS_E_ILLEGAL, "Bad Mode");
        callIt(GraphicDisplay.goURLBack(), "GoURLBack");
    }

    @Override
    public void goURLForward() throws JposException {
        logPreCall("GoURLForward");
        checkEnabled();
        check(Data.DisplayMode == GDSP_DMODE_HIDDEN, JPOS_E_ILLEGAL, "Bad Mode");
        callIt(GraphicDisplay.goURLForward(), "GoURLForward");
    }

    @Override
    public void loadImage(String s) throws JposException {
        logPreCall("LoadImage", removeOuterArraySpecifier(new Object[]{s}, Device.MaxArrayStringElements));
        if (s == null)
            s = "";
        long[] valid = { GDSP_DMODE_IMAGE_FIT, GDSP_DMODE_IMAGE_FILL, GDSP_DMODE_IMAGE_CENTER };
        checkEnabled();
        check(!Data.CapImageType, JPOS_E_ILLEGAL, "No Image Mode Support");
        checkMember(Data.DisplayMode, valid, JPOS_E_ILLEGAL, "Invalid DisplayMode For LoadImage: " + Data.DisplayMode);
        callIt(GraphicDisplay.loadImage(s), "LoadImage");
    }

    @Override
    public void loadURL(String s) throws JposException {
        logPreCall("LoadURL", removeOuterArraySpecifier(new Object[]{s}, Device.MaxArrayStringElements));
        if (s == null)
            s = "";
        checkEnabled();
        check(Data.DisplayMode == GDSP_DMODE_HIDDEN, JPOS_E_ILLEGAL, "Bad Mode");
        check(s.length() == 0, JPOS_E_ILLEGAL, "Empty URL");
        callIt(GraphicDisplay.loadURL(s), "LoadURL");
    }

    @Override
    public void playVideo(String s, boolean b) throws JposException {
        logPreCall("PlayVideo", removeOuterArraySpecifier(new Object[]{s, b}, Device.MaxArrayStringElements));
        if (s == null)
            s = "";
        long[] valid = { GDSP_DMODE_VIDEO_NORMAL, GDSP_DMODE_VIDEO_FULL };
        checkEnabled();
        check(!Data.CapVideoType, JPOS_E_ILLEGAL, "No Video Mode Support");
        checkMember(Data.DisplayMode, valid, JPOS_E_ILLEGAL, "Invalid DisplayMode For PlayVideo: " + Data.DisplayMode);
        callIt(GraphicDisplay.playVideo(s, b), "PlayVideo");
    }

    @Override
    public void stopVideo() throws JposException {
        logPreCall("StopVideo");
        checkEnabled();
        check(!VideoPlaying, JPOS_E_ILLEGAL, "No Video Playing");
        GraphicDisplay.stopVideo();
        logCall("StopVideo");
    }

    @Override
    public void updateURLPage() throws JposException {
        logPreCall("UpdateURLPage");
        checkEnabled();
        check(Data.DisplayMode == GDSP_DMODE_HIDDEN, JPOS_E_ILLEGAL, "Bad Mode");
        check(UrlLoading, JPOS_E_ILLEGAL, "URL Loading");
        callIt(GraphicDisplay.updateURLPage(), "UpdateURLPage");
    }
}
