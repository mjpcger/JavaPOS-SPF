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

package de.gmxhome.conrad.jpos.jpos_base.voicerecognition;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the VoiceRecognition device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Voice Recognition.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
@SuppressWarnings("unused")
public interface VoiceRecognitionInterface extends JposBaseInterface {
    /**
     * Validation part of StartHearingFree method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>language is one of the languages specified in property LanguageList,</li>
     *     <li>Voice recognition is currently not in progress.</li>
     * </ul>
     *
     * @param language Language to be used for voice recognition.
     * @return StartHearingFree object for use in final part.
     * @throws JposException    For details, see UPOS method PrintTwoNormal.
     */
    StartHearingFree startHearingFree(String language) throws JposException;

    /**
     * Final part of StartHearingFree method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartHearingFree object. This method will be called
     * when the corresponding operation shall be performed asynchronously. All plausibility
     * checks have been made before.
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by StartHearingFree.
     * @throws JposException    For details, see UPOS method StartHearingFree.
     */
    void startHearingFree(StartHearingFree request) throws JposException;

    /**
     * Validation part of StartHearingSentence method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>language is one of the languages specified in property LanguageList,</li>
     *     <li>neither wordList not patternList is null or empty,</li>
     *     <li>all word group IDs and pattern IDs are unique,</li>
     *     <li>all specified words are not empty,</li>
     *     <li>all group IDs specified within patternList are present in wordList,</li>
     *     <li>voice recognition is currently not in progress.</li>
     * </ul>
     *
     * @param language Language to be used for voice recognition.
     * @param wordList Comma-separated list of double-point separated list of word groups. . See UPOS specification for details.
     * @param patternList Comma-separated list of sentence pattern to be waiting for. See UPOS specification for details.
     * @return StartHearingSentence object for use in final part.
     * @throws JposException    For details, see UPOS method PrintTwoNormal.
     */
    StartHearingSentence startHearingSentence(String language, String wordList, String patternList) throws JposException;

    /**
     * Final part of StartHearingSentence method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartHearingSentence object. This method will be called
     * when the corresponding operation shall be performed asynchronously. All plausibility
     * checks have been made before.
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by StartHearingSentence.
     * @throws JposException    For details, see UPOS method StartHearingSentence.
     */
    void startHearingSentence(StartHearingSentence request) throws JposException;

    /**
     * Validation part of StartHearingWord method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>language is one of the languages specified in property LanguageList,</li>
     *     <li>wordList is neither null nor empty,</li>
     *     <li>All specified words are not empty,</li>
     *     <li>Voice recognition is currently not in progress.</li>
     * </ul>
     *
     * @param language Language to be used for voice recognition.
     * @param wordList Comma-separated list of words to be waiting for.
     * @return StartHearingWord object for use in final part.
     * @throws JposException    For details, see UPOS method PrintTwoNormal.
     */
    StartHearingWord  startHearingWord (String language, String wordList) throws JposException;

    /**
     * Final part of StartHearingWord method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartHearingWord object. This method will be called
     * when the corresponding operation shall be performed asynchronously. All plausibility
     * checks have been made before.
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by StartHearingWord.
     * @throws JposException    For details, see UPOS method StartHearingWord.
     */
    void startHearingWord (StartHearingWord  request) throws JposException;

    /**
     * Validation part of StartHearingYesNo method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>language is one of the languages specified in property LanguageList,</li>
     *     <li>Voice recognition is currently not in progress.</li>
     * </ul>
     *
     * @param language Language to be used for voice recognition.
     * @return StartHearingYesNo object for use in final part.
     * @throws JposException    For details, see UPOS method PrintTwoNormal.
     */
    StartHearingYesNo startHearingYesNo(String language) throws JposException;

    /**
     * Final part of StartHearingYesNo method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartHearingYesNo object. This method will be called
     * when the corresponding operation shall be performed asynchronously. All plausibility
     * checks have been made before.
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by StartHearingYesNo.
     * @throws JposException    For details, see UPOS method StartHearingYesNo.
     */
    void startHearingYesNo(StartHearingYesNo request) throws JposException;

    void stopHearing() throws JposException;
}
