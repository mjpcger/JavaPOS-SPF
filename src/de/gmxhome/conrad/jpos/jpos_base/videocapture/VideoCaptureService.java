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

package de.gmxhome.conrad.jpos.jpos_base.videocapture;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.VideoCaptureService116;

/**
 * VideoCapture service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class VideoCaptureService extends JposBase implements VideoCaptureService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public VideoCaptureService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the VideoCaptureInterface for video capture specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public VideoCaptureInterface VideoCapture;

    @Override
    public boolean getAutoExposure() throws JposException {
        return false;
    }

    @Override
    public void setAutoExposure(boolean b) throws JposException {

    }

    @Override
    public boolean getAutoFocus() throws JposException {
        return false;
    }

    @Override
    public void setAutoFocus(boolean b) throws JposException {

    }

    @Override
    public boolean getAutoGain() throws JposException {
        return false;
    }

    @Override
    public void setAutoGain(boolean b) throws JposException {

    }

    @Override
    public boolean getAutoWhiteBalance() throws JposException {
        return false;
    }

    @Override
    public void setAutoWhiteBalance(boolean b) throws JposException {

    }

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
    public boolean getCapAutoExposure() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAutoFocus() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAutoGain() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAutoWhiteBalance() throws JposException {
        return false;
    }

    @Override
    public boolean getCapBrightness() throws JposException {
        return false;
    }

    @Override
    public boolean getCapContrast() throws JposException {
        return false;
    }

    @Override
    public boolean getCapExposure() throws JposException {
        return false;
    }

    @Override
    public boolean getCapGain() throws JposException {
        return false;
    }

    @Override
    public boolean getCapHorizontalFlip() throws JposException {
        return false;
    }

    @Override
    public boolean getCapHue() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPhoto() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPhotoColorSpace() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPhotoFrameRate() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPhotoResolution() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPhotoType() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSaturation() throws JposException {
        return false;
    }

    @Override
    public int getCapStorage() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapVerticalFlip() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideo() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideoColorSpace() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideoFrameRate() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideoResolution() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideoType() throws JposException {
        return false;
    }

    @Override
    public int getContrast() throws JposException {
        return 0;
    }

    @Override
    public void setContrast(int i) throws JposException {

    }

    @Override
    public int getExposure() throws JposException {
        return 0;
    }

    @Override
    public void setExposure(int i) throws JposException {

    }

    @Override
    public int getGain() throws JposException {
        return 0;
    }

    @Override
    public void setGain(int i) throws JposException {

    }

    @Override
    public boolean getHorizontalFlip() throws JposException {
        return false;
    }

    @Override
    public void setHorizontalFlip(boolean b) throws JposException {

    }

    @Override
    public int getHue() throws JposException {
        return 0;
    }

    @Override
    public void setHue(int i) throws JposException {

    }

    @Override
    public String getPhotoColorSpace() throws JposException {
        return null;
    }

    @Override
    public void setPhotoColorSpace(String s) throws JposException {

    }

    @Override
    public String getPhotoColorSpaceList() throws JposException {
        return null;
    }

    @Override
    public int getPhotoFrameRate() throws JposException {
        return 0;
    }

    @Override
    public void setPhotoFrameRate(int i) throws JposException {

    }

    @Override
    public int getPhotoMaxFrameRate() throws JposException {
        return 0;
    }

    @Override
    public String getPhotoResolution() throws JposException {
        return null;
    }

    @Override
    public void setPhotoResolution(String s) throws JposException {

    }

    @Override
    public String getPhotoResolutionList() throws JposException {
        return null;
    }

    @Override
    public String getPhotoType() throws JposException {
        return null;
    }

    @Override
    public void setPhotoType(String s) throws JposException {

    }

    @Override
    public String getPhotoTypeList() throws JposException {
        return null;
    }

    @Override
    public int getRemainingRecordingTimeInSec() throws JposException {
        return 0;
    }

    @Override
    public int getSaturation() throws JposException {
        return 0;
    }

    @Override
    public void setSaturation(int i) throws JposException {

    }

    @Override
    public int getStorage() throws JposException {
        return 0;
    }

    @Override
    public void setStorage(int i) throws JposException {

    }

    @Override
    public boolean getVerticalFlip() throws JposException {
        return false;
    }

    @Override
    public void setVerticalFlip(boolean b) throws JposException {

    }

    @Override
    public int getVideoCaptureMode() throws JposException {
        return 0;
    }

    @Override
    public void setVideoCaptureMode(int i) throws JposException {

    }

    @Override
    public String getVideoColorSpace() throws JposException {
        return null;
    }

    @Override
    public void setVideoColorSpace(String s) throws JposException {

    }

    @Override
    public String getVideoColorSpaceList() throws JposException {
        return null;
    }

    @Override
    public int getVideoFrameRate() throws JposException {
        return 0;
    }

    @Override
    public void setVideoFrameRate(int i) throws JposException {

    }

    @Override
    public int getVideoMaxFrameRate() throws JposException {
        return 0;
    }

    @Override
    public String getVideoResolution() throws JposException {
        return null;
    }

    @Override
    public void setVideoResolution(String s) throws JposException {

    }

    @Override
    public String getVideoResolutionList() throws JposException {
        return null;
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
    public void startVideo(String s, boolean b, int i) throws JposException {

    }

    @Override
    public void stopVideo() throws JposException {

    }

    @Override
    public void takePhoto(String s, boolean b, int i) throws JposException {

    }
}
