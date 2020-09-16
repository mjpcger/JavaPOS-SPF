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

package de.gmxhome.conrad.jpos.jpos_base;


import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Class used for synchronisation purposes.
 */
public class SyncObject {
    /**
     * Constant for infinite wait
     */
    public static final long INFINITE = -1;

    /**
     * Object used for synchronisation
     */
    private Semaphore TheSemaphore;

    /**
     * Constructor, initializes Semaphore used for synchronization.
     */
    public SyncObject() {
        TheSemaphore = new Semaphore(0, true);
    }

    /**
     * Suspends the current thread for a specified time or until signalled.
     * Waits until timeout (in milliseconds, -1 = for ever) occurs or signalled by other thread
     * @param milliseconds Timeout (-1: without timeout).
     * @return true: SyncObject has been signalled, false otherwise.
     */
    public boolean suspend(long milliseconds) {
        boolean ret = true;
        if (milliseconds == INFINITE) {
            TheSemaphore.acquireUninterruptibly();
        }
        else {
            long starttime = System.currentTimeMillis();
            ret = false;
            for (long currenttime = starttime; currenttime - starttime <= milliseconds; currenttime = System.currentTimeMillis()) {
                try {
                    if (ret = TheSemaphore.tryAcquire(milliseconds - (currenttime - starttime), TimeUnit.MILLISECONDS))
                        break;
                } catch (Exception e) {}
            }
        }
        return ret;
    }

    /**
     * Wake up other thread, if suspended.
     */
    public void signal() {
        TheSemaphore.release();
    }
}
