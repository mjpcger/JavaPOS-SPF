package SampleCAT;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

/**
 * Factory class for ElectronicJournal sample service implementation
 */
public class ElectronicJournalFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            int index = Integer.parseInt(jposEntry.getPropertyValue("DevIndex").toString());
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String port = jposEntry.getPropertyValue("Port").toString();

            synchronized(Devices) {
                if (deviceClass.equals("ElectronicJournal")) {
                    JposDevice any = getDevice(port);
                    Device dev;
                    boolean created = any != null;
                    if (!created) {
                        dev = new Device(port, jposEntry.getPropertyValue("DisplayName"), jposEntry.getPropertyValue("JournalPath"));
                    } else if (!(any instanceof Device))
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Port " + port + " used by " + any.getClass().getName());
                    else {
                        dev = (Device) any;
                    }
                    dev.checkRange(index, 0, dev.ElectronicJournals.length - 1, JposConst.JPOS_E_ILLEGAL, "Electronic journal index out of range");
                    dev.checkProperties(jposEntry);
                    JposServiceInstance srv = addDevice(index, dev);
                    if (!created)
                        putDevice(port, dev);
                    return srv;
                }
            }
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "Bad device category " + deviceClass);
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid or missing JPOS property", e);
        }
    }
}
