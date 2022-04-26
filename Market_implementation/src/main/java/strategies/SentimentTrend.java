package strategies;

import agent.Agent;
import good.Offer;
import lombok.SneakyThrows;
import trade.Exchange;
import trade.TradingCycle;

public class SentimentTrend extends AbstractStrategy implements Runnable {
    Agent agent;
    TradingCycle tc;
    int roundNum;

    public SentimentTrend(Agent agent, TradingCycle tc, int roundNum) {
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

        while(agent.getAgentLock()) wait();
        agent.setAgentLock(true);

        if (Agent.getSentiment() > 21) {
            if (agent.getFunds() > price) {
                //if (lowestAsk != 99999) {
                    Offer offer = Exchange.getInstance().getGoods().get(0).getLowestAskOffer();
                    if (offer != null) {
                        int wantToBuy;
                        wantToBuy = (int) Math.floor((agent.getFunds() / offer.getPrice()) * 0.5);
                        //if (!(offer.getOfferMaker().getName().equals(agent.getName()))) {
                            if (offer.getNumOffered() < wantToBuy) {
                                wantToBuy = offer.getNumOffered();
                            }
                            if (offer.getPrice() < (price * 1.02)) {
                                if ((wantToBuy > 0) && (agent.getId() != offer.getOfferMaker().getId())) {
                                    boolean success = Exchange.getInstance().execute(agent, offer.getOfferMaker(), offer, wantToBuy, tc, roundNum);
                                    if (!success) {
                                        System.out.println("trade execution failed");
                                    }
                                }
                            }
                        //}
                    }
                //}
            }

                /*
                if (agent.getFunds() > price) {
                    int purchaseLimit = (int) Math.floor(((agent.getFunds() / price) * (0.05 * diffSent)));
                    if (purchaseLimit > 1000) {
                        purchaseLimit = 1000;
                    }
                    if (!agent.getPlacedBid()) {
                        if (purchaseLimit > 0) {
                            try {
                                createBid(price, Exchange.getInstance().getGoods().get(0), purchaseLimit);
                            } catch (InterruptedException e) {
                                System.out.println("Creating bid threw an error");
                            }
                            agent.setPlacedBid(true);
                        }
                    }
                }
                */
        } else  if (Agent.getSentiment() < 18){
            if (agent.getGoodsOwned().size() > 0) {
                //if (highestBid != 0) {
                    int offering = (int) Math.floor(agent.getGoodsOwned().get(0).getNumAvailable());
                    if (offering > 5) {
                        offering = (int) Math.floor((agent.getGoodsOwned().get(0).getNumAvailable() * 0.5));
                    }
                    Offer offer = Exchange.getInstance().getGoods().get(0).getHighestBidOffer();
                    if (offer != null) {
                        //if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                            if (offer.getNumOffered() < offering) {
                                offering = offer.getNumOffered();
                            }
                            if (offer.getPrice() > (price * 0.98)) {
                                if (offering > 0) {
                                    boolean success = Exchange.getInstance().execute(offer.getOfferMaker(), agent, offer, offering, tc, roundNum);
                                    if (!success) {
                                        System.out.println("trade execution failed");
                                    }
                                }
                            }
                        //}
                    }
                //}
            }

                /*
                if (agent.getGoodsOwned().size() > 0) {
                    int offering = (int) Math.floor((agent.getGoodsOwned().get(0).getNumAvailable() * (-0.05 * diffSent)));
                    if (offering > 1000) {
                        offering = 1000;
                    }
                    if (highestBid != 0) {// || ((targetPrice < lowestAsk) && (targetPrice > highestBid) && (highestBid != 0))) {
                        if (!agent.getPlacedAsk()) {
                            if (offering > 0) {
                                try {
                                    createAsk(price, agent.getGoodsOwned().get(0), offering);
                                } catch (InterruptedException e) {
                                    System.out.println("creating ask threw an error");
                                }
                                agent.setPlacedAsk(true);
                            }
                        }
                    }
                }
                */
        }

        //agent.saveUser(false);
        //cleanOffers(agent, price);
        agent.setAgentLock(false);
        notify();
        return;
    }
}
