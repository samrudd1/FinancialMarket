package trade;

import org.jfree.data.time.RegularTimePeriod;

import java.util.Calendar;

public class Time extends RegularTimePeriod {
    private long serialIndex;
    public Time(long roundNum) {
        this.serialIndex = roundNum;
    }
    @Override
    public RegularTimePeriod previous() {
        return null;
    }

    @Override
    public RegularTimePeriod next() {
        return null;
    }

    @Override
    public long getSerialIndex() {
        return serialIndex;
    }

    @Override
    public void peg(Calendar calendar) {

    }

    @Override
    public long getFirstMillisecond() {
        return serialIndex;
    }

    @Override
    public long getFirstMillisecond(Calendar calendar) {
        return serialIndex;
    }

    @Override
    public long getLastMillisecond() {
        return serialIndex;
    }

    @Override
    public long getLastMillisecond(Calendar calendar) {
        return serialIndex;
    }

    @Override
    public int compareTo(Object o) {
        try{
            Time other = (Time) o;
            return Long.compare(this.getSerialIndex(), other.getSerialIndex());
        } catch (Exception e){
            //Logger.warning("Comparison between an OwnedGood and a different object!");
            return 1;
        }
    }
}
