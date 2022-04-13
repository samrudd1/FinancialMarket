package trade;

import agent.OwnedGood;
import lombok.Getter;

import java.util.logging.Logger;

public class TradeData implements Comparable {
    @Getter private final float price;
    @Getter private final int volume;
    @Getter private final int round;
    public TradeData(float price, int volume, int round) {
        this.price = price;
        this.volume = volume;
        this.round = round;
    }

    @Override
    public int compareTo(Object o) {
        try{
            TradeData other = (TradeData) o;
            return Integer.compare(this.getRound(), other.getRound());
        } catch (Exception e){
            //Logger.warning("Comparison between an OwnedGood and a different object!");
            return 1;
        }
    }
}
