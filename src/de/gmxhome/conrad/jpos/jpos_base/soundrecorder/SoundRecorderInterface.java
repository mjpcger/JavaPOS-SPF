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

package de.gmxhome.conrad.jpos.jpos_base.soundrecorder;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the SoundRecorder device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Sound Recorder.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface SoundRecorderInterface extends JposBaseInterface {
    /**
     * Final part of setting Channel. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapChannel is true or channel equals the current value of Channel,</li>
     *     <li>channel equals one of the values specified in property ChannelList.</li>
     * </ul>
     *
     * @param channel Sound channel to be used for recording.
     * @throws JposException If an error occurs.
     */
    void channel(String channel) throws JposException;

    /**
     * Final part of setting RecordingLevel. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecordingLevel is true or recordingLevel equals the current value of RecordingLevel,</li>
     *     <li>0 &le; recordingLevel &le; 100.</li>
     * </ul>
     *
     * @param recordingLevel New recording level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void recordingLevel(int recordingLevel) throws JposException;

    /**
     * Final part of setting SamplingRate. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSamplingRate is true or samplingRate equals the current value of SamplingRate,</li>
     *     <li>samplingRate equals one of the values specified in property SamplingRateList.</li>
     * </ul>
     *
     * @param samplingRate One of the sampling rates specified in property SamplingRateList.
     * @throws JposException If an error occurs.
     */
    void samplingRate(String samplingRate) throws JposException;

    /**
     * Final part of setting SoundType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSoundType is true or soundType equals the current value of SoundType,</li>
     *     <li>soundType equals one of the values specified in property SoundTypeList.</li>
     * </ul>
     *
     * @param soundType One of the sound types specified in property SoundTypeList.
     * @throws JposException If an error occurs.
     */
    void soundType(String soundType) throws JposException;

    /**
     * Final part of setting Storage. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>storage is one of ST_HOST, ST_HARDTOTALS and ST_HOST_HARDTOTALS,</li>
     *     <li>The value in CapStorage must allow the specified target(s).</li>
     * </ul>
     *
     * @param storage Target for sound recording.
     * @throws JposException If an error occurs.
     */
    void storage(int storage) throws JposException;

    /**
     * Validation part of StartRecording method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall perform additional validation and may start sound recording. The functions to store the
     * recorded data will be buffered for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>No other sound recording is active,</li>
     *     <li>recordingTime is &gt; 0 or FOREVER.</li>
     * </ul>
     *
     *
     * @param fileName      Indicates the sound target located on host, HardTotals device or both, depending on Storage property.
     * @param overWrite     Specifies whether the sound file shall be overwritten if just present. If false, StartRecording
     *                      will fail if the specified file just exists.
     * @param recordingTime Specifies the recording time in seconds. If FOREVER, method StopRecording must be used to finish
     *                      the recording process.
     * @throws JposException    If an error occurs.
     * @return StartRecording object for use in final part.
     */
    StartRecording startRecording(String fileName, boolean overWrite, int recordingTime) throws JposException;

    /**
     * Final part of StartRecording method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartRecording object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Input request object returned by validation method that contains all parameters
     *                          to be used by StartRecording.
     * @throws JposException    If an error occurs.
     */
    public void startRecording(StartRecording request) throws JposException;

    /**
     * Final part of StopRecording method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Recording is active.</li>
     * </ul>
     * @throws JposException If an error occurs.
     */
    void stopRecording() throws JposException;
}
