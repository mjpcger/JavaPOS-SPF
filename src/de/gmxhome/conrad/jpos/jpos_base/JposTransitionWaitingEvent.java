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

package de.gmxhome.conrad.jpos.jpos_base;


/**
 * Transition confirmation event. Contains synchronization object that allows the service to wait until the event
 * callback method of the application has been finished. This is necessary whenever the callback may change pData or
 * pString.
 */
public class JposTransitionWaitingEvent extends JposTransitionEvent {
    /**
     * Object to be used to wait until event handler returns.
     */
    private final SyncObject Waiter = new SyncObject();

    /**
     * Synchronization object, will be signalled after the event callback has been finished.
     * @return The SyncObject used for synchronization.
     */
    public SyncObject getWaiter() {
        return Waiter;
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source      Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param eventNumber The ID number of the asynchronous I/O device process condition
     * @param pData       Additional information about appropriate response which is dependent upon the specific
     *                    process condition.
     * @param pString     Information about the specific event that has occurred.
     */
    public JposTransitionWaitingEvent(JposBase source, int eventNumber, int pData, String pString) {
        super(source, eventNumber, pData, pString);
    }
}
