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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

import java.awt.*;

/**
 * Class containing the signature capture specific properties, their default values and default implementations of
 * SignatureCaptureInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Signature Capture.
 */
public class SignatureCaptureProperties extends JposCommonProperties implements SignatureCaptureInterface {
    /**
     * UPOS property CapDisplay. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapDisplay = false;

    /**
     * UPOS property CapRealTimeData. Default: false. Can be overwritten by objects derived from JposDevice within
     * the changeDefaults method.
     */
    public boolean CapRealTimeData = false;

    /**
     * UPOS property CapUserTerminated. Default: false. Can be overwritten by objects derived from JposDevice within
     * the changeDefaults method.
     */
    public boolean CapUserTerminated = false;

    /**
     * UPOS property MaximumX. Default: 0. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int MaximumX = 0;

    /**
     * UPOS property MaximumY. Default: 0. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int MaximumY = 0;

    /**
     * UPOS property PointArray. Default: null (no data). Must be overwritten
     * before delivering a data event.
     */
    public Point[] PointArray = null;

    /**
     * UPOS property RawData. Default: null (no data). Must be overwritten
     * before delivering a data event.
     */
    public byte[] RawData = null;

    /**
     * UPOS property RealTimeDataEnabled. Default: false. Can be overwritten by objects derived from JposDevice
     * within the changeDefaults method.
     */
    public boolean RealTimeDataEnabled = false;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected SignatureCaptureProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
        FlagWhenIdleStatusValue = -1;   // To avoid FlagWhenIdle handling for CASH_SUE_DRAWERCLOSED
    }

    @Override
    public void realTimeDataEnabled(boolean b) throws JposException {
        RealTimeDataEnabled = b;
    }

    @Override
    public void beginCapture(String formName) throws JposException {
    }

    @Override
    public void endCapture() throws JposException {
    }
}
