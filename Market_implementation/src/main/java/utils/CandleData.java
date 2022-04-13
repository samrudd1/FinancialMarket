package utils;

import lombok.Getter;
import trade.TradeData;

import java.util.ArrayList;

public class CandleData {
    @Getter private float open;
    @Getter private float close;
    @Getter private float low;
    @Getter private float high;
    @Getter private int round;
    @Getter private int volume;
    public CandleData() {}
    public CandleData(float o, float c, float l, float h, int r, int v) {
        open = o;
        close = c;
        low = l;
        high = h;
        round = r;
        volume = v;
    }
    public static ArrayList<CandleData> createCandles(ArrayList<TradeData> trades) {
        ArrayList<CandleData> candles = new ArrayList<>();
        int maxRound = trades.get(trades.size() - 1).getRound();
        ArrayList<Float> temp = new ArrayList<Float>();
        int candleCount = 0;
        for (int i = 2; i < maxRound; i++) {
            temp.clear();
            float open = 0;
            float close = 0;
            float low = 99999;
            float high = 0;
            int volume = 0;
            for (TradeData trade: trades) {
                if (trade.getRound() == i) {
                    temp.add(trade.getPrice());
                    volume += trade.getVolume();
                    if (trade.getPrice() < low) {
                        low = trade.getPrice();
                    }
                    if (trade.getPrice() > high) {
                        high = trade.getPrice();
                    }
                }
            }
            if (temp.size() > 1) {
                candleCount++;
                open = temp.get(0);
                close = temp.get(temp.size() - 1);
                candles.add(new CandleData(open, close, low, high, candleCount, volume));
            }
        }
        return candles;
    }
}
