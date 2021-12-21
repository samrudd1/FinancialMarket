package trade;

import agent.OwnedGood;
import good.Good;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

public class GoodForSale {
    @Getter @Setter private OwnedGood ownedGood;
    @Getter @Setter private Good good;
    @Getter @Setter private float desiredPrice;
    private static final float DIRECT_GOOD_PRICE_MULTIPLIER = 1.01f;
    private DecimalFormat df = new DecimalFormat("0.00");

    public GoodForSale(OwnedGood ownedGood, float desiredPrice){
        this.ownedGood = ownedGood;
        this.good = ownedGood.getGood();
        this.desiredPrice = Float.parseFloat(df.format(desiredPrice));
    }

    /**
     * a constructor for direct goods
     * @param good the good to be sold
     */
    public GoodForSale(Good good){
        this.good = good;
        this.ownedGood = null;
        this.desiredPrice = Float.parseFloat(df.format(good.getPrice() * DIRECT_GOOD_PRICE_MULTIPLIER));
    }

    @Override
    public String toString(){
            return (
                    "Good: " + good.getName() + "\n" +
                            "Current Price: " + good.getPrice() + "\n" +
                            "Desired price: " + desiredPrice
            );
    }
}
