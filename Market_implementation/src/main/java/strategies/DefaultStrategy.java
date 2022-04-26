package strategies;

import agent.Agent;
import good.Offer;
import lombok.SneakyThrows;
import trade.Exchange;
import trade.TradingCycle;

public class DefaultStrategy extends AbstractStrategy implements Runnable {
    Agent agent;
    TradingCycle tc;
    int roundNum;

    public DefaultStrategy(Agent agent, TradingCycle tc, int roundNum) {
        super(agent, tc, roundNum);
        this.agent = agent;
        this.tc = tc;
        this.roundNum = roundNum;
    }

    @SneakyThrows
    @Override
    public synchronized void run() {
        float random = (float) Math.random();
        float lowestAsk = Exchange.getInstance().getGoods().get(0).getLowestAsk();
        float highestBid = Exchange.getInstance().getGoods().get(0).getHighestBid();
        float price = Exchange.getInstance().getPriceCheck();

        //if (random > 0.9) {
            //agent.changeTargetPrice(price);
        //}
        if (random > 0.9) {
            agent.changeTargetPrice();
        }
        //agent.setPlacedBid(false);
        //agent.setPlacedAsk(false);

        while(agent.getAgentLock()) wait();
        agent.setAgentLock(true);
        cleanOffers(agent, price);

        if (agent.getGoodsOwned().size() > 0) {
            int offering = (int) Math.round((float) agent.getGoodsOwned().get(0).getNumAvailable() * 0.25);
            if (offering > 1000) {
                offering = 1000;
            }
            if ((agent.getTargetPrice() > highestBid) && (agent.getTargetPrice() < (price * 1.1))) {// || ((targetPrice < lowestAsk) && (targetPrice > highestBid) && (highestBid != 0))) {
                if (!agent.getPlacedAsk()) {
                    if (offering > 0) {
                        try {
                            createAsk(agent.getTargetPrice(), agent.getGoodsOwned().get(0), offering);
                        } catch (InterruptedException e) {
                            System.out.println("creating ask threw an error");
                        }
                    }
                }
            }
        }

        if (agent.getFunds() > price) {
            int purchaseLimit = (int) Math.floor(((agent.getFunds() / price) * 0.25));
            if (purchaseLimit > 1000) {
                purchaseLimit = 1000;
            }
            if ((agent.getTargetPrice() < lowestAsk) && (agent.getTargetPrice() > (price * 0.9))) { //|| ((targetPrice > highestBid) && (targetPrice < lowestAsk) && (highestBid != 0))) {
                if (!agent.getPlacedBid()) {
                    if (purchaseLimit > 0) {
                        try {
                            createBid(agent.getTargetPrice(), Exchange.getInstance().getGoods().get(0), purchaseLimit);
                        } catch (InterruptedException e) {
                            System.out.println("Creating bid threw an error");
                        }
                    }
                }
            }
        }

        if (agent.getGoodsOwned().size() > 0) {
            if ((agent.getTargetPrice() < highestBid) && (highestBid != 0)) {
                Offer offer = Exchange.getInstance().getGoods().get(0).getHighestBidOffer();
                if (offer != null) {
                    if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                        int offering = (int) Math.floor(agent.getGoodsOwned().get(0).getNumAvailable() * 0.25);
                        if (offer.getNumOffered() < offering) {
                            offering = offer.getNumOffered();
                        }
                        if (offer.getPrice() > (price * 0.984)) {
                            if ((offering > 0) && (agent.getId() != offer.getOfferMaker().getId())) {
                                boolean success = Exchange.getInstance().execute(offer.getOfferMaker(), agent, offer, offering, tc, roundNum);
                                if (!success) {
                                    System.out.println("trade execution failed");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (agent.getFunds() > price) {
            if ((lowestAsk != 99999) && (lowestAsk < agent.getTargetPrice())) {
                Offer offer = Exchange.getInstance().getGoods().get(0).getLowestAskOffer();
                if (offer != null) {
                    if (!(offer.getOfferMaker().getName().equals(agent.getName()))) {
                        int wantToBuy = (int) Math.floor(((agent.getFunds() / offer.getPrice()) * 0.25));
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
                    }
                }
            }
        }

        //causes massive slowdown and causes price to drop a lot in later rounds
        /*
        if (random < 0.2) {
            agent.createTargetPrice();
        }
        */

        if ((agent.getTargetPrice() > (price * 1.2)) || (agent.getTargetPrice() < (price * 0.8))) {
            agent.setTargetPrice(price);
            agent.changeTargetPrice();
        }

        agent.setAgentLock(false);
        notify();
        //agent.saveUser(false);
        return;
    }
}