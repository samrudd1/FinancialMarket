package trade;
import java.util.ArrayList;
import java.util.logging.Logger;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import lombok.Getter;
import session.Session;

public class Exchange {
    private static Exchange exchange = new Exchange();
    private ArrayList<Good> goods = new ArrayList<Good>();
    public Exchange() {}
    public static Exchange getInstance() {
        return exchange;
    }

    public void addGood(Good good) {
        goods.add(good);
    }

    public synchronized void openingTrade(Agent buyer, Agent seller, Good good, int amountBought, float price) throws InterruptedException {
        while ((buyer.getAgentLock() == true) || (seller.getAgentLock() == true) || (good.getGoodLock() == true)) wait();
        buyer.setAgentLock(true);
        seller.setAgentLock(true);
        good.setGoodLock(true);
        if ((buyer.getGoodsOwned().isEmpty())) {
            buyer.getGoodsOwned().add(0, new OwnedGood(buyer, good, amountBought, (((float)Math.round(price * 100)) / 100), true));
            buyer.setFunds(buyer.getFunds() - (price * amountBought));
            seller.setFunds(seller.getFunds() + (price * amountBought));
            Good.setDirectlyAvailable(Good.getDirectlyAvailable() - amountBought); // still uses initial
            //return true;
        } else {
            OwnedGood tempOwned = buyer.getGoodsOwned().get(0);
            float newBoughtAt = (float)(Math.round(((tempOwned.getBoughtAt() * tempOwned.getNumOwned()) + (amountBought * price)) * 100) / ((tempOwned.getNumOwned() + amountBought) * 100));
            int newNumOwned = (tempOwned.getNumOwned() + amountBought);
            buyer.setFunds((buyer.getFunds() - (price * amountBought)));
            seller.setFunds(seller.getFunds() + (price * amountBought));
            OwnedGood newOne = new OwnedGood(buyer, good, newNumOwned, newBoughtAt, false);
            buyer.getGoodsOwned().set(0, newOne);
            Good.setDirectlyAvailable(Good.getDirectlyAvailable() - amountBought);
            //return true;
        }
        buyer.setAgentLock(false);
        seller.setAgentLock(false);
        good.setGoodLock(false);
        notifyAll();
    }

}
