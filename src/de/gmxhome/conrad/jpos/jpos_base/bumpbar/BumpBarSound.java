package de.gmxhome.conrad.jpos.jpos_base.bumpbar;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.UnitOutputRequest;
import jpos.JposException;

import static jpos.JposConst.*;

/**
 * Output request executor for BumpBar method BumpBarSound.
 */
public class BumpBarSound extends UnitOutputRequest {
    /**
     * Retrieves parameter frequency of method BumpBarSound. See UPOS specification for further information.
     * @return  Value of method parameter frequency.
     */
    public int getFrequency() {
        return Frequency;
    }

    private final int Frequency;

    /**
     * Retrieves parameter duration of method BumpBarSound. See UPOS specification for further information.
     * @return  Value of method parameter duration.
     */
    public int getDuration() {
        return Duration;
    }

    private final int Duration;

    /**
     * Retrieves parameter numberOfCycles of method BumpBarSound. See UPOS specification for further information.
     * @return  Value of method parameter numberOfCycles.
     */
    public int getNumberOfCycles() {
        return NumberOfCycles;
    }

    private final int NumberOfCycles;

    /**
     * Retrieves parameter interSoundWait of method BumpBarSound. See UPOS specification for further information.
     * @return  Value of method parameter interSoundWait.
     */
    public int getInterSoundWait() {
        return InterSoundWait;
    }

    private final int InterSoundWait;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param units Units where status has been changed.
     * @param frequency         Tone frequency in Hertz.
     * @param duration          Tone duration in milliseconds.
     * @param numberOfCycles    Number of cycles to generate tone.
     * @param interSoundWait    Delay between sounds, in milliseconds.
     */
    public BumpBarSound(JposCommonProperties props, int units, int frequency, int duration, int numberOfCycles, int interSoundWait) {
        super(props, units);
        Frequency = frequency;
        Duration = duration;
        NumberOfCycles = numberOfCycles;
        InterSoundWait = interSoundWait;
    }

    @Override
    public void invoke() throws JposException {
        BumpBarService svc = (BumpBarService) Props.EventSource;
        if (EndSync == null) {
            checkUnitsOnline();
            int errunits = svc.validateTone(getUnits());
            svc.check(errunits != 0, errunits, JPOS_E_FAILURE, 0, "Selected units do not support bump bar sound: " + errunits, EndSync != null);
        }
        svc.BumpBarInterface.bumpBarSound(this);
    }
}
