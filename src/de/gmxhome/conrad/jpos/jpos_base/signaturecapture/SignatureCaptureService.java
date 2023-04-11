/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.signaturecapture;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.awt.*;
import java.util.*;

/**
 * SignatureCapture service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SignatureCaptureService extends JposBase implements SignatureCaptureService115 {
    /**
     * Instance of a class implementing the SignatureCaptureInterface for signature capture specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SignatureCaptureInterface SignatureCaptureInterface;

    private SignatureCaptureProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SignatureCaptureService(SignatureCaptureProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapDisplay() throws JposException {
        checkOpened();
        logGet("CapDisplay");
        return Data.CapDisplay;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        checkOpened();
        logGet("CapRealTimeData");
        return Data.CapRealTimeData;
    }

    @Override
    public boolean getCapUserTerminated() throws JposException {
        checkOpened();
        logGet("CapUserTerminated");
        return Data.CapUserTerminated;
    }

    @Override
    public int getMaximumX() throws JposException {
        checkOpened();
        logGet("MaximumX");
        return Data.MaximumX;
    }

    @Override
    public int getMaximumY() throws JposException {
        checkOpened();
        logGet("MaximumY");
        return Data.MaximumY;
    }

    @Override
    public Point[] getPointArray() throws JposException {
        checkOpened();
        logGet("PointArray");
        Point[] result = new Point[Data.PointArray.length];
        for (int i = Data.PointArray.length - 1; i >= 0; --i) {
            result[i] = (Point) Data.PointArray[i].clone();
        }
        return result;
    }

    @Override
    public byte[] getRawData() throws JposException {
        checkOpened();
        logGet("RawData");
        return Arrays.copyOf(Data.RawData, Data.RawData.length);
    }

    @Override
    public boolean getRealTimeDataEnabled() throws JposException {
        checkOpened();
        logGet("RealTimeDataEnabled");
        return Data.RealTimeDataEnabled;
    }

    @Override
    public void setRealTimeDataEnabled(boolean b) throws JposException {
        logPreSet("RealTimeDataEnabled");
        checkOpened();
        Device.check(b && !Data.CapRealTimeData, JposConst.JPOS_E_ILLEGAL, "Activating real time data not supported");
        checkNoChangedOrClaimed(Data.RealTimeDataEnabled, b);
        SignatureCaptureInterface.realTimeDataEnabled(b);
        logSet("RealTimeDataEnabled");
    }

    @Override
    public void beginCapture(String formName) throws JposException {
        if (formName == null)
            formName = "";
        logPreCall("BeginCapture", formName);
        checkEnabled();
        SignatureCaptureInterface.beginCapture(formName);
        logCall("BeginCapture");
    }

    @Override
    public void endCapture() throws JposException {
        logPreCall("EndCapture");
        checkEnabled();
        SignatureCaptureInterface.endCapture();
        logCall("EndCapture");
    }
}
