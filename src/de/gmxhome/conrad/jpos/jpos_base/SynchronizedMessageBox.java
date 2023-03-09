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
import java.awt.*;
import java.awt.event.*;

/**
 * Helper class to create message boxes from any thread that are synchronized with the current thread. Option dialogs
 * as well as simple message boxes can be created that way.
 */
public class SynchronizedMessageBox {
    private JOptionPane Box;
    private JDialog Dialog;
    private SyncObject Sync = new SyncObject();
    private SyncObject Ready = new SyncObject();
    private final Timer[] Timer = {null};

    /**
     * Confirmation result. Is null while confirmation dialog has not been finished. If not null, it holds
     * the index of the selected option, starting at zero. -2 in case of timeout,  -1 in case of a stop
     * via abortDialog or no selection. Keep in mind: -1 is equal to JOptionPane.CLOSE_OPTION.
     */
    public Integer Result = JOptionPane.CLOSED_OPTION;

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
     */
    public void synchronizedConfirmationBox(final String message, final String title, final String[] options, final String defaultOption, final int messageType, final int timeout) {
        Result = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int result;
                int optionType = (options == null && defaultOption != null) ? Integer.parseInt(defaultOption) : JOptionPane.DEFAULT_OPTION;
                Box = new JOptionPane(message, messageType, optionType, null, options, options == null ? null : defaultOption);
                Dialog = Box.createDialog(title);
                if (Timer[0] == null) {
                    Dialog.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent e) {
                            super.componentShown(e);
                            Timer[0] = new Timer(Integer.MAX_VALUE, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    synchronized (Sync) {
                                        if (Dialog != null && Dialog.isVisible() && Result == null) {
                                            Box.setValue(JOptionPane.CLOSED_OPTION - 1);
                                            Dialog.setVisible(false);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
                Dialog.setAlwaysOnTop(true);
                Dialog.setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                if (timeout > 0) {
                    Timer[0].setInitialDelay(timeout);
                    Timer[0].start();
                    Dialog.setVisible(true);
                    Timer[0].stop();
                } else
                    Dialog.setVisible(true);
                Object selected = Box.getValue();
                if (selected == null)
                    result = JOptionPane.CLOSED_OPTION;
                else if (selected instanceof String) {
                    if ("".equals(selected))
                        result = JOptionPane.CLOSED_OPTION;
                    else {
                        for (result = options.length - 1; result >= 0; result--) {
                            if (options[result].equals(selected))
                                break;
                        }
                    }
                } else
                    result = (Integer) selected;
                synchronized (Sync) {
                    Result = result;
                    Ready.signal();
                    Dialog.dispose();
                    Dialog = null;
                }
            }
        });
        Ready.suspend(SyncObject.INFINITE);
    }

    /**
     * Method to abort the dialog.
     */
    public void abortDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (Sync) {
                    if (Dialog != null && Dialog.isVisible()) {
                        Box.setValue(JOptionPane.CLOSED_OPTION);
                        Dialog.setVisible(false);
                        Result = JOptionPane.CLOSED_OPTION;
                        Ready.signal();
                    }
                }
            }
        });
    }
}
