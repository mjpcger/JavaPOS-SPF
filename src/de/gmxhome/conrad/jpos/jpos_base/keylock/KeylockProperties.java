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

package de.gmxhome.conrad.jpos.jpos_base.keylock;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Class containing the keylock specific properties, their default values and default implementations of
 * KeylockInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Keylock.
 */
public class KeylockProperties extends JposCommonProperties implements KeylockInterface {
    /**
     * UPOS property CapKeylockType. Default: KeylockConst.LOCK_KT_STANDARD. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapKeylockType = KeylockConst.LOCK_KT_STANDARD;
    /**
     * Default value of ElectronicKeyValue property. Default: Zero-length byte array. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public byte[] ElectronicKeyValueDef = new byte[0];
    /**
     * Default value of KeylockConst.LOCK_KP_ANY property. Default: KeylockConst.LOCK_KP_ANY. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int KeyPositionDef = KeylockConst.LOCK_KP_ANY;
    /**
     * UPOS property PositionCount. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int PositionCount = 0;
    /**
     * UPOS property KeyPosition.
     */
    public int KeyPosition;
    /**
     * UPOS property ElectronicKeyValue.
     */
    public byte[] ElectronicKeyValue;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveNo to match the Keylock device model.
     * @param dev Device index
     */
    public KeylockProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveNo;
        FlagWhenIdleStatusValue = -1;   // To avoid FlagWhenIdle handling for LOCK_KP_ELECTRONIC
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
    }

    @Override
    public void initOnEnable(boolean enable) {
        if (enable) {
            super.initOnEnable(enable);
            ElectronicKeyValue = ElectronicKeyValueDef;
            KeyPosition = KeyPositionDef;
        }
    }

    @Override
    public void waitForKeylockChange(int keyPosition, int timeout) throws JposException {
    }
}
