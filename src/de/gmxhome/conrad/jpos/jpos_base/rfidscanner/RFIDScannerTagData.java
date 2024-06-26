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
    private final byte[] TagID;
    private final int TagProtocol;
    private final byte[] TagUserData;

    /**
     * Retrieves tag ID.
     * @return Tag specific ID value.
     */
    public byte[] getTagID() {
        return Arrays.copyOf(TagID, TagID.length);
    }

    /**
     * Retrieves TagProtocol
     * @return Tag specific protocol value.
     */
    public int getTagProtocol() {
        return TagProtocol;
    }

    /**
     * Retrieves TagUserData property.
     * @return Value of tag specific user data.
     */
    public byte[] getTagUserData() {
        return Arrays.copyOf(TagUserData, TagUserData.length);
    }

    /**
     * Creates instance of an RFID tag data element
     * @param tagID       Tag ID.
     * @param tagUserData Tag specific used data.
     * @param tagProtocol Tag specific protocol value.
     */
    public RFIDScannerTagData(byte[] tagID, byte[] tagUserData, int tagProtocol) {
        TagID = Arrays.copyOf(tagID, tagID.length);
        TagUserData = Arrays.copyOf(tagUserData, tagUserData.length);
        TagProtocol = tagProtocol;
    }
}
