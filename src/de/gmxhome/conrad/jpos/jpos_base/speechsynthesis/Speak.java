/*
 * Copyright 2024 Martin Conrad
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

import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jpos.SpeechSynthesisConst.SPCH_SUE_START_SPEAK;
import static jpos.SpeechSynthesisConst.SPCH_SUE_STOP_SPEAK;

/**
 * Output request executor for SpeechSynthesis methods Speak and SpeakImmediate. Instead of the un-parsed text containing
 * words to be spoken and tag / value specifications, it gets a list of TextPart objects, each containing either a text
 * part to be spoken or values representing the tag values passed at once between braces.
 */
public class Speak extends JposOutputRequest {
    /**
     * Parsed speech specifier, text parts and tag values
     */
    public final List<SpeechSynthesisProperties.TextPart> ParsedText;

    /**
     * Contents of property Language at the time this request has been created.
     */
    public final String Language;

    /**
     * Contents of property Voice at the time this request has been created.
     */
    public final String Voice;

    /**
     * Contents of property Pitch at the time this request has been created.
     */
    public final int Pitch;

    /**
     * Contents of property Speed at the time this request has been created.
     */
    public final int Speed;

    /**
     * Contents of property Volume at the time this request has been created.
     */
    public final int Volume;

    /**
     * Constructor. Stores given parameters for later use.
     * @param props       Property set of device service.
     * @param parsedText  speech parts, text and tag values.
     */
    public Speak(SpeechSynthesisProperties props, List<SpeechSynthesisProperties.TextPart> parsedText) {
        super(props);
        ParsedText = new ArrayList<>();
        ParsedText.addAll(parsedText);
        Language = props.Language;;
        Voice = props.Voice;
        Pitch = props.Pitch;
        Speed = props.Speed;;
        Volume = props.Volume;
    }

    /**
     * Calls the final part of speech synthesis. When called, a StatusUpdateEvent with status SPCH_SUE_START_SPEAK
     * will be thrown. When operation finishes, even normally, via abort or due to an exception, a StatusUpdateEvent
     * with status SPCH_SUE_STOP_SPEAK will be thrown.
     * @throws JposException In case of an error condition.
     */
    @Override
    public void invoke() throws JposException {
        Device.handleEvent(new SpeechSynthesisStatusUpdateEvent(Props.EventSource, SPCH_SUE_START_SPEAK));
        try {
            ((SpeechSynthesisService) Props.EventSource).SpeechSynthesis.speak(this);
        } finally {
            Device.handleEvent(new SpeechSynthesisStatusUpdateEvent(Props.EventSource, SPCH_SUE_STOP_SPEAK));
        }
        removeFromOutputIDList();
    }

    /**
     * Remove OutputID value of this request from the OutputIDList property.
     */
    public void removeFromOutputIDList() {
        SpeechSynthesisProperties props = (SpeechSynthesisProperties) Props;
        synchronized (props.OutputIdListSync) {
            String[] ids = props.OutputIDList.split(",");
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].equals(String.valueOf(OutputID))) {
                    String[] parts = {
                            String.join(",", Arrays.copyOf(ids, i)),
                            String.join(",", Arrays.copyOfRange(ids, i + 1, ids.length))
                    };
                    if (parts[0].length() * parts[1].length() > 0)
                        props.OutputIDList = parts[0] + "," + parts[1];
                    else
                        props.OutputIDList = parts[parts[0].length() > 0 ? 0 : 1];
                    props.EventSource.logSet("OutputIDList");
                    break;
                }
            }
        }
    }
}
