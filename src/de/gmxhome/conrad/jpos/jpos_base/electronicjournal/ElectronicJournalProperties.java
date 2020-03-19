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
import org.apache.log4j.*;

/**
 * Class containing the electronic journal specific properties, their default values and default implementations of
 * ElectronicJournalInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Electronic Journal.
 */
public class ElectronicJournalProperties extends JposCommonProperties implements ElectronicJournalInterface {
    /**
     * UPOS property CapAddMarker. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAddMarker = false;

    /**
     * UPOS property CapErasableMedium. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapErasableMedium = false;

    /**
     * UPOS property CapInitializeMedium. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapInitializeMedium = false;

    /**
     * UPOS property CapMediumIsAvailable. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMediumIsAvailable = false;

    /**
     * UPOS property CapPrintContent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPrintContent = false;

    /**
     * UPOS property CapPrintContentFile. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPrintContentFile = false;

    /**
     * UPOS property CapRetrieveCurrentMarker. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRetrieveCurrentMarker = false;

    /**
     * UPOS property CapRetrieveMarker. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRetrieveMarker = false;

    /**
     * UPOS property CapRetrieveMarkerByDateTime. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRetrieveMarkerByDateTime = false;

    /**
     * UPOS property CapRetrieveMarkersDateTime. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRetrieveMarkersDateTime = false;

    /**
     * UPOS property CapStation. Default: S_RECEIPT. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapStation = ElectronicJournalConst.EJ_S_RECEIPT;

    /**
     * UPOS property CapStorageEnabled. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapStorageEnabled = false;

    /**
     * UPOS property CapSuspendPrintContent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSuspendPrintContent = false;

    /**
     * UPOS property CapSuspendQueryContent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSuspendQueryContent = false;

    /**
     * UPOS property CapWaterMark. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapWaterMark = false;

    /**
     * Default value of MediumFreeSpace property. Default: 0. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public long MediumFreeSpaceDef = 0;

    /**
     * UPOS property MediumFreeSpace.
     */
    public long MediumFreeSpace;

    /**
     * Default value of MediumID property. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String MediumIDDef = "";

    /**
     * UPOS property MediumID.
     */
    public String MediumID;

    /**
     * Default value of MediumIsAvailable property. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean MediumIsAvailableDef = true;

    /**
     * UPOS property MediumIsAvailable.
     */
    public boolean MediumIsAvailable;

    /**
     * Default value of MediumSize property. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public long MediumSizeDef = 0;

    /**
     * UPOS property MediumSize.
     */
    public long MediumSize;

    /**
     * UPOS property Station. Default: S_RECEIPT. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int Station = ElectronicJournalConst.EJ_S_RECEIPT;

    /**
     * UPOS property StorageEnabled.
     */
    public boolean StorageEnabled;

    /**
     * UPOS property Suspended.
     */
    public boolean Suspended;

    /**
     * UPOS property WaterMark. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean WaterMark = false;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public ElectronicJournalProperties(int dev) {
        super(dev);
        FlagWhenIdleStatusValue = ElectronicJournalConst.EJ_SUE_IDLE;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        Suspended = false;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            MediumFreeSpace = MediumFreeSpaceDef;
            MediumID = MediumIDDef;
            MediumIsAvailable = MediumIsAvailableDef;
            MediumSize = MediumSizeDef;
            return false;
        }
        return true;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (enable && !CapStorageEnabled) {
            StorageEnabled = true;
        }
    }

    @Override
    public JposOutputRequest newJposOutputRequest() {
        return new JposInputRequest(this);
    }

    @Override
    public void clearInput() throws JposException {
        synchronized (DataEventList) {
            DataEventList.clear();
            DataCount = 0;
        }
        newJposOutputRequest().clearInput();
        State = JposConst.JPOS_S_IDLE;
        clearErrorProperties();
        newJposOutputRequest().reactivate(false);
        if (Device.CurrentCommand == null && Device.PendingCommands.size() == 0 && FlagWhenIdle) {
            FlagWhenIdle = false;
            EventSource.logSet("FlagWhenIdle");
            Device.handleEvent(new JposStatusUpdateEvent(EventSource, FlagWhenIdleStatusValue));
        }
    }

    @Override
    public void clearOutput() throws JposException {
        synchronized (ErrorEventList) {
            ErrorEventList.clear();
        }
        newJposOutputRequest().clearOutput();
        if (State != JposConst.JPOS_S_IDLE) {
            State = JposConst.JPOS_S_IDLE;
            EventSource.logSet("State");
        }
        clearOutputErrorProperties();
        newJposOutputRequest().reactivate(true);
        if (Device.CurrentCommand == null && Device.PendingCommands.size() == 0 && FlagWhenIdle) {
            FlagWhenIdle = false;
            EventSource.logSet("FlagWhenIdle");
            Device.handleEvent(new ElectronicJournalStatusUpdateEvent(EventSource, FlagWhenIdleStatusValue));
        }
    }

    @Override
    public void retryOutput() {
        clearErrorProperties();
        newJposOutputRequest().reactivate(false);
        Device.log(Level.DEBUG, LogicalName + ": Enter Retry output...");
    }

    @Override
    public void retryInput() {
        clearErrorProperties();
        newJposOutputRequest().reactivate(true);
        Device.log(Level.DEBUG, LogicalName + ": Enter Retry input...");
    }

    @Override
    public void station(int station) throws JposException {
        Station = station;
    }

    @Override
    public void storageEnabled(boolean flag) throws JposException {
        StorageEnabled = flag;
    }

    @Override
    public void waterMark(boolean flag) throws JposException {
        WaterMark = flag;
    }

    @Override
    public void addMarker(String marker) throws JposException {
    }

    @Override
    public EraseMedium eraseMedium() throws JposException {
        return new EraseMedium(this);
    }

    @Override
    public void eraseMedium(EraseMedium request) throws JposException {
    }

    @Override
    public InitializeMedium initializeMedium(String mediumID) throws JposException {
        return new InitializeMedium(this, mediumID);
    }

    @Override
    public void initializeMedium(InitializeMedium request) throws JposException {
    }

    @Override
    public PrintContent printContent(String fromMarker, String toMarker) throws JposException {
        return new PrintContent(this, fromMarker, toMarker);
    }

    @Override
    public void printContent(PrintContent request) throws JposException {
    }

    @Override
    public PrintContentFile printContentFile(String fileName) throws JposException {
        return new PrintContentFile(this, fileName);
    }

    @Override
    public void printContentFile(PrintContentFile request) throws JposException {
    }

    @Override
    public QueryContent queryContent(String fileName, String fromMarker, String toMarker) throws JposException {
        return new QueryContent(this, fileName, fromMarker, toMarker);
    }

    @Override
    public void queryContent(QueryContent request) throws JposException {
    }

    @Override
    public void cancelPrintContent() throws JposException {
    }

    @Override
    public void cancelQueryContent() throws JposException {
    }

    @Override
    public void resumePrintContent() throws JposException {
    }

    @Override
    public void resumeQueryContent() throws JposException {
    }

    @Override
    public void retrieveCurrentMarker(int markerType, String[] marker) throws JposException {
    }

    @Override
    public void retrieveMarker(int markerType, int sessionNumber, int documentNumber, String[] marker) throws JposException {
    }

    @Override
    public void retrieveMarkerByDateTime(int markerType, String dateTime, String markerNumber, String[] marker) throws JposException {
    }

    @Override
    public void retrieveMarkersDateTime(String marker, String[] dateTime) throws JposException {
    }

    @Override
    public void suspendPrintContent() throws JposException {
    }

    @Override
    public void suspendQueryContent() throws JposException {
    }
}
