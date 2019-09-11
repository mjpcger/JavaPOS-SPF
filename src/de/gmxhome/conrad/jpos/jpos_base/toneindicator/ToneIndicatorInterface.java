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

package de.gmxhome.conrad.jpos.jpos_base.toneindicator;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the ToneIndicator device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Tone Indicator.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ToneIndicatorInterface extends JposBaseInterface {
    /**
     * Final part of setting MelodyType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>type is between 0 and CapMelody.</li>
     * </ul>
     *
     * @param type New MelodyType value
     * @throws JposException If an error occurs during enable or disable
     */
    public void melodyType(int type) throws JposException;

    /**
     * Final part of setting MelodyVolume. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param volume New MelodyVolume value
     * @throws JposException If an error occurs during enable or disable
     */
    public void melodyVolume(int volume) throws JposException;

    /**
     * Final part of setting InterToneWait. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>delay is &ge; 0.</li>
     * </ul>
     *
     * @param delay New InterToneWait value
     * @throws JposException If an error occurs during enable or disable
     */
    public void interToneWait(int delay) throws JposException;

    /**
     * Final part of setting Tone1Duration. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param duration New Tone1Duration value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tone1Duration(int duration) throws JposException;

    /**
     * Final part of setting Tone1Pitch. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param type New Tone1Pitch value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tone1Pitch(int type) throws JposException;

    /**
     * Final part of setting Tone1Volume. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param type New Tone1Volume value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tone1Volume(int type) throws JposException;

    /**
     * Final part of setting Tone2Duration. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param type New Tone2Duration value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tone2Duration(int type) throws JposException;

    /**
     * Final part of setting Tone2Pitch. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param type New Tone2Pitch value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tone2Pitch(int type) throws JposException;

    /**
     * Final part of setting Tone2Volume. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param type New Tone2Volume value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tone2Volume(int type) throws JposException;

    /**
     * Validation part of Sound method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>interSoundWait &ge; 0,</li>
     *     <li>numberOfCycles &gt; 0 or JPOS_FOREVER,</li>
     *     <li>AsyncMode is false: numberOfCycles is not JPOS_FOREVER.</li>
     * </ul>
     * This method will be used in method SoundImmediate as well.
     * @param numberOfCycles The number of cycles to sound the indicator device. If FOREVER, then start the
     *                       indicator sounding and repeat continuously, else perform the sound for the specified
     *                       number of cycles.
     * @param interSoundWait When numberOfCycles is not one, then pause for interSoundWait milliseconds before
     *                       repeating the tone cycle (before playing tone-1 again).
     * @return Sound object for use in final part.
     * @throws JposException For details, see UPOS method Sound.
     */
     public Sound sound(int numberOfCycles, int interSoundWait) throws JposException;
     
    /**
     * Final part of Sound method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a Sound object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>This method will be used in method SoundImmediate as well.
     *
     * @param request Sound object. Used to perform methods synchronously or asynchronously, depending
     *                on call parameters. Holds Abort and EndSync objects that should be checked in longer operations.
     * @throws JposException See UPOS specification, method Sound
     */
    public void sound(Sound request) throws JposException;
}
