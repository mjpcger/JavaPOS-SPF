/*
 * Copyright 2022 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import jpos.JposException;

/**
 * Output request executor for ElectronicValueRW method ActivateEVService.
 */
public class ActivateEVService extends DataObjRequest {
    /**
     * Constructor. Stores given parameters for later use.
     * @param props       Property set of device service.
     * @param data        vendor specific data.
     * @param obj         vendor specific Object.
     */
    public ActivateEVService(ElectronicValueRWProperties props, int[] data, Object[] obj) {
        super(props, data, obj);
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicValueRWService)Props.EventSource).ElectronicValueRW.activateEVService(this);
        super.invoke();
    }
}
