package trade;
import java.util.ArrayList;
import java.util.logging.Logger;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import good.Offer;
import lombok.Getter;
import session.Session;

public class Exchange {
    private static Exchange exchange = new Exchange();
    @Getter private ArrayList<Good> goods = new ArrayList<Good>();
    public Exchange() {}
    public static Exchange getInstance() {
        return exchange;
    }
    private static boolean exchangeLock = false;
    private boolean complete;
    public void addGood(Good good) {
        goods.add(good);
    }
    @Getter private ArrayList<Float> avg10 = new ArrayList<Float>() ;

    public synchronized boolean execute(Agent buyer, Agent seller, Offer offer, int amountBought, TradingCycle tc) throws InterruptedException {
        complete = false;
        boolean bid;
        if (buyer.getName().equals(seller.getName())) {
            return false;
        } else {
            if (buyer.getName().equalsIgnoreCase(offer.getOfferMaker().getName())) {
                bid = true;
            } else {
                bid = false;
            }
            //while (offer.getGood().getGoodLock()) wait();
            //buyer.setAgentLock(true);
            //seller.setAgentLock(true);
            while (exchangeLock) wait();
            exchangeLock = true;
            offer.getGood().setGoodLock(true);

            if (Good.getBid().contains(offer) || Good.getAsk().contains(offer)) {
                if (offer.getNumOffered() > 0) {
                    if (offer.getNumOffered() < amountBought) {
                        amountBought = offer.getNumOffered();
                    }
                    if ((buyer.getGoodsOwned().isEmpty())) {
                        buyer.getGoodsOwned().add(0, new OwnedGood(buyer, offer.getGood(), amountBought, amountBought, (((float) Math.round(offer.getPrice() * 100)) / 100), true));
                        complete = true;
                    } else {
                        OwnedGood tempOwned = buyer.getGoodsOwned().get(0);
                        float newBoughtAt = (float) ((float)Math.round(((tempOwned.getBoughtAt() * tempOwned.getNumOwned()) + (amountBought * offer.getPrice())) * 100) / ((tempOwned.getNumOwned() + amountBought) * 100));
                        int newNumOwned = (tempOwned.getNumOwned() + amountBought);
                        int newAvailable = (tempOwned.getNumAvailable() + amountBought);
                        OwnedGood newOne = new OwnedGood(buyer, offer.getGood(), newNumOwned, newAvailable, newBoughtAt, false);
                        buyer.getGoodsOwned().set(0, newOne);
                        complete = true;
                    }
                    if (complete) {
                        offer.getGood().setPrice(offer, amountBought);
                        if (amountBought == offer.getNumOffered()) {
                            if (Good.getBid().contains(offer)) {
                                offer.getGood().removeBid(offer);
                                offer.setNumOffered(0);
                            } else {
                                offer.getGood().removeAsk(offer);
                                offer.setNumOffered(0);
                            }
                        } else {
                            offer.setNumOffered(offer.getNumOffered() - amountBought);
                        }

                        buyer.setFunds(buyer.getFunds() - (offer.getPrice() * amountBought));
                        seller.setFunds(seller.getFunds() + (offer.getPrice() * amountBought));
                    }
                }

                TradingCycle.setAgentComplete(true);
                tc.notifyAll();
                //buyer.setAgentLock(false);
                //seller.setAgentLock(false);
                offer.getGood().setGoodLock(false);
                exchangeLock = false;
                notifyAll();

                if (avg10.size() < 20) {
                    avg10.add(offer.getPrice());
                } else {
                    avg10.remove(0);
                    avg10.add(offer.getPrice());
                    float avgResult = 0;
                    float total = 0;
                    for (float num : avg10) {
                        avgResult += num;
                        total++;
                    }
                    avgResult = avgResult / total;
                    Good.getAvgPriceList().add(avgResult);
                }

                if (complete) {
                    if (!(buyer.getName().equalsIgnoreCase(offer.getOfferMaker().getName()))) {
                        System.out.println("trade executed between " + buyer.getName() + " and "
                                + offer.getOfferMaker().getName() + " for " + amountBought + " share/s at a price of "
                                + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                + offer.getNumOffered() + " left on the offer.");
                    }
                }
                //buyer.saveUser(false);
                //seller.saveUser(false);
            } else {
                TradingCycle.setAgentComplete(true);
                offer.getGood().setGoodLock(false);
                exchangeLock = false;
                notifyAll();
                tc.notifyAll();
            }
        }
        return complete;
    }
}
