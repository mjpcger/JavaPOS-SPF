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

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import java.util.Arrays;

/**
 * Helper class for RFIDScanner tag data.
 */
public class RFIDScannerTagData {
    private byte[] TagID = null;
    private int TagProtocol = 0;
    private byte[] TagUserData = null;

    public byte[] getTagID() {
        return Arrays.copyOf(TagID, TagID.length);
    }

    public int getTagProtocol() {
        return TagProtocol;
    }

    public byte[] getTagUserData() {
        return Arrays.copyOf(TagUserData, TagUserData.length);
    }

    public RFIDScannerTagData(byte[] tagID, byte[] tagUserData, int tagProtocol) {
        TagID = Arrays.copyOf(tagID, tagID.length);
        TagUserData = Arrays.copyOf(tagUserData, tagUserData.length);
        TagProtocol = tagProtocol;
    }
}
