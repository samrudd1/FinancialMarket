package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import lombok.Getter;
import lombok.ToString;
import session.Session;

import java.util.ArrayList;
import java.util.logging.Logger;

@ToString
public class InitialTrade implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Trade.class.getName());
    @Getter
    private Agent buyer;
    @Getter
    private Good good;
    @Getter
    private int amountBought;
    @Getter
    private float price;


    public InitialTrade(Agent buyer, Good good, int amountBought, float price) {
        this.buyer = buyer;
        this.good = good;
        this.amountBought = amountBought;
        this.price = price;
    }

    public void run() {
        if ((buyer.getGoodsOwned().isEmpty())) {
            buyer.getGoodsOwned().add(0, new OwnedGood(buyer, good, amountBought, Good.getPrice(), true));
            buyer.setFunds(buyer.getFunds() - (Good.getPrice() * amountBought));
            Session.setMarketFunds(Session.getMarketFunds() + (Good.getPrice() * amountBought));
            buyer.saveUser(false);
        } else {
            OwnedGood tempOwned = buyer.getGoodsOwned().get(0);
            tempOwned.setBoughtAt(((tempOwned.getBoughtAt() * tempOwned.getNumOwned()) + (amountBought * Good.getPrice())) / (tempOwned.getNumOwned() + amountBought));
            tempOwned.setNumOwned(tempOwned.getNumOwned() + amountBought);
            buyer.setFunds(buyer.getFunds() - (Good.getPrice() * amountBought));
            Session.setMarketFunds(Session.getMarketFunds() + (Good.getPrice() * amountBought));
            buyer.getGoodsOwned().set(0, tempOwned);
            tempOwned.save(false);
            buyer.saveUser(false);
        }
        Good.setDirectlyAvailable(Good.getDirectlyAvailable() - amountBought);
        LOGGER.info("executing trade with " + buyer.getName() + " directly for " + amountBought + " share/s at a price of " + Good.getPrice() + " each.");
    }
}