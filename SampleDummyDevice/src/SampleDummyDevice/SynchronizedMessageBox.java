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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.SyncObject;

import javax.swing.*;
import java.awt.event.*;

public class SynchronizedMessageBox {
    private JOptionPane Box;
    private JDialog Dialog;
    private ComponentAdapter Adapter;
    private SyncObject Sync = new SyncObject();
    private SyncObject Ready = new SyncObject();

    /**
     * Confirmation result. Is null while confirmation dialog has not been finished. If not null, it holds
     * the index of the selected option, starting at zero. In case of timeout or no selection, it holds the number of
     * options. In case of a stop via abortDialog, it holds -1.
     */
    public Integer Result = -1;

    /**
     * Create a confirmation message box and wait until one option has been selected or a timeout occurs.
     * @param message       Message to be displayed.
     * @param title         Message box title.
     * @param options       Options to be presented.
     * @param defaultOption The default option.
     * @param messageType   JOptionPane message type.
     * @param timeout       timeout in milliseconds.
     */
    public void synchronizedConfirmationBox(final String message, final String title, final String[] options, final String defaultOption, final int messageType, final int timeout) {
        Result = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int result;
                Box = new JOptionPane(message, messageType, JOptionPane.DEFAULT_OPTION, null, options, defaultOption);
                Dialog = Box.createDialog(title);
                if (timeout > 0) {
                    Dialog.addComponentListener(Adapter = new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent e) {
                            super.componentShown(e);
                            new Timer(timeout, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    synchronized (Sync) {
                                        if (Dialog != null && Dialog.isVisible() && Result == null) {
                                            Box.setValue("");
                                            Dialog.setVisible(false);
                                        }
                                    }
                                }
                            }).start();
                        }
                    });
                }
                Dialog.setVisible(true);
                Object selected = Box.getValue();
                for (result = 0; result < options.length; result++) {
                    if (options[result].equals(selected))
                        break;
                }
                if (selected == null)
                    result++;
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
                        Box.setValue("");
                        Dialog.setVisible(false);
                        Result = -1;
                        Ready.signal();
                    }
                }
            }
        });
    }
}
