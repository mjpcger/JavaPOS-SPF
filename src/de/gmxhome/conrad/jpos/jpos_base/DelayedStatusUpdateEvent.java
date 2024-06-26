/*
 * Copyright 2020 Martin Conrad
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

import static net.bplaced.conrad.log4jpos.Level.*;


/**
 * This class provides additional methods that allow firing StatusUpdateEvents with a specific delay. In this
 * implementation, only one event can be buffered for delayed firing per enabled device. Subsequent delayed status
 * update events will overwrite previously buffered events (only the last one will be fired after the delay).<br>
 * This kind of event has been implemented for MotionSensor functionality, but can be used for other device classes
 * as well if delaying status update events shall be used.
 */
public class DelayedStatusUpdateEvent extends JposStatusUpdateEvent implements Runnable {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public DelayedStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    /**
     * This method must be implemented in derived classes. It specifies how delayed events shall be handled.
     * @return 0 if event shall be fired immediately without affecting any delayed event.<br>
     *         Any value &gt; 0 specifies the delay (in milliseconds) for firing this event (any previously buffered
     *         delayed event will not be fired).<br>
     *         CANCEL_ONLY specifies that this event shall only be fired immediately if no delayed event has been
     *         buffered previously. Otherwise, neither the buffered nor this event shall be fired.<br>
     *         CANCEL_FIRE specify that any buffered event shall not be fired. This event will be fired immediately.
     */
    public long handleDelay() {
        return 0;
    }

    /**
     * Return value for handleDelay. If an event has been buffered for delayed firing, both events (this and the buffered
     * event) will be silently ignored. If no event has been buffered, the event will be fired immediately.
     */
    final static public long CANCEL_ONLY = -1;

    /**
     * Return value for handleDelay. If an event has been buffered for delayed firing, the buffered event will be removed
     * without firing it. The event itself will be fired immediately.
     */
    final static public long CANCEL_FIRE = -2;

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new DelayedStatusUpdateEvent(o, getStatus());
    }

    /**
     * Timeout for delayed event firing.
     */
    private long Timeout;

    /**
     * For delayed status update events, block must return true to avoid firing the event immediately. If delayed,
     * firing occurs in a completely different place.
     * @return If firing shall be delayed or if event firing shall be blocked due to different reason.
     */
    @Override
    public boolean block() {
        if (super.block())
            return true;
        if ((Timeout = handleDelay()) == 0)
            return false;
        JposCommonProperties props = getPropertySet();
        synchronized (props.DelayedStatusUpdateEventWaiter) {
            if (Timeout < 0) {
                if (props.BufferedEvent != null) {
                    props.DelayedStatusUpdateEventWaiter.signal();
                    props.BufferedEvent = null;
                    if (Timeout == CANCEL_ONLY) {
                        setAndCheckStatusProperties();
                        return true;
                    }
                }
                return false;
            }
            else {
                new Thread(props.BufferedEvent = this).start();
                return true;
            }
        }
    }

    @Override
    public void run() {
        JposCommonProperties props = getPropertySet();
        props.DelayedStatusUpdateEventWaiter.suspend(Timeout);
        synchronized (props.DelayedStatusUpdateEventWaiter) {
            if (props.BufferedEvent != this)
                return;
            props.BufferedEvent = null;
        }
        synchronized (props.EventList) {
            try {
                setAndCheckStatusProperties();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            props.EventList.add(this);
            props.Device.log(DEBUG, props.LogicalName + ": Buffer StatusUpdateEvent: [" + toLogString() + "]");
            try {
                props.Device.processEventList(props);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
