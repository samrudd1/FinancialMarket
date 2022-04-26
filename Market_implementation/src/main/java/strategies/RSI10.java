package strategies;

import agent.Agent;
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
        float price = Exchange.getInstance().getPriceCheck();
        float rsi = Exchange.getRsiP();

        while(agent.getAgentLock()) wait();
        agent.setAgentLock(true);

        if ((rsi >= 0) && (rsi <= 100)) {
            if (rsi < 30) {
                if (agent.getFunds() > price) {
                    //if (lowestAsk != 99999) {
                    double tradeMult = 0.4 + ((30 - rsi) * 0.02);
                    //if ((lowestAsk / highestBid) < 101) {
                    Offer offer = Exchange.getInstance().getGoods().get(0).getLowestAskOffer();
                    if (offer != null) {
                        int wantToBuy = (int) Math.floor((agent.getFunds() / offer.getPrice()) * tradeMult);
                        if (offer.getNumOffered() < wantToBuy) {
                            wantToBuy = offer.getNumOffered();
                        }
                        if ((wantToBuy > 0) && (agent.getId() != offer.getOfferMaker().getId()) && (offer.getPrice() < (price * 1.03))) {
                            boolean success = Exchange.getInstance().execute(agent, offer.getOfferMaker(), offer, wantToBuy, tc, roundNum);
                            if (!success) {
                                System.out.println("trade execution failed");
                            }
                        }
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

            } else if (rsi > 70) {
                if (agent.getGoodsOwned().size() > 0) {
                    //if (highestBid != 0) {
                    double tradeMult = 0.4 + ((rsi - 70) * 0.02);
                    //if ((lowestAsk / highestBid) < 101) {
                    Offer offer = Exchange.getInstance().getGoods().get(0).getHighestBidOffer();
                    if (offer != null) {
                        int offering = (int) Math.floor((agent.getGoodsOwned().get(0).getNumAvailable() * tradeMult));
                        if (offer.getNumOffered() < offering) {
                            offering = offer.getNumOffered();
                        }
                        if ((offering > 0) && (agent.getId() != offer.getOfferMaker().getId()) && (offer.getPrice() > (price * 0.98))) {
                            boolean success = Exchange.getInstance().execute(offer.getOfferMaker(), agent, offer, offering, tc, roundNum);
                            if (!success) {
                                System.out.println("trade execution failed");
                            }
                        }
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
        }

        //agent.saveUser(false);
        //cleanOffers(agent, price);
        agent.setAgentLock(false);
        notify();
        return;
    }
}