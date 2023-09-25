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

package jpos.services;

import jpos.JposException;
import jpos.loader.JposServiceInstance;

public interface VideoCaptureService116 extends BaseService, JposServiceInstance {
    public boolean getCapCompareFirmwareVersion() throws JposException;

    public int getCapPowerReporting() throws JposException;

    public boolean getCapStatisticsReporting() throws JposException;

    public boolean getCapUpdateFirmware() throws JposException;

    public boolean getCapUpdateStatistics() throws JposException;

    public int getPowerNotify() throws JposException;

    public void setPowerNotify(int var1) throws JposException;

    public int getPowerState() throws JposException;

    public void clearInput() throws JposException;

    public void compareFirmwareVersion(String var1, int[] var2) throws JposException;

    public void resetStatistics(String var1) throws JposException;

    public void retrieveStatistics(String[] var1) throws JposException;

    public void updateFirmware(String var1) throws JposException;

    public void updateStatistics(String var1) throws JposException;

    public boolean getAutoExposure() throws JposException;

    public void setAutoExposure(boolean var1) throws JposException;

    public boolean getAutoFocus() throws JposException;

    public void setAutoFocus(boolean var1) throws JposException;

    public boolean getAutoGain() throws JposException;

    public void setAutoGain(boolean var1) throws JposException;

    public boolean getAutoWhiteBalance() throws JposException;

    public void setAutoWhiteBalance(boolean var1) throws JposException;

    public int getBrightness() throws JposException;

    public void setBrightness(int var1) throws JposException;

    public String getCapAssociatedHardTotalsDevice() throws JposException;

    public boolean getCapAutoExposure() throws JposException;

    public boolean getCapAutoFocus() throws JposException;

    public boolean getCapAutoGain() throws JposException;

    public boolean getCapAutoWhiteBalance() throws JposException;

    public boolean getCapBrightness() throws JposException;

    public boolean getCapContrast() throws JposException;

    public boolean getCapExposure() throws JposException;

    public boolean getCapGain() throws JposException;

    public boolean getCapHorizontalFlip() throws JposException;

    public boolean getCapHue() throws JposException;

    public boolean getCapPhoto() throws JposException;

    public boolean getCapPhotoColorSpace() throws JposException;

    public boolean getCapPhotoFrameRate() throws JposException;

    public boolean getCapPhotoResolution() throws JposException;

    public boolean getCapPhotoType() throws JposException;

    public boolean getCapSaturation() throws JposException;

    public int getCapStorage() throws JposException;

    public boolean getCapVerticalFlip() throws JposException;

    public boolean getCapVideo() throws JposException;

    public boolean getCapVideoColorSpace() throws JposException;

    public boolean getCapVideoFrameRate() throws JposException;

    public boolean getCapVideoResolution() throws JposException;

    public boolean getCapVideoType() throws JposException;

    public int getContrast() throws JposException;

    public void setContrast(int var1) throws JposException;

    public int getExposure() throws JposException;

    public void setExposure(int var1) throws JposException;

    public int getGain() throws JposException;

    public void setGain(int var1) throws JposException;

    public boolean getHorizontalFlip() throws JposException;

    public void setHorizontalFlip(boolean var1) throws JposException;

    public int getHue() throws JposException;

    public void setHue(int var1) throws JposException;

    public String getPhotoColorSpace() throws JposException;

    public void setPhotoColorSpace(String var1) throws JposException;

    public String getPhotoColorSpaceList() throws JposException;

    public int getPhotoFrameRate() throws JposException;

    public void setPhotoFrameRate(int var1) throws JposException;

    public int getPhotoMaxFrameRate() throws JposException;

    public String getPhotoResolution() throws JposException;

    public void setPhotoResolution(String var1) throws JposException;

    public String getPhotoResolutionList() throws JposException;

    public String getPhotoType() throws JposException;

    public void setPhotoType(String var1) throws JposException;

    public String getPhotoTypeList() throws JposException;

    public int getRemainingRecordingTimeInSec() throws JposException;

    public int getSaturation() throws JposException;

    public void setSaturation(int var1) throws JposException;

    public int getStorage() throws JposException;

    public void setStorage(int var1) throws JposException;

    public boolean getVerticalFlip() throws JposException;

    public void setVerticalFlip(boolean var1) throws JposException;

    public int getVideoCaptureMode() throws JposException;

    public void setVideoCaptureMode(int var1) throws JposException;

    public String getVideoColorSpace() throws JposException;

    public void setVideoColorSpace(String var1) throws JposException;

    public String getVideoColorSpaceList() throws JposException;

    public int getVideoFrameRate() throws JposException;

    public void setVideoFrameRate(int var1) throws JposException;

    public int getVideoMaxFrameRate() throws JposException;

    public String getVideoResolution() throws JposException;

    public void setVideoResolution(String var1) throws JposException;

    public String getVideoResolutionList() throws JposException;

    public String getVideoType() throws JposException;

    public void setVideoType(String var1) throws JposException;

    public String getVideoTypeList() throws JposException;

    public void startVideo(String fileName, boolean overwrite, int recordingTime) throws JposException;

    public void stopVideo() throws JposException;

    public void takePhoto(String fileName, boolean overwrite, int recordingTime) throws JposException;
}
