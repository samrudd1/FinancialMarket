package Strategies;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import good.Offer;
import trade.TradingCycle;

public abstract class AbstractStrategy {
    Agent agent;
    TradingCycle tc;

    public AbstractStrategy(Agent agent, TradingCycle tc) {
        this.agent = agent;
        this.tc = tc;
    }

    void createBid(float price, Good good, int numOffered) throws InterruptedException {
        Offer newBid = new Offer(price, agent, good, numOffered);
        good.addBid(newBid);
        agent.setFunds((agent.getFunds() - (price * numOffered)));
        agent.getBidsPlaced().trimToSize();
        agent.getBidsPlaced().add(newBid);
    }

    void createAsk(float price, OwnedGood good, int numOffered) throws InterruptedException {
        Offer newAsk = new Offer(price, agent, good.getGood(), numOffered);
        good.getGood().addAsk(newAsk);
        good.setNumAvailable((good.getNumAvailable() - numOffered));
        agent.getAsksPlaced().trimToSize();
        agent.getAsksPlaced().add(newAsk);
    }

    void cleanOffers(Agent agent, float price) throws InterruptedException {
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
