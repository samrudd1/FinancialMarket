package trade;

import agent.OwnedGood;
import good.Good;
import good.Offer;
import agent.Agent;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;

public class ChooseOffer implements Runnable {
    Agent agent;
    TradingCycle tc;
    boolean didOrder = false;

    public ChooseOffer(Agent agent, TradingCycle tc) {
        this.agent = agent;
        this.tc = tc;
    }

    @SneakyThrows
    @Override
    public synchronized void run() {
        float random = (float) Math.random();
        if (random > 0.9) {
            agent.changeTargetPrice();
        }
        float lowestAsk = Exchange.getInstance().getGoods().get(0).getLowestAsk();
        float highestBid = Exchange.getInstance().getGoods().get(0).getHighestBid();

        while(agent.getAgentLock()) wait();
        agent.setAgentLock(true);
        //synchronized (tc) {
        if (agent.getGoodsOwned().size() > 0) {
            int offering = (int) Math.round((float) agent.getGoodsOwned().get(0).getNumAvailable() * 0.25);
            if (offering > 500) {
                offering = 500;
            }
            if ((agent.getTargetPrice() > highestBid) && (highestBid != 0) && (agent.getTargetPrice() < (Good.getPrice() * 1.04))) {// || ((targetPrice < lowestAsk) && (targetPrice > highestBid) && (highestBid != 0))) {
                if (!agent.getPlacedAsk()) {
                    if (offering > 0) {
                        try {
                            createAsk(agent.getTargetPrice(), agent.getGoodsOwned().get(0), offering);
                        } catch (InterruptedException e) {
                            System.out.println("creating ask threw an error");
                        }
                        agent.setPlacedAsk(true);
                    }
                }
            }
        }

        if (agent.getFunds() > Good.getPrice()) {
            float posPurchase = agent.getFunds() / Good.getPrice();
            int purchaseLimit = (int) Math.floor(posPurchase);
            if (posPurchase > 10) {
                purchaseLimit = (int) Math.round(posPurchase * 0.25);
            }
            if (purchaseLimit > 500) {
                purchaseLimit = 500;
            }
            if ((agent.getTargetPrice() < lowestAsk) && (agent.getTargetPrice() > (Good.getPrice() * 0.96))) { //|| ((targetPrice > highestBid) && (targetPrice < lowestAsk) && (highestBid != 0))) {
                if (!agent.getPlacedBid()) {
                    if (purchaseLimit > 0) {
                        try {
                            createBid(agent.getTargetPrice(), Exchange.getInstance().getGoods().get(0), purchaseLimit);
                        } catch (InterruptedException e) {
                            System.out.println("Creating bid threw an error");
                        }
                        agent.setPlacedBid(true);
                    }
                }
            }
        }

        if (agent.getGoodsOwned().size() > 0) {
            if (agent.getTargetPrice() < highestBid) {
                int offering = (int) Math.round((float) agent.getGoodsOwned().get(0).getNumAvailable() * 0.25);
                Offer offer = Exchange.getInstance().getGoods().get(0).getHighestBidOffer();
                if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                    if (offer.getNumOffered() < offering) {
                        offering = offer.getNumOffered();
                    }
                    if (highestBid > (Good.getPrice() * 0.995)) {
                        if (offering > 0) {
                            boolean success = Exchange.getInstance().execute(offer.getOfferMaker(), agent, offer, offering, tc);
                            if (!success) {
                                System.out.println("trade execution failed");
                            }
                        }
                    }
                }
            }
        }

        if (agent.getFunds() > Good.getPrice()) {
            if ((lowestAsk != 0) && (lowestAsk < agent.getTargetPrice())) {
                Offer offer = Exchange.getInstance().getGoods().get(0).getLowestAskOffer();
                int wantToBuy = 0;
                if (!(offer.getOfferMaker().getName().equals(agent.getName()))) {
                    if (offer.getNumOffered() < (agent.getFunds() / offer.getPrice())) {
                        wantToBuy = offer.getNumOffered();
                    } else {
                        wantToBuy = (int) Math.floor(agent.getFunds() / offer.getPrice());
                    }
                    if (lowestAsk < (Good.getPrice() * 1.005)) {
                        if ((wantToBuy > 0) && (agent.getId() != offer.getOfferMaker().getId())) {
                            boolean success = Exchange.getInstance().execute(agent, offer.getOfferMaker(), offer, wantToBuy, tc);
                            if (!success) {
                                System.out.println("trade execution failed");
                            }
                        }
                    }
                }
            }
        }
        if (random > 0.9) {
            agent.changeTargetPrice();
        }
        agent.setAgentLock(false);
        notifyAll();
        //agent.saveUser(false);
        return;
    }

    private void createBid(float price, Good good, int numOffered) throws InterruptedException {
        good.addBid(new Offer(price, agent, good, numOffered));
        agent.setFunds((agent.getFunds() - (price * numOffered)));
    }

    private void createAsk(float price, OwnedGood good, int numOffered) throws InterruptedException {
        good.getGood().addAsk(new Offer(price, agent, good.getGood(), numOffered));
        good.setNumAvailable((good.getNumAvailable() - numOffered));
    }
}