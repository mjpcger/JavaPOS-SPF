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

package de.gmxhome.conrad.jpos.jpos_base.cashdrawer;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Class containing the drawer specific properties, their default values and default implementations of
 * CashDrawerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Cash Drawer.
 */
public class CashDrawerProperties extends JposCommonProperties implements CashDrawerInterface {
    /**
     * UPOS property CapStatus. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapStatus = true;
    /**
     * UPOS property CapStatusMultiDrawerDetect. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapStatusMultiDrawerDetect = true;
    /**
     * UPOS property DrawerOpened.
     */
    public boolean DrawerOpened;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveAllowed to match the CashDrawer device model.
     *
     * @param dev Device index
     */
    public CashDrawerProperties(int dev)
    {
        super(dev);
        ExclusiveUse = ExclusiveAllowed;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        DrawerOpened = false;
    }

    @Override
    public void openDrawer() throws JposException {
    }

    @Override
    public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
    }

    @Override
    public void waitForDrawerClose() throws JposException {
    }
}
