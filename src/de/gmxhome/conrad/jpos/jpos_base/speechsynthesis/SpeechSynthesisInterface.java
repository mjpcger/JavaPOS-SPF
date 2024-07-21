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

package de.gmxhome.conrad.jpos.jpos_base.speechsynthesis;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

import java.util.List;

/**
 * Interface for methods that implement property setter and method calls for the SpeechSynthesis device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Speech Synthesis.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface SpeechSynthesisInterface extends JposBaseInterface {
    /**
     * Final part of setting Language. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapLanguage is true or language equals the current value of Language,</li>
     *     <li>Language is one of the language codes in LanguageList.</li>
     * </ul>
     *
     * @param language Speech volume.
     * @throws JposException If an error occurs.
     */
    void language(String language) throws JposException;

    /**
     * Final part of setting Pitch. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPitch is true or pitch equals the current value of Pitch,</li>
     *     <li>pitch is between 50 and 200.</li>
     * </ul>
     *
     * @param pitch Percentage value for pitch at speech.
     * @throws JposException If an error occurs.
     */
    void pitch(int pitch) throws JposException;

    /**
     * Final part of setting Speed. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSpeed is true or speed equals the current value of Speed,</li>
     *     <li>speed is between 50 and 200.</li>
     * </ul>
     *
     * @param speed Percentage value for speed.
     * @throws JposException If an error occurs.
     */
    void speed(int speed) throws JposException;

    /**
     * Final part of setting Voice. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVoice is true or voice equals the current value of Voice,</li>
     *     <li>voice is one of the values listed in VoiceList.</li>
     * </ul>
     *
     * @param voice Voice tone indicator.
     * @throws JposException If an error occurs.
     */
    void voice(String voice) throws JposException;

    /**
     * Final part of setting Volume. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVolume is true or volume equals the current value of Volume,</li>
     *     <li>volume is between zero and 100.</li>
     * </ul>
     *
     * @param volume Speech volume.
     * @throws JposException If an error occurs.
     */
    void volume(int volume) throws JposException;


    /**
     * Validation part of Speak method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>parsedText is formally valid (no unknown or bad formatted tags, no duplicates, ...</li>
     * </ul>
     *
     * @param parsedText List of TextPart objects describing what and how some text shall be spoken.
     * @throws JposException    If an error occurs.
     * @return Speak object for use in final part.
     */
    Speak speak(List<SpeechSynthesisProperties.TextPart> parsedText) throws JposException;

    /**
     * Final part of Speak method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a Speak object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by Speak.
     * @throws JposException    If an error occurs.
     */
    void speak(Speak request) throws JposException;


    /**
     * Final part of StopCurrentSpeaking method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is not S_IDLE.</li>
     * </ul>
     * @throws JposException If an error occurs.
     */
    void stopCurrentSpeaking() throws JposException;


    /**
     * Final part of StopSpeaking method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>outputID is one of the values specified in OutputIDList.</li>
     * </ul>
     * @param outputID The output ID of the sound to be stopped.
     * @throws JposException If an error occurs.
     */
    void stopSpeaking(int outputID) throws JposException;
}
