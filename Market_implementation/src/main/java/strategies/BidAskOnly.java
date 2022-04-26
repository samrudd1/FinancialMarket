package strategies;

import agent.Agent;
import lombok.SneakyThrows;
import trade.Exchange;
import trade.TradingCycle;

public class BidAskOnly extends AbstractStrategy implements Runnable {
    Agent agent;
    TradingCycle tc;
    int roundNum;

    public BidAskOnly(Agent agent, TradingCycle tc, int roundNum) {
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