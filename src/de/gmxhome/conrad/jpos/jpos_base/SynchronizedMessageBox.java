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

import javax.swing.*;
import javax.swing.plaf.OptionPaneUI;
import java.awt.*;
import java.awt.event.*;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static javax.swing.JOptionPane.*;

/**
 * Helper class to create message boxes from any thread that are synchronized with the current thread. Option dialogs
 * as well as simple message boxes can be created that way.
 */
public class SynchronizedMessageBox {
    private JOptionPane Box;
    private JDialog Dialog;
    private Point CurrentPosition;
    private final SyncObject Sync = new SyncObject();
    private final SyncObject Ready = new SyncObject();
    private final Timer[] Timer = {null};

    /**
     * Confirmation result. Is null while confirmation dialog has not been finished. If not null, it holds
     * the index of the selected option, starting at zero. -2 in case of timeout,  -1 in case of a stop
     * via abortDialog or no selection. Keep in mind: -1 is equal to CLOSE_OPTION.
     */
    public Integer Result = CLOSED_OPTION;

    /**
     * Create a confirmation message box and wait until one option has been selected or a timeout occurs. If at least
     * one option has been specified, defaultOption must be equal to one of the given options. Otherwise, defaultOption
     * can be null to specify a default optionType of DEFAULT_OPTION or a string representation of one of YES_NO_OPTION,
     * YES_NO_CANCEL_OPTION or OK_CANCEL_OPTION.
     * @param message       Message to be displayed.
     * @param title         Message box title.
     * @param options       Options to be presented, null for simple message boxes.
     * @param defaultOption The default option.
     * @param messageType   JOptionPane message type.
     * @param timeout       timeout in milliseconds. If &le; 0, no timeout handling will occur.
     * @return      If timeout occurred, CLOSED_OPTION - 1. If box has been close by user or via AbortDialog, CLOSED_OPTION.
     *              If options is null, YES_OPTION, NO_OPTION or CANCEL_OPTION. If option is array of String, the index
     *              of the selected option.
     */
    public int synchronizedConfirmationBox(final String message, final String title, final String[] options, final String defaultOption, final int messageType, final int timeout) {
        Result = null;
        synchronized (JposOutputRequest.JposRequestThread.ActiveMessageBoxes) {
            JposOutputRequest.JposRequestThread.ActiveMessageBoxes.add(this);
        }
        SwingUtilities.invokeLater(() -> {
            int result = CLOSED_OPTION;
            try {
                int optionType = (options == null && defaultOption != null) ? Integer.parseInt(defaultOption) : DEFAULT_OPTION;
                Box = new JOptionPane(message, messageType, optionType, null, options, options == null ? null : defaultOption);
                Dialog = Box.createDialog(title);
                if (CurrentPosition != null)
                    Dialog.setLocation(CurrentPosition);
                if (Timer[0] == null) {
                    Dialog.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent e) {
                            super.componentShown(e);
                            Timer[0] = new Timer(Integer.MAX_VALUE, (ActionEvent ignore) -> {
                                synchronized (Sync) {
                                    if (Dialog != null && Dialog.isVisible() && Result == null) {
                                        Box.setValue(CLOSED_OPTION - 1);
                                        Dialog.setVisible(false);
                                    }
                                }
                                Timer[0].stop();
                            });
                            if (timeout > 0) {
                                Timer[0].setInitialDelay(timeout);
                                Timer[0].start();
                            }
                        }
                    });
                } else if (timeout > 0) {
                    Timer[0].setInitialDelay(timeout);
                    Timer[0].restart();
                }
                Dialog.setAlwaysOnTop(true);
                Dialog.setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                if (timeout > 0) {
                    Dialog.setVisible(true);
                    if (Timer[0] != null)
                        Timer[0].stop();
                } else
                    Dialog.setVisible(true);
                Object selected = Box.getValue();
                if (selected != null) {
                    if (selected instanceof String) {
                        if (!"".equals(selected)) {
                            assert options != null;
                            for (result = options.length - 1; result >= 0; result--) {
                                if (options[result].equals(selected))
                                    break;
                            }
                        }
                    } else
                        result = (Integer) selected;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                result = CLOSED_OPTION;
                Ready.signal();
            } finally {
                synchronized (Sync) {
                    Result = result;
                    Ready.signal();
                    CurrentPosition = Dialog.getLocation();
                    Dialog.dispose();
                    Dialog = null;
                }
            }
        });
        Ready.suspend(INFINITE);
        synchronized (JposOutputRequest.JposRequestThread.ActiveMessageBoxes) {
            JposOutputRequest.JposRequestThread.ActiveMessageBoxes.remove(this);
        }
        return Result;
    }

    /**
     * Input result. Is null while input dialog has not been finished. If not null, it holds
     * the input value.
     */
    String InputResult = null;

    /**
     * Create an input message box and wait until input has been finished or a timeout occurs. If at least
     * one option has been specified, a combo box containing the options as selectable input will be generated. Otherwise,
     * a text field can be filled.
     * @param message       Message to be displayed.
     * @param title         Message box title.
     * @param options       Options for combo box or null for simple input field.
     * @param defaultOption The default value.
     * @param messageType   JOptionPane message type.
     * @param timeout       timeout in milliseconds. If &le; 0, no timeout handling will occur.
     * @return      The selected or entered input value or null in case of cancellation or timeout.
     */
    @SuppressWarnings("unused")
    public String synchronizedInputBox(final String message, final String title, final String[] options, final String defaultOption, final int messageType, final int timeout) {
        InputResult = null;
        synchronized (JposOutputRequest.JposRequestThread.ActiveMessageBoxes) {
            JposOutputRequest.JposRequestThread.ActiveMessageBoxes.add(this);
        }
        SwingUtilities.invokeLater(() -> {
            String result = null;
            try {
                Box = new JOptionPane(message, messageType, DEFAULT_OPTION, null, null, DEFAULT_OPTION);
                Box.setWantsInput(true);
                if (options != null && options.length > 0)
                    Box.setSelectionValues(options);
                Box.setInitialSelectionValue(defaultOption);
                Box.setInputValue(null);
                Dialog = Box.createDialog(title);
                if (CurrentPosition != null)
                    Dialog.setLocation(CurrentPosition);
                if (Timer[0] == null) {
                    Dialog.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent e) {
                            super.componentShown(e);
                            Timer[0] = new Timer(Integer.MAX_VALUE, (ActionEvent ignore) -> {
                                synchronized (Sync) {
                                    if (Dialog != null && Dialog.isVisible() && InputResult == null) {
                                        Box.setValue(CLOSED_OPTION);
                                        Dialog.setVisible(false);
                                    }
                                }
                                Timer[0].stop();
                            });
                            if (timeout > 0) {
                                Timer[0].setInitialDelay(timeout);
                                Timer[0].start();
                            }
                        }
                    });
                } else if (timeout > 0) {
                    Timer[0].setInitialDelay(timeout);
                    Timer[0].restart();
                }
                Dialog.setAlwaysOnTop(true);
                Dialog.setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                if (timeout > 0) {
                    Dialog.setVisible(true);
                    if (Timer[0] != null)
                        Timer[0].stop();
                } else
                    Dialog.setVisible(true);
                Object input = Box.getInputValue();
                if (input != null) {
                    if (input instanceof String) {
                        result = input.toString();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Ready.signal();
            } finally {
                synchronized (Sync) {
                    InputResult = result;
                    Ready.signal();
                    CurrentPosition = Dialog.getLocation();
                    Dialog.dispose();
                    Dialog = null;
                }
            }
        });
        Ready.suspend(INFINITE);
        synchronized (JposOutputRequest.JposRequestThread.ActiveMessageBoxes) {
            JposOutputRequest.JposRequestThread.ActiveMessageBoxes.remove(this);
        }
        return InputResult;
    }

    /**
     * Method to abort the dialog.
     */
    public void abortDialog() {
        SwingUtilities.invokeLater(() -> {
            synchronized (Sync) {
                if (Dialog != null && Dialog.isVisible()) {
                    Box.setValue(CLOSED_OPTION);
                    Dialog.setVisible(false);
                }
            }
        });
    }
}
