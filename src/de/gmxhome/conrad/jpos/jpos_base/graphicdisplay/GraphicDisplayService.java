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
import jpos.services.GraphicDisplayService116;

/**
 * GraphicDisplay service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class GraphicDisplayService extends JposBase implements GraphicDisplayService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public GraphicDisplayService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the GraphicDisplayInterface for tone indicator specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public GraphicDisplayInterface GraphicDisplay;

    @Override
    public int getBrightness() throws JposException {
        return 0;
    }

    @Override
    public void setBrightness(int i) throws JposException {

    }

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        return null;
    }

    @Override
    public boolean getCapBrightness() throws JposException {
        return false;
    }

    @Override
    public boolean getCapImageType() throws JposException {
        return false;
    }

    @Override
    public int getCapStorage() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapURLBack() throws JposException {
        return false;
    }

    @Override
    public boolean getCapURLForward() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideoType() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        return false;
    }

    @Override
    public int getDisplayMode() throws JposException {
        return 0;
    }

    @Override
    public void setDisplayMode(int i) throws JposException {

    }

    @Override
    public String getImageType() throws JposException {
        return null;
    }

    @Override
    public void setImageType(String s) throws JposException {

    }

    @Override
    public String getImageTypeList() throws JposException {
        return null;
    }

    @Override
    public int getLoadStatus() throws JposException {
        return 0;
    }

    @Override
    public int getStorage() throws JposException {
        return 0;
    }

    @Override
    public void setStorage(int i) throws JposException {

    }

    @Override
    public int getURL() throws JposException {
        return 0;
    }

    @Override
    public String getVideoType() throws JposException {
        return null;
    }

    @Override
    public void setVideoType(String s) throws JposException {

    }

    @Override
    public String getVideoTypeList() throws JposException {
        return null;
    }

    @Override
    public int getVolume() throws JposException {
        return 0;
    }

    @Override
    public void setVolume(int i) throws JposException {

    }

    @Override
    public void cancelURLLoading() throws JposException {

    }

    @Override
    public void goURLBack() throws JposException {

    }

    @Override
    public void goURLForward() throws JposException {

    }

    @Override
    public void loadImage(String s) throws JposException {

    }

    @Override
    public void loadURL(String s) throws JposException {

    }

    @Override
    public void playVideo(String s, boolean b) throws JposException {

    }

    @Override
    public void stopVideo() throws JposException {

    }

    @Override
    public void updateURLPage() throws JposException {

    }
}
