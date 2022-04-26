package strategies;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import good.Offer;
import trade.TradingCycle;

public abstract class AbstractStrategy {
    Agent agent;
    TradingCycle tc;
    int roundNum;

    public AbstractStrategy(Agent agent, TradingCycle tc, int roundNum) {
        this.agent = agent;
        this.tc = tc;
        this.roundNum = roundNum;
    }

    void createBid(float price, Good good, int numOffered) throws InterruptedException {
        if ((price < good.getLowestAsk()) && (numOffered > 0)) {
            Offer newBid = new Offer(price, agent, good, numOffered);
            good.addBid(newBid);
            agent.setFunds((agent.getFunds() - (price * numOffered)));
            agent.getBidsPlaced().trimToSize();
            agent.getBidsPlaced().add(newBid);
            agent.setPlacedBid(true);
        }
    }

    void createAsk(float price, OwnedGood good, int numOffered) throws InterruptedException {
        if ((price > good.getGood().getHighestBid()) && (numOffered > 0)) {
            Offer newAsk = new Offer(price, agent, good.getGood(), numOffered);
            good.getGood().addAsk(newAsk);
            good.setNumAvailable((good.getNumAvailable() - numOffered));
            agent.getAsksPlaced().trimToSize();
            agent.getAsksPlaced().add(newAsk);
            agent.setPlacedAsk(true);
        }
    }

    void cleanOffers(Agent agent, float price) throws InterruptedException {
        if (agent.getBidsPlaced().size() > 0) {
            agent.getBidsPlaced().trimToSize();
            for (Offer offer : agent.getBidsPlaced()) {
                if (offer.getPrice() < (price * 0.9)) {
                    offer.getGood().removeBid(offer);
                }
            }
            agent.getBidsPlaced().trimToSize();
        }
        if (agent.getAsksPlaced().size() > 0) {
            agent.getAsksPlaced().trimToSize();
            for (Offer offer : agent.getAsksPlaced()) {
                if (offer.getPrice() > (price * 1.1)) {
                    offer.getGood().removeAsk(offer);
                }
            }
            agent.getAsksPlaced().trimToSize();
        }
    }
}
