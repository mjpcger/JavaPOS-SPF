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

package de.gmxhome.conrad.jpos.jpos_base.soundplayer;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the SoundPlayer device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Sound Player.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface SoundPlayerInterface extends JposBaseInterface {
    /**
     * Final part of setting Storage. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>storage is one of ST_HARDTOTALS or ST_HOST,</li>
     *     <li>if CapStorage is CST_HOST_ONLY, storage is ST_HOST,</li>
     *     <li>if CapStorage is CST_HARDTOTALS_ONLY, storage is ST_HARDTOTALS,</li>
     *     <li>if CapAssociatedHardTotalsDevice is an empty string, storage is ST_HOST.</li>
     * </ul>
     *
     * @param storage Specifies the source device for sound data.
     * @throws JposException If an error occurs.
     */
    void storage(int storage) throws JposException;

    /**
     * Final part of setting Volume. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVolume is true or volume equals Volume,</li>
     *     <li>The relationship 0 &le; volume &le; 100 is true.</li>
     * </ul>
     *
     * @param volume Volume of the sound to be played, between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void volume(int volume) throws JposException;

    /**
     * Final part of StopSound method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>outputID is one of the values specified in OutputIDList.</li>
     * </ul>
     * @param outputID The output ID of the sound to be stopped.
     * @throws JposException If an error occurs.
     */
    void stopSound(int outputID) throws JposException;

    /**
     * Validation part of PlaySound method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>action is one of STORE_DATA, STORE_PROGRAM, EXECUTE_DATA, XML_BLOCK_DATA, SECURITY_FUSE or RESET,</li>
     *     <li>count is &gt; 0.</li>
     * </ul>
     *
     * @param fileName Indicates the sound source located on host or HardTotals device, depending on Storage property.
     * @param loop     Specified whether the sound shall be played in a loop or only once.
     * @throws JposException    If an error occurs.
     * @return PlaySound object for use in final part.
     */
    PlaySound playSound(String fileName, boolean loop) throws JposException;

    /**
     * Final part of PlaySound method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PlaySound object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PlaySound.
     * @throws JposException    If an error occurs.
     */
    void playSound(PlaySound request) throws JposException;
}
