package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import good.Offer;
import lombok.SneakyThrows;

public class DefaultStrategy implements Runnable {
    Agent agent;
    TradingCycle tc;

    public DefaultStrategy(Agent agent, TradingCycle tc) {
        this.agent = agent;
        this.tc = tc;
    }

    @SneakyThrows
    @Override
    public synchronized void run() {
        float random = (float) Math.random();
        float lowestAsk = Exchange.getInstance().getGoods().get(0).getLowestAsk();
        float highestBid = Exchange.getInstance().getGoods().get(0).getHighestBid();
        float price = Good.getPrice();

        //if (random > 0.9) {
            //agent.changeTargetPrice(price);
        //}
        if (random > 0.9) {
            agent.changeTargetPrice();
        }

        while(agent.getAgentLock()) wait();
        agent.setAgentLock(true);
        cleanOffers(agent, price);

        if (agent.getGoodsOwned().size() > 0) {
            int offering = (int) Math.round((float) agent.getGoodsOwned().get(0).getNumAvailable() * 0.25);
            if (offering > 1000) {
                offering = 1000;
            }
            if ((agent.getTargetPrice() > highestBid) && (highestBid != 0) && (agent.getTargetPrice() < (price * 1.03))) {// || ((targetPrice < lowestAsk) && (targetPrice > highestBid) && (highestBid != 0))) {
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

        if (agent.getFunds() > price) {
            int purchaseLimit = (int) Math.floor(((agent.getFunds() / price) * 0.25));
            if (purchaseLimit > 1000) {
                purchaseLimit = 1000;
            }
            if ((agent.getTargetPrice() < lowestAsk) && (agent.getTargetPrice() > (price * 0.97))) { //|| ((targetPrice > highestBid) && (targetPrice < lowestAsk) && (highestBid != 0))) {
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
            if ((agent.getTargetPrice() < highestBid) && (highestBid != 0)) {
                int offering = (int) Math.floor(agent.getGoodsOwned().get(0).getNumAvailable() * 0.25);
                Offer offer = Exchange.getInstance().getGoods().get(0).getHighestBidOffer();
                if (offer != null) {
                    if (!(offer.getOfferMaker().getName().equalsIgnoreCase(agent.getName()))) {
                        if (offer.getNumOffered() < offering) {
                            offering = offer.getNumOffered();
                        }
                        if (offer.getPrice() > (Exchange.lastPrice * 0.999)) {
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
        }

        if (agent.getFunds() > price) {
            if ((lowestAsk != 99999) && (lowestAsk < agent.getTargetPrice())) {
                Offer offer = Exchange.getInstance().getGoods().get(0).getLowestAskOffer();
                if (offer != null) {
                    if (!(offer.getOfferMaker().getName().equals(agent.getName()))) {
                        int wantToBuy = (int) Math.floor(((agent.getFunds() / offer.getPrice()) * 0.25));
                        if (offer.getNumOffered() < (agent.getFunds() / offer.getPrice())) {
                            wantToBuy = offer.getNumOffered();
                        }
                        if (offer.getPrice() < (Exchange.lastPrice * 1.001)) {
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
        }

        //causes massive slowdown and causes price to drop a lot in later rounds
        /*
        if (random < 0.2) {
            agent.createTargetPrice();
        }
        */
        agent.setAgentLock(false);
        notify();
        //agent.saveUser(false);
        return;
    }

    private void createBid(float price, Good good, int numOffered) throws InterruptedException {
        Offer newBid = new Offer(price, agent, good, numOffered);
        good.addBid(newBid);
        agent.setFunds((agent.getFunds() - (price * numOffered)));
        agent.getBidsPlaced().trimToSize();
        agent.getBidsPlaced().add(newBid);
    }

    private void createAsk(float price, OwnedGood good, int numOffered) throws InterruptedException {
        Offer newAsk = new Offer(price, agent, good.getGood(), numOffered);
        good.getGood().addAsk(newAsk);
        good.setNumAvailable((good.getNumAvailable() - numOffered));
        agent.getAsksPlaced().trimToSize();
        agent.getAsksPlaced().add(newAsk);
    }

    private void cleanOffers(Agent agent, float price) throws InterruptedException {
        if (agent.getBidsPlaced().size() > 0) {
            agent.getBidsPlaced().trimToSize();
            for (Offer offer : agent.getBidsPlaced()) {
                if (offer.getPrice() < (price * 0.95)) {
                    offer.getGood().removeBid(offer);
                }
            }
            agent.getBidsPlaced().trimToSize();
        }
        if (agent.getAsksPlaced().size() > 0) {
            agent.getAsksPlaced().trimToSize();
            for (Offer offer : agent.getAsksPlaced()) {
                if (offer.getPrice() > (price * 1.05)) {
                    offer.getGood().removeBid(offer);
                }
            }
            agent.getAsksPlaced().trimToSize();
        }
    }
}