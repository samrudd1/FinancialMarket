package Strategies;

import agent.Agent;
import good.Good;
import good.Offer;
import lombok.SneakyThrows;
import trade.Exchange;
import trade.TradingCycle;

public class RSI10 extends AbstractStrategy implements Runnable {
    Agent agent;
    TradingCycle tc;
    int roundNum;

    public RSI10(Agent agent, TradingCycle tc, int roundNum) {
        super(agent, tc, roundNum);
        this.agent = agent;
        this.tc = tc;
        this.roundNum = roundNum;
    }

    @SneakyThrows
    @Override
    public synchronized void run() {
        //float lowestAsk = Exchange.getInstance().getGoods().get(0).getLowestAsk();
        //float highestBid = Exchange.getInstance().getGoods().get(0).getHighestBid();
        float price = Good.getPrice();

        while(agent.getAgentLock()) wait();
        agent.setAgentLock(true);

        if (Exchange.getRsiP() < 20) {
            if (agent.getFunds() > price) {
                //if (lowestAsk != 99999) {
                double tradeMult = 0.2 + ((20 - Exchange.getRsiP()) * 0.04);
                //if ((lowestAsk / highestBid) < 101) {
                Offer offer = Exchange.getInstance().getGoods().get(0).getLowestAskOffer();
                if (offer != null) {
                    int wantToBuy;
                    wantToBuy = (int) Math.floor((agent.getFunds() / offer.getPrice()) * tradeMult);
                    //if (!(offer.getOfferMaker().getName().equals(agent.getName()))) { //don't need to use if strategy doesn't place bids or offers
                        if (offer.getNumOffered() < wantToBuy) {
                            wantToBuy = offer.getNumOffered();
                        }
                        if (offer.getPrice() < (Exchange.lastPrice * 1.001)) {
                            if ((wantToBuy > 0) && (agent.getId() != offer.getOfferMaker().getId())) {
                                boolean success = Exchange.getInstance().execute(agent, offer.getOfferMaker(), offer, wantToBuy, tc, roundNum);
                                if (!success) {
                                    System.out.println("trade execution failed");
                                }
                            }
                        }
                    //}
                }
                        /*
                    } else {
                        int wantToBuy;
                        wantToBuy = (int) Math.floor((agent.getFunds() / (highestBid + 0.01)) * tradeMult);
                        if (wantToBuy > 1000) { wantToBuy = 1000; }
                        if (wantToBuy > 0) {
                            float newPrice = (float)(((float)(Math.floor(Exchange.getInstance().getGoods().get(0).getHighestBid() * 100)+5)) * 0.01);
                            if (newPrice < lowestAsk) {
                                createBid(newPrice, Exchange.getInstance().getGoods().get(0), wantToBuy);
                            }
                        }
                    }
                         */
                //}
            }

        } else  if (Exchange.getRsiP() > 80){
            if (agent.getGoodsOwned().size() > 0) {
                //if (highestBid != 0) {
                double tradeMult = 0.2 + ((Exchange.getRsiP() - 80) * 0.04);
                int offering = (int) Math.floor(agent.getGoodsOwned().get(0).getNumAvailable());
                if (offering > 10) {
                    offering = (int) Math.floor((agent.getGoodsOwned().get(0).getNumAvailable() * tradeMult));
                }
                //if ((lowestAsk / highestBid) < 101) {
                Offer offer = Exchange.getInstance().getGoods().get(0).getHighestBidOffer();
                if (offer != null) {
                    //if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                        if (offer.getNumOffered() < offering) {
                            offering = offer.getNumOffered();
                        }
                        if (offer.getPrice() > (Exchange.lastPrice * 0.999)) {
                            if (offering > 0) {
                                boolean success = Exchange.getInstance().execute(offer.getOfferMaker(), agent, offer, offering, tc, roundNum);
                                if (!success) {
                                    System.out.println("trade execution failed");
                                }
                            }
                        }
                    //}
                }
                        /*
                    } else {
                        if (offering > 0) {
                            float newPrice = (float)(((float)(Math.floor(Exchange.getInstance().getGoods().get(0).getLowestAsk() * 100)-5)) * 0.01);
                            if (newPrice > highestBid) {
                                createAsk(newPrice, agent.getGoodsOwned().get(0), offering);
                            }
                        }
                    }
                         */
                //}
            }
        }

        //agent.saveUser(false);
        //cleanOffers(agent, price);
        agent.setAgentLock(false);
        notify();
        return;
    }
}