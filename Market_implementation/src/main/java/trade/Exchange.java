package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import good.Offer;
import lombok.Getter;
import utils.LineChartLive;

import java.util.ArrayList;

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
    @Getter private ArrayList<Float> avg20 = new ArrayList<Float>() ;
    @Getter private static LineChartLive liveChart = new LineChartLive();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static float lastPrice;

    public synchronized boolean execute(Agent buyer, Agent seller, Offer offer, int amountBought, TradingCycle tc) throws InterruptedException {
        complete = false;
        if (buyer == seller) {
            return false;
        } else {
            //while (offer.getGood().getGoodLock()) wait();
            //buyer.setAgentLock(true);
            //seller.setAgentLock(true);
            while (exchangeLock) wait();
            exchangeLock = true;
            //offer.getGood().setGoodLock(true);

            if (Good.getBid().contains(offer) || Good.getAsk().contains(offer)) {
                if ((offer.getPrice() < (lastPrice * 1.01)) || (offer.getPrice() > (lastPrice * 0.99))) {
                    if (offer.getNumOffered() > 0) {
                        if (offer.getNumOffered() < amountBought) {
                            amountBought = offer.getNumOffered();
                        }
                        if ((buyer.getGoodsOwned().isEmpty())) {
                            buyer.getGoodsOwned().add(0, new OwnedGood(buyer, offer.getGood(), amountBought, amountBought, (((float) Math.round(offer.getPrice() * 100)) / 100), true));
                            complete = true;
                        } else {
                            OwnedGood tempOwned = buyer.getGoodsOwned().get(0);
                            float newBoughtAt = (float) Math.round(((tempOwned.getBoughtAt() * tempOwned.getNumOwned()) + (amountBought * offer.getPrice())) * 100) / ((tempOwned.getNumOwned() + amountBought) * 100);
                            int newNumOwned = (tempOwned.getNumOwned() + amountBought);
                            int newAvailable = (tempOwned.getNumAvailable() + amountBought);
                            OwnedGood newOne = new OwnedGood(buyer, offer.getGood(), newNumOwned, newAvailable, newBoughtAt, false);
                            buyer.getGoodsOwned().set(0, newOne);
                            complete = true;
                        }
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
                        //Runnable chartUpdate = new Thread(TradingCycle.getLiveChart());
                        //chartUpdate.run();
                    }
                    lastPrice = offer.getPrice();
                }
                //buyer.setAgentLock(false);
                //seller.setAgentLock(false);
                //offer.getGood().setGoodLock(false);
                TradingCycle.setAgentComplete(true);
                exchangeLock = false;
                notifyAll();
                tc.notifyAll();

                if (avg20.size() < 20) {
                    avg20.add(offer.getPrice());
                } else {
                    avg20.remove(0);
                    avg20.add(offer.getPrice());
                    float avgResult = 0;
                    float total = 0;
                    for (float num : avg20) {
                        avgResult += num;
                        total++;
                    }
                    avgResult = avgResult / total;
                    Good.getAvgPriceList().add(avgResult);
                }

                /*
                if (complete) {
                    if (!buyer.getName().equalsIgnoreCase(seller.getName())) {
                        if (offer.getPrice() > Good.getPrevPrice()) {
                            System.out.println(ANSI_GREEN + "trade executed between " + buyer.getName() + " and "
                                    + seller.getName() + " for " + amountBought + " share/s at a price of "
                                    + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                    + offer.getNumOffered() + " left on the offer." + ANSI_RESET);
                        } else if (offer.getPrice() < Good.getPrevPrice()) {
                            System.out.println(ANSI_RED + "trade executed between " + buyer.getName() + " and "
                                    + seller.getName() + " for " + amountBought + " share/s at a price of "
                                    + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                    + offer.getNumOffered() + " left on the offer." + ANSI_RESET);
                        } else {
                            System.out.println("trade executed between " + buyer.getName() + " and "
                                    + seller.getName() + " for " + amountBought + " share/s at a price of "
                                    + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                    + offer.getNumOffered() + " left on the offer." + ANSI_RESET);
                        }
                    }
                }
                */



                //buyer.saveUser(false);
                //seller.saveUser(false);
            } else {
                //buyer.setAgentLock(false);
                //seller.setAgentLock(false);
                //offer.getGood().setGoodLock(false);
                TradingCycle.setAgentComplete(true);
                exchangeLock = false;
                notifyAll();
                tc.notifyAll();
            }
        }
        //if (Good.getNumTrades() % 10 == 0) {
            //Runnable lc = new Thread(liveChart);
            //lc.run();
        //}
        return complete;
    }
}
