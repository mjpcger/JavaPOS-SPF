/*
 * Copyright 2018 Martin Conrad
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

package SampleCAT;

import de.gmxhome.conrad.jpos.jpos_base.cat.*;
import jpos.JposConst;
import jpos.JposException;

/**
 * Sample device specific accessor class. The device uses the following commands:
 * <ul>
 *     <li>p%d\3			Set print line width. Parameters: Line width (must be &ge; 28).</li>
 *     <li>l%d\3			Lock or unlock terminal. Parameters: 0: unlock, 1: lock</li>
 *     <li>b\3				Begin transaction. s, v or r must follow.</li>
 *     <li>s%f\3			Set sale amount. Parameters: Amount.</li>
 *     <li>c%d\2%d\3		Commit operation. Parameters: No. of transaction to be committed, result (0: Verification
 *                          error, 1: Signature verified). Mandatory after sign-based sale operations.</li>
 *     <li>r%f\3			Set return amount. Parameters: Amount.</li>
 *     <li>v%d\3			Void transaction. Parameters: No. of transaction to be voided.</li>
 *     <li>a\3				Abort operation.</li>
 * </ul>
 * In addition, the device sends the following responses:
 * <ul>
 *     <li>L%d\3										Lock terminal. Parameters: Result code (0: OK, 4: just locked).</li>
 *     <li>U%d\3										Unlock terminal. Parameters: Result code (0: OK, 4: just unlocked).</li>
 *     <li>B%d\3										Begin transaction. Parameters: Result code (0: OK, 4: just locked,
 *                                                      6: waiting for commit, 7: authorization active).</li>
 *     <li>E%d\3										End. Parameters: Result code (0: OK, 3: Abort, 4: locked, 5: no
 *                                                      transaction, 6: wait for commit, 7: other operation active,
 *                                                      8: invalid transaction).</li>
 *     <li>E%d\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\3 	End processing. Parameters: Result code (0: OK, 1: wait for commit,
 *                                                      2: Error), Result code (0: no error), approval result (0: OK,
 *                                                      1111: check, else error), balance, tip (included in balance),
 *                                                      card issuer (abbreviation, see IssuerList), card no (last 4 digits),
 *                                                      expiration date, transaction number, transaction time (format
 *                                                      YYYYmmddTHHMMSS).</li>
 * </ul>
 * The device sends the following status messages:
 * <ul>
 *     <li>D%d\2%s\3					Display line. Parameters: line no (0-3), contents (UTF-8).</li>
 *     <li>P%d\2%s\3					Print ticket. Parameters: count (1-2), ticket data (UTF-8), may contain line feeds.</li>
 * </ul>
 * The device will be connected via TCP.
 */
public class CAT extends CATProperties {
    private Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for sample is always 0.
     * @param dev   Instance of Device that controls the communication with the terminal.
     */
    public CAT(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void claim(int timeout) throws JposException {
        if (timeout < Dev.MinClaimTimeout)
            timeout = Dev.MinClaimTimeout;
        super.claim(timeout);
        Dev.setPrintWidth(Dev.JournalWidth);
        if (Dev.InIOError) {
            release();
            throw new JposException(JposConst.JPOS_E_NOHARDWARE, "CAT not detected");
        }
    }

    @Override
    public void release() throws JposException {
        Dev.ToBeFinished = true;
        synchronized(Dev) {
            Dev.closePort();
        }
        while (Dev.ToBeFinished) {
            try {
                Dev.StateWatcher.join();
            } catch (Exception e) {}
            break;
        }
        Dev.StateWatcher = null;
        PowerState = JposConst.JPOS_PS_UNKNOWN;
        EventSource.logSet("PowerState");
        Dev.InIOError = false;
        super.release();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        CheckHealthText = Dev.InIOError ? "Internal CheckHealth: OFFLINE" : "Internal CheckHealth: OK";
        EventSource.logSet("CheckHealthText");
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        Dev.lock(!enable, Dev.RequestTimeout);
        super.deviceEnabled(enable);
    }

    @Override
    public void authorizeSales(AuthorizeSales request) throws JposException  {
        Dev.Display.init();
        Dev.Ticket.init();
        int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
        try {
            long starttime = System.currentTimeMillis();
            Dev.beginAuthorization(timeout);
            long deltatime = System.currentTimeMillis() - starttime;
            if (deltatime < timeout) {
                Dev.sale(false, request.getAmount() + request.getTaxOthers(), (int) (timeout - deltatime));
                deltatime = System.currentTimeMillis() - starttime;
                if (Integer.parseInt(CenterResultCode) != 0 && deltatime < timeout) {
                    Dev.confirm(Integer.parseInt(AdditionalSecurityInformation), true, (int) (timeout - deltatime));
                }
                SequenceNumber = request.getSequenceNumber();
            }
        } finally {
            Dev.Ticket.TransactionDate = SlipNumber;
            Dev.Ticket.release();
            Dev.Display.release();
        }
    }

    @Override
    public AuthorizeVoid authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        // AdditionalSecurityInformation must be an integer value, containing the transaction number generated by
        // the device.
        try {
            Integer.parseInt(AdditionalSecurityInformation);
        }
        catch (Exception e) {
            Dev.check(true, JposConst.JPOS_E_ILLEGAL, "AdditionalSecurityInformation (device transaction number) invalid");
        }
        return super.authorizeVoid(sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizeVoid(AuthorizeVoid request) throws JposException  {
        Dev.Display.init();
        Dev.Ticket.init();
        int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
        try {
            long starttime = System.currentTimeMillis();
            Dev.beginAuthorization(timeout);
            long deltatime = System.currentTimeMillis() - starttime;
            if (deltatime < timeout) {
                Dev.rollback(Integer.parseInt(request.getAdditionalSecurityInformation()), (int) (timeout - deltatime));
                SequenceNumber = request.getSequenceNumber();
            }
        } finally {
            Dev.Ticket.TransactionDate = SlipNumber;
            Dev.Ticket.release();
            Dev.Display.release();
        }
    }

    @Override
    public void authorizeRefund(AuthorizeRefund request) throws JposException  {
        Dev.Display.init();
        Dev.Ticket.init();
        int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
        try {
            long starttime = System.currentTimeMillis();
            Dev.beginAuthorization(timeout);
            long deltatime = System.currentTimeMillis() - starttime;
            if (deltatime < timeout) {
                Dev.sale(true, request.getAmount() + request.getTaxOthers(), (int) (timeout - deltatime));
                SequenceNumber = request.getSequenceNumber();
            }
        } finally {
            Dev.Ticket.TransactionDate = SlipNumber;
            Dev.Ticket.release();
            Dev.Display.release();
        }
    }

    @Override
    public void clearOutput() throws JposException {
        Dev.abort();
        super.clearOutput();
    }
}
