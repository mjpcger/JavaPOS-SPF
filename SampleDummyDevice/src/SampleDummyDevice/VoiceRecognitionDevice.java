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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.voicerecognition.*;
import jpos.JposException;
import jpos.config.JposEntry;

import javax.swing.*;

import java.util.HashMap;
import java.util.Map;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static jpos.JposConst.JPOS_E_NOSERVICE;
import static jpos.JposConst.JPOS_PR_NONE;
import static jpos.VoiceRecognitionConst.*;

/**
 * JposDevice based dummy implementation for JavaPOS VoiceRecognition device service implementation.
 * No real hardware. All read data with dummy values, operator interaction via InputDialog boxes.<br>
 * The following device specific settings can be configured via jpos.xml:
 * <ul>
 *     <li>LanguageList: Value of LanguageList property. Default: en-US. Can be set to another value if other languages
 *     shall be supported.</li>
 *     <li>Yes.<i>language</i>: For each language specified in LanguageList, a comma separated list of words meaning yes.</li>
 *     <li>No.<i>language</i>: For each language specified in LanguageList, a comma separated list of words meaning No.</li>
 * </ul>
 * <b>SPECIAL REMARKS:</b> This sample does not implement any really existing VoiceRecognition system and shall not be
 * used in any really existing cash register application.
 */
public class VoiceRecognitionDevice extends JposDevice {
    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, not used by implementation.
     */
    protected VoiceRecognitionDevice(String id) {
        super(id);
        voiceRecognitionInit(1);
        PhysicalDeviceDescription = "Dummy VoiceRecognition simulator";
        PhysicalDeviceName = "Dummy VoiceRecognition Simulator";
        CapPowerReporting = JPOS_PR_NONE;
    }

    @Override
    public VoiceRecognitionProperties getVoiceRecognitionProperties(int index) {
        return new MyProperties();
    }

    @Override
    public void changeDefaults(VoiceRecognitionProperties p) {
        MyProperties props = (MyProperties) p;
        super.changeDefaults(p);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Voice Recognition service for sample dummy device";
        props.CapLanguage = props.LanguageList.split(",").length > 1;
    }

    private class MyProperties extends VoiceRecognitionProperties {
        protected MyProperties() {
            super(0);
        }

        private final Map<String,String[]> YesWords = new HashMap<>();
        private final Map<String,String[]> NoWords = new HashMap<>();

        @Override
        public void checkProperties(JposEntry entries) throws JposException {
            super.checkProperties(entries);
            Object o = entries.getPropertyValue("LanguageList");
            if (o != null) {
                String[] languages = o.toString().split(",");
                for (String language : languages) {
                    String[] languageCountry = language.split("-", 2);
                    boolean ok = languageCountry[0].matches("[a-z]{2,3}") && (languageCountry[1].matches("[A-Z\\-]{2,3}") || languageCountry[1].matches("[0-9]{3}"));
                    check(!ok, JPOS_E_NOSERVICE, "Invalid LanguageList: " + o);
                    checkAndStoreYesOrNoWordList(language, entries, YesWords, "Yes");
                    checkAndStoreYesOrNoWordList(language, entries, NoWords, "No");
                    checkSameWords(language);
                }
                LanguageList = o.toString();
            } else {
                this.LanguageList = "en_US";
                YesWords.put(LanguageList, new String[]{"yes", "ok"});
                NoWords.put(LanguageList, new String[]{"no"});
            }
        }

        private void checkSameWords(String language) throws JposException {
            StringBuilder unspecific = new StringBuilder();
            String[] yes = YesWords.get(language), no = NoWords.get(language);
            for (String word : yes) {
                for (String current : no) {
                    if (word.equalsIgnoreCase(current)) {
                        unspecific.append(',').append(word.toUpperCase());
                        break;
                    }
                }
            }
            if (unspecific.length() > 1)
                throw new JposException(JPOS_E_NOSERVICE, "Unspecific meaning for following words: " + unspecific.substring(1));
        }

        private void checkAndStoreYesOrNoWordList(String language, JposEntry entries, Map<String, String[]> wordList, String what) throws JposException {
            Object o = entries.getPropertyValue(what + "." + language);
            check(o == null, JPOS_E_NOSERVICE, "Missing " + what + " word list for language " + language);
            String[] words = o.toString().split(",");
            for (int i = 0; i < words.length; i++) {
                check(words[i].length() == 0 || !words[i].matches("\\p{IsAlphabetic}*"), JPOS_E_NOSERVICE, "Invalid word: " + words[i].toUpperCase());
                for (int j = 0; j < i; j++)
                    check(words[j].equalsIgnoreCase(words[i]), JPOS_E_NOSERVICE, "Duplicate word: " + words[j].toUpperCase());
            }
            wordList.put(language, words);
        }

        private ThreadHandler Handler;
        private final SynchronizedMessageBox Box = new SynchronizedMessageBox();
        private StartHearingFree Request = null;

        @Override
        public void claim(int timeout) throws JposException {
            super.claim(timeout);
            synchronized (Box) {
                (Handler = new ThreadHandler("VoiceRecognitionHandler", () -> {
                    while (!Handler.ToBeFinished)
                        doVoiceRecognition();
                })).start();
            }
        }

        @Override
        public void release() throws JposException {
            synchronized (Box) {
                Handler.ToBeFinished = true;
            }
            Box.abortDialog();
            super.release();
        }

        @Override
        public void clearInput() throws JposException {
            super.clearInput();
            Request = null;
            Box.abortDialog();
        }

        @Override
        public void startHearingFree(StartHearingFree request) throws JposException {
            StartRecognition(request);
        }

        @Override
        public void startHearingWord(StartHearingWord request) throws JposException {
            StartRecognition(request);
        }

        @Override
        public void startHearingYesNo(StartHearingYesNo request) throws JposException {
            StartRecognition(request);
        }

        @Override
        public void startHearingSentence(StartHearingSentence request) throws JposException {
            StartRecognition(request);
        }

        @Override
        public void stopHearing() throws JposException {
            Request.abortCommand();
            Request = null;
            Box.abortDialog();
        }

        private void StartRecognition(StartHearingFree request) {
            Request = request;
            Box.abortDialog();
            request.Waiting.suspend(INFINITE);
        }

        private void doVoiceRecognition() {
            try {
                if (Request == null) {
                    getInput("Voice Recognition Inactive");
                } else if (Request instanceof StartHearingSentence hearSentence) {
                    handleHearingSentence(hearSentence);
                } else if (Request instanceof StartHearingWord hearWord) {
                    handleHearingWord(hearWord);
                } else if (Request instanceof StartHearingYesNo hearYesNo) {
                    handleHearingYesNo(hearYesNo);
                } else {
                    handleHearingFree();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private String getInput(String message) {
            return Box.synchronizedInputBox(message, "VoiceRecognition Sample", null, "", JOptionPane.INFORMATION_MESSAGE, 0);
        }

        private void handleHearingSentence(StartHearingSentence hearSentence) throws JposException {
            String[] phrases = hearSentence.PatternList.split(",");
            String[][] thePhrases = new String[phrases.length][];
            HashMap<String, String[]> wordlist = new HashMap<>();
            String text = fillThePhrasesWordListReturnMessage(hearSentence, phrases, thePhrases, wordlist);
            String result = getInput(text);
            if (result != null && Request == hearSentence) {
                String[] sentence = result.split(" ");
                for (String[] phraseDefinition : thePhrases) {
                    if (matchedPhrase(wordlist, sentence, phraseDefinition))
                        break;
                }
            }
        }

        private boolean matchedPhrase(HashMap<String, String[]> wordlist, String[] sentence, String[] phraseDefinition) throws JposException {
            int wordIndex = 0;
            StringBuilder hearing = new StringBuilder();
            boolean match = true;
            String phrase = phraseDefinition[1];
            while (match && wordIndex < sentence.length) {
                int wordEnd;
                if (phrase.charAt(0) == '[') {
                    wordEnd = phrase.indexOf(']');
                    match = wordMatch(wordlist, sentence[wordIndex], hearing, phrase.substring(1, wordEnd++));
                } else {
                    wordEnd = phrase.indexOf(' ');
                    match = (wordEnd < 0 ? phrase : phrase.substring(0, wordEnd)).equalsIgnoreCase(sentence[wordIndex]);
                }
                phrase = skipToNextWord(match, phrase, wordEnd);
                wordIndex++;
            }
            return fireDataEventOnSuccess(sentence, wordIndex, match, phraseDefinition[0], hearing.substring(hearing.length() > 0 ? 1 : 0));
        }

        private boolean wordMatch(HashMap<String, String[]> wordlist, String word, StringBuilder hearing, String id) {
            String[] wordsToCheck = wordlist.get(id);
            for (String current : wordsToCheck) {
                if (current.equalsIgnoreCase(word)) {
                    hearing.append(',').append(id).append(':').append(current.toUpperCase());
                    return true;
                }
            }
            return false;
        }

        private String skipToNextWord(boolean match, String phrase, int wordEnd) {
            if (match && wordEnd >= 0) {
                while (match && wordEnd < phrase.length() && phrase.charAt(wordEnd) == ' ')
                    ++wordEnd;
                return phrase.substring(wordEnd);
            }
            return "";
        }

        private boolean fireDataEventOnSuccess(String[] sentence, int wordIndex, boolean match, String pattern, String words) throws JposException {
            if (match && wordIndex == sentence.length) {
                handleEvent(new VoiceRecognitionDataEvent(EventSource, 0, VRCG_HRESULT_SENTENCE, words, pattern));
                return true;
            }
            return false;
        }

        private String fillThePhrasesWordListReturnMessage(StartHearingSentence hearSentence, String[] phrases, String[][] thePhrases, HashMap<String, String[]> wordlist) {
            for (String wordID : hearSentence.WordList.split(",")) {
                int idEnd = wordID.indexOf(':');
                wordlist.put(wordID.substring(0, idEnd), wordID.substring(idEnd + 1).split(":"));
            }
            StringBuilder message;
            if (phrases.length == 1) {
                thePhrases[0] = phrases[0].split(":");
                message = new StringBuilder("Enter a response matching the following phrase:\n" + thePhrases[0][1] + "\n");
            } else {
                message = new StringBuilder("Enter a response matching one of the following phrases:\n");
                for (int i = 0; i < phrases.length; i++) {
                    thePhrases[i] = phrases[i].split(":");
                    message.append(thePhrases[i][1]).append("\n");
                }
            }
            message.append("Please answer using language ").append(hearSentence.Language);
            String text = message.toString();
            for (Map.Entry<String,String[]> entry : wordlist.entrySet()) {
                String var = "[" + entry.getKey() + "]";
                String replacement = "[" + String.join("|", entry.getValue()) + "]";
                text = text.replace(var, replacement);
            }
            return text;
        }

        private void handleHearingWord(StartHearingWord hearWord) throws JposException {
            String input = getInput("Give me at least one of the following words:\n" + hearWord.WordList);
            String[] matches = hearWord.WordList.split(",");
            if (input != null && Request == hearWord) {
                String[] words = input.split(" ");
                for (String word : words) {
                    for (String match : matches) {
                        if (word.equalsIgnoreCase(match)) {
                            handleEvent(new VoiceRecognitionDataEvent(EventSource, 0, VRCG_HRESULT_WORD, word.toUpperCase()));
                            break;
                        }
                    }
                }
            }
        }

        private void handleHearingYesNo(StartHearingYesNo hearYesNo) throws JposException {
            String input = getInput("Give me yes or no, use language " + Request.Language + ".");
            if (Request == hearYesNo) {
                if (input == null)
                    handleEvent(new VoiceRecognitionDataEvent(EventSource, 0, VRCG_HRESULT_YESNO_CANCEL, "CANCEL"));
                else {
                    String[] words = input.split(" ");
                    for (String word : words) {
                        checkForYesOrNo(word, YesWords.get(Request.Language),VRCG_HRESULT_YESNO_YES);
                        checkForYesOrNo(word, NoWords.get(Request.Language),VRCG_HRESULT_YESNO_NO);
                    }
                }
            }
        }

        private void checkForYesOrNo(String word, String[] yesOrNo, int result) throws JposException {
            for (String current : yesOrNo) {
                if (word.equalsIgnoreCase(current)) {
                    handleEvent(new VoiceRecognitionDataEvent(EventSource, 0, result, word.toUpperCase()));
                    break;
                }
            }
        }

        private void handleHearingFree() throws JposException {
            StartHearingFree current = Request;
            String input = getInput("Give me some words");
            if (input != null && Request == current && input.length() > 0) {
                String[] words = input.split(" ");
                StringBuilder recognized = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0 && word.matches("\\p{IsAlphabetic}*"))
                        recognized.append(" ").append(word.toUpperCase());
                }
                if (recognized.length() > 1)
                    handleEvent(new VoiceRecognitionDataEvent(EventSource, 0, VRCG_HRESULT_FREE, recognized.substring(1)));
            }
        }
    }
}
