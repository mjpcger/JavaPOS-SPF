/*
 * Copyright 2019 Martin Conrad
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
 */

package de.gmxhome.conrad.jpos.jpos_base;

import de.gmxhome.conrad.jpos.jpos_base.bumpbar.BumpBarService;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.CheckScannerService;
import de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.GestureControlService;
import de.gmxhome.conrad.jpos.jpos_base.graphicdisplay.GraphicDisplayService;
import de.gmxhome.conrad.jpos.jpos_base.imagescanner.ImageScannerService;
import de.gmxhome.conrad.jpos.jpos_base.micr.MICRService;
import de.gmxhome.conrad.jpos.jpos_base.msr.MSRService;
import de.gmxhome.conrad.jpos.jpos_base.pinpad.PINPadService;
import de.gmxhome.conrad.jpos.jpos_base.poskeyboard.POSKeyboardService;
import de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay.RemoteOrderDisplayService;
import de.gmxhome.conrad.jpos.jpos_base.scanner.ScannerService;
import de.gmxhome.conrad.jpos.jpos_base.signaturecapture.SignatureCaptureService;
import de.gmxhome.conrad.jpos.jpos_base.soundplayer.SoundPlayerService;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.SpeechSynthesisService;
import de.gmxhome.conrad.jpos.jpos_base.videocapture.VideoCaptureService;
import jpos.*;
import jpos.events.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.*;
import static jpos.JposConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * Error event.
 * The default implementation for event handling does not handle instances of ErrorEvent, it handles only
 * instances of JposErrorEvent instead.
 */
public class JposErrorEvent extends ErrorEvent {
    /**
     * Contains the error message stored in a JposException passed to method createErrorEvent of a JposOutputRequest.
     * Contains an empty string as default otherwise.
     */
    public final String Message;

    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param locus ErrorLocus, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     */
    public JposErrorEvent(JposBase source, int errorcode, int extended, int locus) {
        super(source, errorcode, extended, locus, locus == JPOS_EL_OUTPUT ? JPOS_ER_RETRY : (locus == JPOS_EL_INPUT ? JPOS_ER_CLEAR : JPOS_ER_CONTINUEINPUT));
        Message = "";
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param locus ErrorLocus, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param message Error message from exception.
     */
    public JposErrorEvent(JposBase source, int errorcode, int extended, int locus, String message) {
        super(source, errorcode, extended, locus, locus == JPOS_EL_OUTPUT ? JPOS_ER_RETRY : (locus == JPOS_EL_INPUT ? JPOS_ER_CLEAR : JPOS_ER_CONTINUEINPUT));
        Message = message;
    }

    /**
     * Sets the error properties stored within the JposErrorEvent into
     * the JposCommonProperties object stored in Source. The corresponding properties must be
     * defined within derived classes.
     */
    public void setErrorProperties() {
    }
    /**
     * Generates string describing the error event for logging purposes.
     * @return Describing string.
     */
    public String toLogString() {
        return Integer.toString(getErrorCode()) + "/" + getErrorCodeExtended() + "/" + getErrorLocus() + (Message.equals("") ? "" : "/" + Message);
    }

    /**
     * Returns property set bound to the event source.
     * @return Property set.
     */
    public JposCommonProperties getPropertySet() {
        return ((JposBase) getSource()).Props;
    }

    /**
     * Returns an input data event corresponding to the given event. Will be created automatically within input event
     * handler.
     * @return JposErrorEvent with locus input data.
     */
    public JposErrorEvent getInputDataErrorEvent() {
        return new JposErrorEvent((JposBase) getSource(), getErrorCode(), getErrorCodeExtended(), JPOS_EL_INPUT_DATA);
    }

    /**
     * Clears all input data or buffered output requests, error and output complete events that belong to this error
     * event. Neither ClearInput nor ClearOutput should throw an exception because both operations should be implemented
     * as internal operations that do not need to throw an exception. However, if an implementation should throw a
     * JposException, it will be caught and ignored.
     */
    public void clear() {
        try {
            if (getErrorLocus() == JPOS_EL_OUTPUT) {
                ((JposBase) getSource()).DeviceInterface.clearOutput();
            } else {
                ((JposBase) getSource()).DeviceInterface.clearInput();
            }
        } catch (JposException e) {
            e.printStackTrace();
        }
    }

    /**
     * Array or valid error response values.
     */
    private final long[] validErrorResponses = { JPOS_ER_CLEAR, JPOS_ER_RETRY, JPOS_ER_CONTINUEINPUT };

    /**
     * Array of all classes which do not allow ER_CONTINUEINPUT as ErrorResponse.
     */
    private final Class<?>[] invalidContinueClasses = {
            BumpBarService.class, CheckScannerService.class, ImageScannerService.class, MICRService.class,
            MSRService.class, PINPadService.class, POSKeyboardService.class, RemoteOrderDisplayService.class,
            ScannerService.class, SignatureCaptureService.class, VideoCaptureService.class, SoundPlayerService.class,
            SpeechSynthesisService.class, GestureControlService.class, GraphicDisplayService.class
    };

    /**
     * Set ErrorResponse property of the ErrorEvent instance. Trying to set an invalid value is not allowed and the
     * previous value remains unchanged.
     * <br>The following values are valid:
     * <ul>
     *     <li><b>ER_CLEAR</b> is always allowed.</li>
     *     <li><b>ER_RETRY</b> is normally allowed if <i>ErrorLocus</i> is <b>EL_OUTPUT</b>.</li>
     *     <li>In addition, <b>ER_RETRY</b> is normally allowed if <i>ErrorLocus</i> is <b>EL_INPUT</b> and the delivering
     *     control is <b>not</b> one of <i>BumpBar, CheckScanner, ImageScanner, MICR, MSR, PINPad, POSKeyboard,
     *     RemoteOrderDisplay, Scanner or SignatureCapture.</i>
     *     <i><b>ER_CONTINUEINPUT</b></i> is only allowed if <i>ErrorLocus</i> is <b>EL_INPUT_DATA</b>.</li>
     * </ul>
     * All other values are not allowed.
     * <br>Some device services can decide to generate ErrorEvent objects derived from <i>JposErrorEvent</i> where
     * <b>ER_RETRY</b> is not allowed, for example because the device does not allow retries.
     * <br>UPOS does not specify what happens
     *
     * @param resp New value for ErrorResponse.
     */
    @Override
    public void setErrorResponse(int resp) {
        JposBase srv = ((JposBase)getSource());
        boolean valid = true;
        if (!member(resp, validErrorResponses)) {
            srv.Device.log(INFO, srv.Props.LogicalName + ": setErrorResponse: Value must be one of"
                    + " ER_CLEAR, ER_RETRY or ER_CONTINUEINPUT. " + resp + " has been ignored");
            valid = false;
        } else if (resp == JPOS_ER_CONTINUEINPUT && getErrorLocus() != JPOS_EL_INPUT_DATA) {
            srv.Device.log(INFO, srv.Props.LogicalName + ": setErrorResponse: CONTINUEINPUT in"
                    + " ErrorEvent where error locus is not INPUT_DATA has been ignored.");
            valid = false;
        } else if (resp == JPOS_ER_RETRY && getErrorLocus() == JPOS_EL_INPUT) {
            for (Class<?> service : invalidContinueClasses) {
                if (service.isInstance(srv)) {
                    srv.Device.log(INFO, srv.Props.LogicalName + ": setErrorResponse: Retry in"
                            + " ErrorEvent with error locus INPUT in device class "
                            + service.getSimpleName().substring(0, service.getSimpleName().indexOf("Service"))
                            + " has been ignored.");
                    valid = false;
                    break;
                }
            }
        }
        if (!valid) {
            // Invalid response: recommended handling is default handling:
            switch (getErrorLocus()) {
                case JPOS_EL_INPUT:
                    resp = JPOS_ER_CLEAR;
                    break;
                case JPOS_EL_INPUT_DATA:
                    resp = JPOS_ER_CONTINUEINPUT;
                    break;
                case JPOS_EL_OUTPUT:
                    resp = JPOS_ER_RETRY;
            }
        }
        super.setErrorResponse(resp);
    }
}
