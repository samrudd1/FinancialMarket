package Strategies;

import agent.Agent;
import good.Good;
import good.Offer;
import lombok.SneakyThrows;
import trade.Exchange;
import trade.TradingCycle;

public class HighFrequency extends AbstractStrategy implements Runnable {
    Agent agent;
    TradingCycle tc;
    static int roundNum;
    int finalRound;

    public HighFrequency(Agent agent, TradingCycle tc, int roundNum, int finalRound) {
        super(agent, tc, roundNum);
        this.agent = agent;
        this.tc = tc;
        HighFrequency.roundNum = roundNum;
        this.finalRound = finalRound;
    }

    @SneakyThrows
    @Override
    public synchronized void run() {
        Good good = Exchange.getInstance().getGoods().get(0);
        float highestBid;
        float secondHighestBid;
        float lowestAsk;
        float secondLowestAsk;
        //while (((tc.getNumOfRounds() % 10) > 0) && (tc.getNumOfRounds() < (finalRound - 1))) {
        while (tc.getNumOfRounds() < (finalRound - 1)) {
            if (agent.getFunds() > Exchange.lastPrice) {
                lowestAsk = good.getLowestAsk();
                secondLowestAsk = good.getSecondLowestAsk();
                if ((lowestAsk != 99999) && (secondLowestAsk != 99999)) {
                    if ((lowestAsk - secondLowestAsk) > 0.02) {
                        Offer offer = good.getLowestAskOffer();
                        if ((agent.getFunds() * 0.5) > (offer.getPrice() * offer.getNumOffered())) {
                            if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                                if (offer.getNumOffered() > 2) {
                                    boolean success = Exchange.getInstance().execute(agent, offer.getOfferMaker(), offer, offer.getNumOffered(), tc, roundNum);
                                    if (!success) {
                                        System.out.println("trade execution failed");
                                    } else {
                                        int numShares = offer.getNumOffered();
                                        //int firstOffer = (int) Math.floor(numShares * 0.5);
                                        try {
                                            createAsk((float) (secondLowestAsk - 0.01), agent.getGoodsOwned().get(0), numShares);
                                        } catch (InterruptedException e) {
                                            System.out.println("creating ask threw an error");
                                        }
                                        //try {
                                        //    createAsk((float) (secondLowestAsk + 0.01), agent.getGoodsOwned().get(0), numShares);
                                        //} catch (InterruptedException e) {
                                        //    System.out.println("creating ask threw an error");
                                        //}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (agent.getGoodsOwned().size() > 0) {
                highestBid = good.getHighestBid();
                secondHighestBid = good.getSecondHighestBid();
                if ((highestBid != 0) && (secondHighestBid != 0)) {
                    if ((highestBid - secondHighestBid) > 0.02) {
                        Offer offer = good.getHighestBidOffer();
                        if (agent.getGoodsOwned().get(0).getNumOwned() > (offer.getPrice() * offer.getNumOffered())) {
                            if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                                boolean success = Exchange.getInstance().execute(offer.getOfferMaker(), agent, offer, offer.getNumOffered(), tc, roundNum);
                                if (!success) {
                                    System.out.println("trade execution failed");
                                } else {
                                    int wantToBuy = (int)Math.floor(agent.getFunds() / Exchange.lastPrice);
                                    if (wantToBuy > 1000) {
                                        wantToBuy = 500;
                                    } else {
                                        wantToBuy = offer.getNumOffered();
                                        wantToBuy = (int)Math.floor(wantToBuy * 0.5);
                                    }
                                    try {
                                        createBid((float)(secondHighestBid + 0.01), good, (wantToBuy * 2));
                                    } catch (InterruptedException e) {
                                        System.out.println("creating bid threw an error");
                                    }
                                    //try {
                                    //    createBid((float)(secondHighestBid - 0.01), good, wantToBuy);
                                    //} catch (InterruptedException e) {
                                    //    System.out.println("creating bid threw an error");
                                    //}
                                }
                            }
                        }
                    }
                }
            }
        }
        return;
    }
}
