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

package de.gmxhome.conrad.jpos.jpos_base;

/**
 * Common class for threads that shall stop after setting a flag.
 */
@SuppressWarnings("unused")
public class ThreadHandler extends Thread {
    /**
     * Flag to be set to force thread termination.
     */
    public boolean ToBeFinished = false;

    /**
     * Constructor. Set thread name and Runnable for thread main method
     * @param name  Thread name.
     * @param runner The Runnable that implements the run() method to be called after the thread has been started.
     */
    public ThreadHandler(String name, Runnable runner) {
        super(runner);
        setName(name);
    }

    /**
     * Sets ToBeFinished ad waits until the thread stops execution. The run() method implemening the Runnable interface
     * passed to the constructor shall check ToBeFinished periodically to verify whether it shall continue processing
     * or stop execution as soon as possible.
     */
    public void waitFinished() {
        ToBeFinished = true;
        while (ToBeFinished) {
            try {
                join();
                ToBeFinished = false;
            } catch (InterruptedException ignored) {}
        }
    }
}
