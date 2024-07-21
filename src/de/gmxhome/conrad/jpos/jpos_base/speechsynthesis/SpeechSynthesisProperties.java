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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

import java.util.ArrayList;
import java.util.List;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.check;
import static jpos.JposConst.JPOS_E_ILLEGAL;

/**
 * Class containing the speech synthesis specific properties, their default values and default implementations of
 * SpeechSynthesisInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Speech Synthesis.
 */
@SuppressWarnings("unused")
public class SpeechSynthesisProperties extends JposCommonProperties implements SpeechSynthesisInterface {
    /**
     * UPOS property CapLanguage. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapLanguage = false;

    /**
     * UPOS property CapPitch. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPitch = false;

    /**
     * UPOS property CapSpeed. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapSpeed = false;

    /**
     * UPOS property CapVoice. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVoice = false;

    /**
     * UPOS property CapVolume. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVolume = false;

    /**
     * UPOS property Language. Default: The first language specified in LanguageList. In not overwritten until before, it
     * will be set to the first language specified in LanguageList within initOnOpen method.
     */
    public String Language = null;

    /**
     * UPOS property LanguageList. Default: null. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String LanguageList = null;

    /**
     * UPOS property OutputIDList. Default: an empty string.
     */
    public String OutputIDList = "";

    /**
     * Every access to OutputIDList must be synchronized via OutputIdListSync.
     */
    public final String[] OutputIdListSync = { OutputIDList };

    /**
     * UPOS property Pitch. Default: 100.
     */
    public int Pitch = 100;

    /**
     * UPOS property Speed. Default: 100.
     */
    public int Speed = 100;

    /**
     * UPOS property Voice. Default: null. In not overwritten until before, it
     * will be set to the first voice specified in VoiceList within initOnOpen method.
     */
    public String Voice = "";

    /**
     * UPOS property VoiceList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String VoiceList = "";

    /**
     * UPOS property Volume. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * <b>REMARK:</b> Even if the default has not been specified within the UPOS specification for property Volume, a
     * default of 50 has been specified within the table of tag values in the chapter about method Speak.
     */
    public int Volume = 50;

    /**
     * Class for parts or text to be spoken. Text specified in method Speek or SpeekImmediate will be split into parts,
     * each containing either some text to be spoken or some tag values.
     */
    public static class TextPart {
        /**
         * Textual part to be spoken or null in case of token values.
         */
        public String Text = null;
        /**
         * Value of volum token or null.
         */
        public Integer Volume = null;
        /**
         * Value of pitch token or null.
         */
        public Integer Pitch = null;
        /**
         * Value of speed token or null.
         */
        public Integer Speed = null;
        /**
         * Value of pause token or null.
         */
        public Integer Pause = null;
        /**
         * True in case of reset token, false otherwise.
         */
        public boolean Reset = false;

        private boolean validate() {
            if (Text != null)
                return Volume == null && Pitch == null && Speed == null && Pause == null && !Reset;
            if (Reset)
                return Volume == null && Pitch == null && Speed == null;
            return Volume != null || Pitch != null || Speed != null || Pause != null;
        }

        /**
         * Parses speech text and returns it in form of parts and tags.
         * @param text text to be spoken
         * @return  Parsed text in form of a list of parts containing TextPart objects.
         * @throws JposException If parse failed due to wrong or malformed tags or invalid or out-of-range tag values
         * or duplicates.
         */
        static List<TextPart>parse(String text) throws JposException{
            ArrayList<TextPart> result = new ArrayList<>();
            if (text != null) {
                int index;
                while ((index = text.indexOf('{')) >= 0) {
                    if (index > 0) {
                        TextPart part = new TextPart();
                        part.Text = text.substring(0, index);
                        result.add(part);
                        text = text.substring(index);
                    }
                    index = text.indexOf('}');
                    check(index < 1, JPOS_E_ILLEGAL, "Tag has not been closed: " + text);
                    String[] taglist = text.substring(1, index).split(",");
                    text = text.substring(index + 1);
                    TextPart part = new TextPart();
                    for (String tag : taglist) {
                        validateTag(part, tag);
                    }
                    check(!part.validate(), JPOS_E_ILLEGAL, "Invalid tag combination: " + String.join(",", taglist));
                    result.add(part);
                }
                if (text.length() > 0) {
                    TextPart part = new TextPart();
                    part.Text = text;
                    result.add(part);
                }
            }
            return result;
        }

        private static void validateTag(TextPart part, String tag) throws JposException {
            try {
                if (tag.equals("reset")) {
                    check(part.Reset, JPOS_E_ILLEGAL, "Duplicate tag: " + tag);
                    part.Reset = true;
                } else {
                    int value = Integer.parseInt(tag.substring(tag.indexOf('=') + 1));
                    if (tag.startsWith("volume=")) {
                        check(part.Volume != null, JPOS_E_ILLEGAL, "Duplicate tag: " + tag);
                        check(value < 0 || value > 100, JPOS_E_ILLEGAL, "Invalid tag value: " + tag);
                        part.Volume = value;
                    } else if (tag.startsWith("pitch=")) {
                        check(part.Pitch != null, JPOS_E_ILLEGAL, "Duplicate tag: " + tag);
                        check(value < 50 || value > 200, JPOS_E_ILLEGAL, "Invalid tag value: " + tag);
                        part.Pitch = value;
                    } else if (tag.startsWith("speed=")) {
                        check(part.Speed != null, JPOS_E_ILLEGAL, "Duplicate tag: " + tag);
                        check(value < 50 || value > 200, JPOS_E_ILLEGAL, "Invalid tag value: " + tag);
                        part.Speed = value;
                    } else if (tag.startsWith("pause=")) {
                        check(part.Pause != null, JPOS_E_ILLEGAL, "Duplicate tag: " + tag);
                        check(value < 1 || value > 50000, JPOS_E_ILLEGAL, "Invalid tag value: " + tag);
                        part.Pause = value;
                    }
                }
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Invalid tag value: " + tag);
            }
        }
    }

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected SpeechSynthesisProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        if (Voice == null)
            Voice = VoiceList.split(",")[0];
        if (Language == null)
            Language = LanguageList.split(",")[0];
    }

    @Override
    public void language(String language) throws JposException {
        Language = language;
    }

    @Override
    public void pitch(int pitch) throws JposException {
        Pitch = pitch;
    }

    @Override
    public void speed(int speed) throws JposException {
        Speed = speed;
    }

    @Override
    public void voice(String voice) throws JposException {
        Voice = voice;
    }

    @Override
    public void volume(int volume) throws JposException {
        Volume = volume;
    }

    @Override
    public Speak speak(List<TextPart> parsedText) throws JposException {
        return new Speak(this, parsedText);
    }

    @Override
    public void stopCurrentSpeaking() throws JposException {

    }

    @Override
    public void stopSpeaking(int outputID) throws JposException {

    }

    @Override
    public void speak(Speak speech) throws JposException {

    }
}
