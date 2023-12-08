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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.GraphicDisplay;
import jpos.GraphicDisplayConst;
import jpos.JposException;

public class GraphicDisplayProperties extends JposCommonProperties implements GraphicDisplayInterface {
    /**
     * UPOS property Brightness. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Brightness = 0;

    /**
     * UPOS property CapAssociatedHardTotalsDeviceCapAssociatedHardTotalsDevice. Default: An empty string. Must be overwritten by
     * objects derived from JposDevice within the changeDefaults method if CapStorage is not GDSP_CST_HOST_ONLY.
     */
    public String CapAssociatedHardTotalsDevice = "";

    /**
     * UPOS property CapBrightness. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBrightness = false;

    /**
     * UPOS property CapImageType. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapImageType = false;

    /**
     * UPOS property CapStorage. Default: GDSP_CST_HARDTOTALS_ONLY. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapStorage = GraphicDisplayConst.GDSP_CST_HOST_ONLY;

    /**
     * UPOS property CapURLBack. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapURLBack = false;

    /**
     * UPOS property CapURLForward. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapURLForward = false;

    /**
     * UPOS property CapVideoType. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVideoType = false;

    /**
     * UPOS property CapVolume. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVolume = false;

    /**
     * UPOS property DisplayMode. Default: GDSP_DMODE_HIDDEN. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int DisplayMode = GraphicDisplayConst.GDSP_DMODE_HIDDEN;

    /**
     * UPOS property ImageType. Default: An empty string. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String ImageType = "";

    /**
     * UPOS property ImageTypeList. Default: An empty string. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method if the device supports displaying images.
     */
    public String ImageTypeList = "";

    /**
     * UPOS property LoadStatus. Default: null. Will be overwritten by objects derived from JposDevice before
     * GraphisDisplayStatusUpdateEvent delivery.
     */
    public Integer LoadStatus = null;

    /**
     * UPOS property Storage. Default: GDSP_ST_HOST. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Storage = GraphicDisplayConst.GDSP_ST_HOST;

    /**
     * UPOS property URL. Default: null. must be overwritten by objects derived from JposDevice before StatusUpdateEvent
     * delivery.
     */
    public String URL = null;

    /**
     * UPOS property VideoType. Default: An empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String VideoType = "";

    /**
     * UPOS property VideoTypeList. Default: An empty string. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method if the device supports playing video.
     */
    public String VideoTypeList = "";

    /**
     * UPOS property Volume. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Volume = 0;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected GraphicDisplayProperties(int dev) {
        super(dev);
        DeviceServiceVersion = 1016000;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (!enable) {
            if (LoadStatus != null) {
                LoadStatus = null;
                EventSource.logSet("LoadStatus");
            }
            ((GraphicDisplayService)EventSource).VideoPlaying = ((GraphicDisplayService)EventSource).UrlLoading = false;
        }
    }

    @Override
    public void brightness(int brightness) throws JposException {
        Brightness = brightness;
    }

    @Override
    public void displayMode(int displayMode) throws JposException {
        DisplayMode = displayMode;
    }

    @Override
    public void imageType(String imageType) throws JposException {
        ImageType = imageType;
    }

    @Override
    public void storage(int storage) throws JposException {
        Storage = storage;
    }

    @Override
    public void videoType(String videoType) throws JposException {
        VideoType = videoType;
    }

    @Override
    public void volume(int volume) throws JposException {
        Volume = volume;
    }

    @Override
    public void cancelURLLoading() throws JposException {

    }

    @Override
    public void stopVideo() throws JposException {

    }

    @Override
    public GoURLBack goURLBack() throws JposException {
        return null;
    }

    @Override
    public void goURLBack(GoURLBack request) throws JposException {

    }

    @Override
    public GoURLForward goURLForward() throws JposException {
        return null;
    }

    @Override
    public void goURLForward(GoURLForward request) throws JposException {

    }

    @Override
    public LoadImage loadImage(String fileName) throws JposException {
        return null;
    }

    @Override
    public void loadImage(LoadImage request) throws JposException {

    }

    @Override
    public LoadURL loadURL(String url) throws JposException {
        return null;
    }

    @Override
    public void loadURL(LoadURL request) throws JposException {

    }

    @Override
    public PlayVideo playVideo(String fileName, boolean loop) throws JposException {
        return null;
    }

    @Override
    public void playVideo(PlayVideo request) throws JposException {

    }

    @Override
    public UpdateURLPage updateURLPage() throws JposException {
        return null;
    }

    @Override
    public void updateURLPage(UpdateURLPage request) throws JposException {

    }
}
