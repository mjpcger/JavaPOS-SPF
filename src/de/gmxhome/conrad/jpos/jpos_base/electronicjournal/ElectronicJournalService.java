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

package de.gmxhome.conrad.jpos.jpos_base.electronicjournal;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * ElectronicJournal service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ElectronicJournalService extends JposBase implements ElectronicJournalService115 {
    /**
     * Instance of a class implementing the ElectronicJournalInterface for electronic journal specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ElectronicJournalInterface ElectronicJournalInterface;

    private ElectronicJournalProperties Data;

    static private long MarkerTypesNormal[] = new long[]{
            ElectronicJournalConst.EJ_MT_SESSION_BEG,
            ElectronicJournalConst.EJ_MT_SESSION_END,
            ElectronicJournalConst.EJ_MT_DOCUMENT
    };
    static private long MarkerTypesSpecial[] = new long[]{
            ElectronicJournalConst.EJ_MT_HEAD,
            ElectronicJournalConst.EJ_MT_TAIL
    };

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public ElectronicJournalService(ElectronicJournalProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapAddMarker() throws JposException {
        checkOpened();
        logGet("CapAddMarker");
        return Data.CapAddMarker;
    }

    @Override
    public boolean getCapErasableMedium() throws JposException {
        checkOpened();
        logGet("CapErasableMedium");
        return Data.CapErasableMedium;
    }

    @Override
    public boolean getCapInitializeMedium() throws JposException {
        checkOpened();
        logGet("CapInitializeMedium");
        return Data.CapInitializeMedium;
    }

    @Override
    public boolean getCapMediumIsAvailable() throws JposException {
        checkOpened();
        logGet("CapMediumIsAvailable");
        return Data.CapMediumIsAvailable;
    }

    @Override
    public boolean getCapPrintContent() throws JposException {
        checkOpened();
        logGet("CapPrintContent");
        return Data.CapPrintContent;
    }

    @Override
    public boolean getCapPrintContentFile() throws JposException {
        checkOpened();
        logGet("CapPrintContentFile");
        return Data.CapPrintContentFile;
    }

    @Override
    public boolean getCapRetrieveCurrentMarker() throws JposException {
        checkOpened();
        logGet("CapRetrieveCurrentMarker");
        return Data.CapRetrieveCurrentMarker;
    }

    @Override
    public boolean getCapRetrieveMarker() throws JposException {
        checkOpened();
        logGet("CapRetrieveMarker");
        return Data.CapRetrieveMarker;
    }

    @Override
    public boolean getCapRetrieveMarkerByDateTime() throws JposException {
        checkOpened();
        logGet("CapRetrieveMarkerByDateTime");
        return Data.CapRetrieveMarkerByDateTime;
    }

    @Override
    public boolean getCapRetrieveMarkersDateTime() throws JposException {
        checkOpened();
        logGet("CapRetrieveMarkersDateTime");
        return Data.CapRetrieveMarkersDateTime;
    }

    @Override
    public int getCapStation() throws JposException {
        checkOpened();
        logGet("CapStation");
        return Data.CapStation;
    }

    @Override
    public boolean getCapStorageEnabled() throws JposException {
        checkOpened();
        logGet("CapStorageEnabled");
        return Data.CapStorageEnabled;
    }

    @Override
    public boolean getCapSuspendPrintContent() throws JposException {
        checkOpened();
        logGet("CapSuspendPrintContent");
        return Data.CapSuspendPrintContent;
    }

    @Override
    public boolean getCapSuspendQueryContent() throws JposException {
        checkOpened();
        logGet("CapSuspendQueryContent");
        return Data.CapSuspendQueryContent;
    }

    @Override
    public boolean getCapWaterMark() throws JposException {
        checkOpened();
        logGet("CapWaterMark");
        return Data.CapWaterMark;
    }

    @Override
    public boolean getFlagWhenIdle() throws JposException {
        checkOpened();
        logGet("FlagWhenIdle");
        return Data.FlagWhenIdle;
    }

    @Override
    public void setFlagWhenIdle(boolean flag) throws JposException {
        logPreSet("FlagWhenIdle");
        checkOpened();
        ElectronicJournalInterface.flagWhenIdle(flag);
        logSet("FlagWhenIdle");
    }

    @Override
    public long getMediumFreeSpace() throws JposException {
        checkEnabled();
        logGet("MediumFreeSpace");
        return Data.MediumFreeSpace;
    }

    @Override
    public String getMediumID() throws JposException {
        checkEnabled();
        logGet("MediumID");
        return Data.MediumID;
    }

    @Override
    public boolean getMediumIsAvailable() throws JposException {
        checkEnabled();
        logGet("MediumIsAvailable");
        return Data.MediumIsAvailable;
    }

    @Override
    public long getMediumSize() throws JposException {
        checkEnabled();
        logGet("MediumSize");
        return Data.MediumSize;
    }

    @Override
    public int getStation() throws JposException {
        checkOpened();
        logGet("Station");
        return Data.Station;
    }

    @Override
    public void setStation(int station) throws JposException {
        logPreSet("Station");
        checkOpened();
        Device.check((station & ~Data.CapStation) != 0, JposConst.JPOS_E_ILLEGAL, "Invalid station: " + station);
        ElectronicJournalInterface.station(station);
        logSet("Station");
    }

    @Override
    public boolean getStorageEnabled() throws JposException {
        checkEnabled();
        logGet("StorageEnabled");
        return Data.StorageEnabled;
    }

    @Override
    public void setStorageEnabled(boolean flag) throws JposException {
        logPreSet("StorageEnabled");
        checkEnabled();
        Device.check(!Data.CapStorageEnabled && !flag, JposConst.JPOS_E_ILLEGAL, "Storage cannot be disabled for device");
        ElectronicJournalInterface.storageEnabled(flag);
        logSet("StorageEnabled");
    }

    @Override
    public boolean getSuspended() throws JposException {
        checkOpened();
        logGet("Suspended");
        return Data.Suspended;
    }

    @Override
    public boolean getWaterMark() throws JposException {
        checkOpened();
        logGet("WaterMark");
        return Data.WaterMark;
    }

    @Override
    public void setWaterMark(boolean flag) throws JposException {
        logPreSet("WaterMark");
        checkOpened();
        Device.check(!Data.CapWaterMark && flag, JposConst.JPOS_E_ILLEGAL, "No watermark support for device");
        ElectronicJournalInterface.waterMark(flag);
        logSet("WaterMark");
    }

    @Override
    public void addMarker(String marker) throws JposException {
        logPreCall("AddMarker", "" + marker);
        checkEnabled();
        Device.check(!Data.CapAddMarker, JposConst.JPOS_E_ILLEGAL, "Device does not support AddMarker method");
        ElectronicJournalInterface.addMarker(marker);
        logCall("AddMarker");
    }

    @Override
    public void cancelPrintContent() throws JposException {
        logPreCall("CancelPrintContent");
        checkEnabled();
        Device.check(!Data.CapSuspendPrintContent, JposConst.JPOS_E_ILLEGAL, "Device does not support CancelPrintContent method");
        Device.check(!Data.Suspended, JposConst.JPOS_E_ILLEGAL, "Device not suspended");
        ElectronicJournalInterface.cancelPrintContent();
        logCall("CancelPrintContent");
    }

    @Override
    public void cancelQueryContent() throws JposException {
        logPreCall("CancelQueryContent");
        checkEnabled();
        Device.check(!Data.CapSuspendQueryContent, JposConst.JPOS_E_ILLEGAL, "Device does not support CancelQueryContent method");
        Device.check(!Data.Suspended, JposConst.JPOS_E_ILLEGAL, "Device not suspended");
        ElectronicJournalInterface.cancelQueryContent();
        logCall("CancelQueryContent");
    }

    @Override
    public void eraseMedium() throws JposException {
        logPreCall("EraseMedium");
        checkEnabled();
        Device.check(!Data.CapErasableMedium, JposConst.JPOS_E_ILLEGAL, "EraseMedium not supported");
        Device.check(Data.CapMediumIsAvailable && !Data.MediumIsAvailable, JposConst.JPOS_E_FAILURE, "Medium not available");
        callIt(ElectronicJournalInterface.eraseMedium(), "EraseMedium");
    }

    private void callIt(JposOutputRequest request, String name) throws JposException {
        if (callNowOrLater(request))
            logAsyncCall(name);
        else
            logCall(name);
    }

    @Override
    public void initializeMedium(String mediumID) throws JposException {
        Device.check(mediumID == null, JposConst.JPOS_E_ILLEGAL, "Invalid mediumID parameter: [null]");
        logPreCall("InitializeMedium", mediumID);
        checkEnabled();
        Device.check(!Data.CapInitializeMedium, JposConst.JPOS_E_ILLEGAL, "InitializeMedium not supported");
        callIt(ElectronicJournalInterface.initializeMedium(mediumID), "InitializeMedium");
    }

    @Override
    public void printContent(String fromMarker, String toMarker) throws JposException {
        Device.check(fromMarker == null, JposConst.JPOS_E_ILLEGAL, "Invalid fromMarker parameter: [null]");
        Device.check(toMarker == null, JposConst.JPOS_E_ILLEGAL, "Invalid toMarker parameter: [null]");
        logPreCall("PrintContent", fromMarker + ", " + toMarker);
        checkEnabled();
        Device.check(!Data.CapPrintContent, JposConst.JPOS_E_ILLEGAL, "PrintContent not supported");
        callIt(ElectronicJournalInterface.printContent(fromMarker, toMarker), "PrintContent");
    }

    @Override
    public void printContentFile(String fileName) throws JposException {
        Device.check(fileName == null, JposConst.JPOS_E_ILLEGAL, "Invalid fileName parameter: [null]");
        logPreCall("PrintContentFile", fileName);
        checkEnabled();
        Device.check(!Data.CapPrintContentFile, JposConst.JPOS_E_ILLEGAL, "PrintContentFile not supported");
        callIt(ElectronicJournalInterface.printContentFile(fileName), "PrintContentFile");
    }

    @Override
    public void queryContent(String fileName, String fromMarker, String toMarker) throws JposException {
        Device.check(fileName == null, JposConst.JPOS_E_ILLEGAL, "Invalid fileName parameter: [null]");
        Device.check(fromMarker == null, JposConst.JPOS_E_ILLEGAL, "Invalid fromMarker parameter: [null]");
        Device.check(toMarker == null, JposConst.JPOS_E_ILLEGAL, "Invalid toMarker parameter: [null]");
        logPreCall("QueryContent");
        checkEnabled();
        callIt(ElectronicJournalInterface.queryContent(fileName, fromMarker, toMarker), "QueryContent");
    }

    @Override
    public void resumePrintContent() throws JposException {
        logPreCall("ResumePrintContent");
        checkEnabled();
        Device.check(!Data.CapSuspendPrintContent, JposConst.JPOS_E_ILLEGAL, "Device does not support ResumePrintContent method");
        Device.check(!Data.Suspended, JposConst.JPOS_E_ILLEGAL, "Device not suspended");
        ElectronicJournalInterface.resumePrintContent();
        logCall("ResumePrintContent");
    }

    @Override
    public void resumeQueryContent() throws JposException {
        logPreCall("ResumeQueryContent");
        checkEnabled();
        Device.check(!Data.CapSuspendQueryContent, JposConst.JPOS_E_ILLEGAL, "Device does not support ResumeQueryContent method");
        Device.check(!Data.Suspended, JposConst.JPOS_E_ILLEGAL, "Device not suspended");
        ElectronicJournalInterface.resumeQueryContent();
        logCall("ResumeQueryContent");
    }

    @Override
    public void retrieveCurrentMarker(int markerType, String[] marker) throws JposException {
        logPreCall("RetrieveCurrentMarker", "" + markerType);
        Device.check(marker == null || marker.length != 1, JposConst.JPOS_E_ILLEGAL, "Marker must be a String array with length 1");
        checkEnabled();
        Device.check(!Data.CapRetrieveCurrentMarker, JposConst.JPOS_E_ILLEGAL, "Device does not support RetrieveCurrentMarker method");
        Device.check(!Device.member(markerType, MarkerTypesNormal) && !Device.member(markerType, MarkerTypesSpecial), JposConst.JPOS_E_ILLEGAL, "Bad marker type: "+ markerType);
        ElectronicJournalInterface.retrieveCurrentMarker(markerType, marker);
        logCall("RetrieveCurrentMarker", marker[0]);
    }

    @Override
    public void retrieveMarker(int markerType, int sessionNumber, int documentNumber, String[] marker) throws JposException {
        logPreCall("RetrieveMarker", "" + markerType + ", " + sessionNumber + ", " + documentNumber);
        Device.check(marker == null || marker.length != 1, JposConst.JPOS_E_ILLEGAL, "Marker must be a String array with length 1");
        checkEnabled();
        Device.check(!Data.CapRetrieveMarker, JposConst.JPOS_E_ILLEGAL, "Device does not support RetrieveMarker method");
        Device.checkMember(markerType, MarkerTypesNormal, JposConst.JPOS_E_ILLEGAL, "Bad marker type: "+ markerType);
        Device.check(documentNumber < 0, JposConst.JPOS_E_ILLEGAL, "Illegal document number: " + documentNumber);
        ElectronicJournalInterface.retrieveMarker(markerType, sessionNumber, documentNumber, marker);
        logCall("RetrieveMarker", marker[0]);
    }

    @Override
    public void retrieveMarkerByDateTime(int markerType, String dateTime, String markerNumber, String[] marker) throws JposException {
        Device.check(dateTime == null, JposConst.JPOS_E_ILLEGAL, "Invalid dateTime parameter: [null]");
        Device.check(markerNumber == null, JposConst.JPOS_E_ILLEGAL, "Invalid markerNumber parameter: [null]");
        logPreCall("RetrieveMarkerByDateTime", "" + markerType + ", " + dateTime + ", " + markerNumber);
        Device.check(marker == null || marker.length != 1, JposConst.JPOS_E_ILLEGAL, "Marker must be a String array with length 1");
        checkEnabled();
        Device.check(!Data.CapRetrieveMarkerByDateTime, JposConst.JPOS_E_ILLEGAL, "Device does not support RetrieveMarkerByDateTime method");
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            format.setLenient(false);
            boolean validDate = dateTime.length() >= 8 && dateTime.length() <= 14 && dateTime.length() % 2 == 0;
            validDate = validDate && format.parse(dateTime.substring(0, 8), new ParsePosition(0)) != null;
            validDate = validDate && (dateTime.length() < 10 || (Integer.parseInt(dateTime.substring(8, 10)) >= 0 && Integer.parseInt(dateTime.substring(8, 10)) <= 23));
            validDate = validDate && (dateTime.length() < 12 || (Integer.parseInt(dateTime.substring(10, 12)) >= 0 && Integer.parseInt(dateTime.substring(10, 12)) <= 59));
            validDate = validDate && (dateTime.length() < 14 || (Integer.parseInt(dateTime.substring(12, 14)) >= 0 && Integer.parseInt(dateTime.substring(12, 14)) <= 59));
            Device.check(!validDate, JposConst.JPOS_E_ILLEGAL, "Invalid dateTime value: " + dateTime);
            Device.check(Integer.parseInt(markerNumber) < 1, JposConst.JPOS_E_ILLEGAL, "Illegal markerNumber format, must be numeric >= 1: " + markerNumber);
        } catch (NumberFormatException e) {
            Device.check(true, JposConst.JPOS_E_ILLEGAL, "Format for markerNumber and dateTime must be numeric: " + e.getMessage());
        }
        ElectronicJournalInterface.retrieveMarkerByDateTime(markerType, dateTime, markerNumber, marker);
        logCall("RetrieveMarkerByDateTime", marker[0]);
    }

    @Override
    public void retrieveMarkersDateTime(String marker, String[] dateTime) throws JposException {
        Device.check(marker == null, JposConst.JPOS_E_ILLEGAL, "Invalid marker parameter: [null]");
        logPreCall("RetrieveMarkersDateTime", "" + marker);
        Device.check(dateTime == null || dateTime.length != 1, JposConst.JPOS_E_ILLEGAL, "DateTime must be a String array with length 1");
        checkEnabled();
        Device.check(!Data.CapRetrieveMarkersDateTime, JposConst.JPOS_E_ILLEGAL, "Device does not support RetrieveMarkersDateTime method");
        ElectronicJournalInterface.retrieveMarkersDateTime(marker, dateTime);
        logCall("RetrieveMarkersDateTime", dateTime[0]);
    }

    @Override
    public void suspendPrintContent() throws JposException {
        logPreCall("SuspendPrintContent");
        checkEnabled();
        Device.check(!Data.CapSuspendPrintContent, JposConst.JPOS_E_ILLEGAL, "Device does not support SuspendPrintContent method");
        synchronized (Device.AsyncProcessorRunning) {
            JposOutputRequest effective = Device.CurrentCommand;
            if (Device.CurrentCommand == null && Device.PendingCommands.size() > 0)
                effective = Device.PendingCommands.get(0);
            Device.check(effective == null || (!(effective instanceof PrintContent) && !(effective instanceof PrintContentFile)), JposConst.JPOS_E_ILLEGAL, "Device not printing");
        }
        Device.check(Data.Suspended, JposConst.JPOS_E_ILLEGAL, "Device just suspended");
        ElectronicJournalInterface.suspendPrintContent();
        logCall("SuspendPrintContent");
    }

    @Override
    public void suspendQueryContent() throws JposException {
        logPreCall("SuspendQueryContent");
        checkEnabled();
        Device.check(!Data.CapSuspendQueryContent, JposConst.JPOS_E_ILLEGAL, "Device does not support SuspendQueryContent method");
        synchronized (Device.AsyncProcessorRunning) {
            JposOutputRequest effective = Device.CurrentCommand;
            if (Device.CurrentCommand == null && Device.PendingCommands.size() > 0)
                effective = Device.PendingCommands.get(0);
            Device.check(effective == null || !(effective instanceof QueryContent), JposConst.JPOS_E_ILLEGAL, "Device not querying content");
        }
        Device.check(Data.Suspended, JposConst.JPOS_E_ILLEGAL, "Device just suspended");
        ElectronicJournalInterface.suspendQueryContent();
        logCall("SuspendQueryContent");
    }
}
