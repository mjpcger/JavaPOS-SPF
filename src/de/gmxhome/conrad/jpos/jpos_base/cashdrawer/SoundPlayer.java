/*
 * Copyright 2018 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.cashdrawer;

import javax.sound.sampled.*;
import java.awt.*;

/**
 * This class can be used to play a sound on the till if till hardware supports playing sound.
 * It can be used to generate sounds in WaitForDrawerClose methods.
 */
public class SoundPlayer implements Runnable {
    private int Frequency;
    private int Duration;
    private int Volume;
    private final String ThreadName;
    private SourceDataLine SoundData = null;
    private final AudioFormat Format = new AudioFormat(SAMPLERATE, 8, 1, true, false);
    private static final int SAMPLERATE = 48000;
    private Thread Player = null;
    private enum SoundPlayerState {OFF, OPEN, STARTED};
    SoundPlayerState State = SoundPlayerState.OFF;


    /**
     * Generates object for playing a sound.
     * @param name      Name of the locical device that uses this object.
     */
    public SoundPlayer(String name) {
        ThreadName = name + ".SoundPlayer";
    }

    /**
     * Starts the sound and plays it once in the background.
     * @param frequency Frequency of the sound to be played, in Hertz.
     * @param duration  Duration of the tone in milliseconds.
     * @param volume    Volume, a value between 0 (silent) and 127 (loud).
     */
    synchronized public void startSound(int frequency, int duration, int volume) {
        Frequency = frequency;
        Duration = duration;
        Volume = volume;
        clear();
        try {
            SoundData = AudioSystem.getSourceDataLine(Format);
            State = SoundPlayerState.OFF;
            SoundData.open(Format);
            State = SoundPlayerState.OPEN;
            (Player = new Thread(this, ThreadName)).start();
       } catch (LineUnavailableException e) {
            clear();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Stops sound if sound is playing. Otherwise nothing will happen.
     */
    synchronized public void clear() {
        if (SoundData != null) {
            if (State == SoundPlayerState.STARTED)
                SoundData.stop();
            if (State != SoundPlayerState.OFF)
                SoundData.close();
            SoundData = null;
        }
        if (Player != null)
            Player = null;
    }

    /**
     * Waits until sound has been finished.
     */
    public void waitFinished() {
        Thread player;
        synchronized(this) {
            player = Player;
        }
        if (player != null) {
            try {
                player.join();
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                SoundData.start();
                State = SoundPlayerState.STARTED;
            }
            while (Duration < 0) {
                playMilliSeconds(Long.MAX_VALUE / (SAMPLERATE / 1000));
            }
            playMilliSeconds(Duration);
            SoundData.drain();
        } catch (NullPointerException ignore) {
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            clear();
        }
    }

    private void playMilliSeconds(long millisec) {
        double factor = (2 * Math.PI * Frequency) / (SAMPLERATE);
        boolean again = true;
        for (long l = 0; l < millisec * (SAMPLERATE / 1000) || again; l++) {
            int i = (int) l % SAMPLERATE;
            byte val = (byte) (Math.sin(i * factor) * Volume);
            again = val * ((byte) (Math.sin((i + 1) * factor))) <= 0;
            SoundData.write(new byte[]{val}, 0, 1);
        }
    }
}
