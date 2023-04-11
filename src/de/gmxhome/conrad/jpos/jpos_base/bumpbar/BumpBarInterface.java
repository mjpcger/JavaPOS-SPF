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
 */

package de.gmxhome.conrad.jpos.jpos_base.bumpbar;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the BumpBar device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Bump Bar.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface BumpBarInterface extends JposBaseInterface {
    /**
     * Final part of setting AutoToneDuration. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The unit specified by CurrentUnitID is online,</li>
     *     <li>The new property value is positive.</li>
     * </ul>
     *
     * @param duration New AutoToneDuration value
     * @throws JposException If an error occurs
     */
    public void autoToneDuration(int duration) throws JposException;

    /**
     * Final part of setting AutoToneFrequency. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The unit specified by CurrentUnitID is online,</li>
     *     <li>The new property value is positive.</li>
     * </ul>
     *
     * @param frequency New AutoToneFrequency value
     * @throws JposException If an error occurs
     */
    public void autoToneFrequency(int frequency) throws JposException;

    /**
     * Final part of setting CurrentUnitID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The new property value is one of UID_x, where 1 &le; x &le; 32.</li>
     * </ul>
     *
     * @param unit New CurrentUnitID value
     * @throws JposException If an error occurs
     */
    public void currentUnitID(int unit) throws JposException;

    /**
     * Final part of setting Timeout. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>The new property value is positive,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or milliseconds equals the previous value of Timeout.</li>
     * </ul>
     *
     * @param milliseconds New Timeout value
     * @throws JposException If an error occurs
     */
    public void timeout(int milliseconds) throws JposException;

    /**
     * Final part of logical key value assignment. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>At least one unit has been selected (units != 0),</li>
     *     <li>The device units specified by units are online,</li>
     *     <li>Both, scanCode and logicalKey, are within the range from 0 to 255.</li>
     * </ul>
     *
     * @param units      Bitwise mask indicating which bump bar unit(s) to set key translation for.
     * @param scanCode   The bump bar generated key scan code. Valid values 0-255.
     * @param logicalKey The translated logical key value. Valid values 0-255.
     * @throws JposException If an error occurs
     */
    public void setKeyTranslation(int units, int scanCode, int logicalKey) throws JposException;

    /**
     * Validation part of BumpBarSound method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>At least one unit has been selected (units != 0),</li>
     *     <li>All units specified by units are online,</li>
     *     <li>For all specified units is CapTone true,</li>
     *     <li>interSoundWait is a positive value,</li>
     *     <li>numberOfCycles is FOREVER or a positive non-zero value,</li>
     *     <li>If AsyncMode is false, numberOfCycles is not FOREVER.</li>
     * </ul>
     *
     * @param units             Bitwise mask indicating which bump bar unit(s) to operate on.
     * @param frequency         Tone frequency in Hertz.
     * @param duration          Tone duration in milliseconds.
     * @param numberOfCycles    Number of cycles the tone shall be performed.
     * @param interSoundWait    Milliseconds to wait before playing the tone again.
     * @return BumpBarSound object for use in final part.
     * @throws JposException    For details, see UPOS method BumpBarSound.
     */
    public BumpBarSound bumpBarSound(int units, int frequency, int duration, int numberOfCycles, int interSoundWait) throws JposException;

    /**
     * Final part of BumpBarSound method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a BumpBarSound object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are still online.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by BumpBarSound.
     * @throws JposException    For details, see UPOS method BumpBarSound.
     */
    public void bumpBarSound(BumpBarSound request) throws JposException;
}
