package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import lombok.extern.java.Log;
import session.Session;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


@Log
public class TradingRound {
    private static final double SELLING_THRESHOLD = 0.9;
    private static final float INCREASE_MULTIPLIER = 1.01f;
    private static final float DECREASE_MULTIPLIER = 0.99f;

    /**
     * This method looks at the goods in the session manager and returns the goods that have directly available units
     * @return a list of goods that can be bought directly
     */
    private ArrayList<Good> getDirectGoodsToSell(){
        ArrayList<Good> directGoodsToReturn = new ArrayList<>();
        for(Good directGood : Session.getGoods().values()){
            if(directGood.getAmountUnsold() > 0){
                directGoodsToReturn.add(directGood);
            }
        }
        return directGoodsToReturn;
    }

    /**
     * Here we look at the owned goods in the session manager,
     * if there are any owned goods here that have a price above the bought at price (
     * or if the price has dropped significantly), they should be put up for sale.
     * @return a list of the agent owned goods that would be advisable to sell
     */
    private ArrayList<OwnedGood> getAgentOwnedGoods(){
        ArrayList<OwnedGood> agentOwnedGoods = new ArrayList<>();
        for(OwnedGood ownedGood : Session.getOwnerships().values()){
            float currentPrice = ownedGood.getGood().getPrice();
            float boughtAtPrice = ownedGood.getBoughtAt();
            if(currentPrice > boughtAtPrice || currentPrice < boughtAtPrice*SELLING_THRESHOLD){
                agentOwnedGoods.add(ownedGood);
            }
        }
        return agentOwnedGoods;
    }

    /**
     * Here we take in two lists, one containing direct goods to be sold and one taking owned goods to be sold.
     * Prioritising directly sold goods, we try to bind goods to agents and set up trades
     * @param directGoodsForSale the list of direct goods that are to be sold.
     * @param ownedGoodsForSale the list of owned goods that are to be sold.
     * @return a list of the trades to be executed.
     */
    private ArrayList<Trade> createTrades(ArrayList<Good> directGoodsForSale, ArrayList<OwnedGood> ownedGoodsForSale) {
        ArrayList<Trade> tradesToExecute = new ArrayList<>();
//        Try to create a trade for each agent
        for (Agent agent : Session.getAgents().values()) {
//            First look at direct goods
            // add element of agent being happy with price
            //direct goods add funds to market or good itself, like raising capital for company
            Trade trade = bindAgentAndGood(directGoodsForSale,agent);
            if (trade == null) trade = bindAgentAndGood(ownedGoodsForSale,agent); //direct good and owned goods for sale should be together
            if (trade != null) tradesToExecute.add(trade);
        }
        return tradesToExecute;
    }

    private Trade bindAgentAndGood(ArrayList<?> goodsForSale, Agent buyer){
        Object goodToBuy = null;
        boolean foundTrade = false;
        int counter = 0;
        if(!goodsForSale.isEmpty()){
            if(goodsForSale.get(0).getClass() == Good.class){
                ArrayList<Good> directGoodsForSale = (ArrayList<Good>) goodsForSale;
                Collections.sort(directGoodsForSale);
                Collections.reverse(directGoodsForSale);
                while(!foundTrade && counter < directGoodsForSale.size()){
                    Good good = directGoodsForSale.get(counter);
                    if(buyer.getFunds() > good.getPrice()){
                        goodToBuy = good;
                        foundTrade = true;
                    } else {
                        counter++;
                    }
                }
            } else if(goodsForSale.get(0).getClass() == OwnedGood.class){
                ArrayList<OwnedGood> ownedGoodsForSale = (ArrayList<OwnedGood>) goodsForSale;
                Collections.sort(ownedGoodsForSale);
                Collections.reverse(ownedGoodsForSale);
                while(!foundTrade && counter < ownedGoodsForSale.size()){
                    OwnedGood good = ownedGoodsForSale.get(counter);
                    if(buyer.getFunds() > good.getGood().getPrice()){
                        goodToBuy = good;
                        foundTrade = true;
                    } else {
                        counter++;
                    }
                }
            } else {
                log.severe("Non Accepted type passed to agent and good binding.");
            }
        }


        try{
            if(goodToBuy != null){
                OwnedGood ownedGood = null;
                Agent seller = null;
                Good good;
                if(goodToBuy.getClass() == OwnedGood.class){
                    ownedGood = (OwnedGood)goodToBuy;
                    seller = ownedGood.getOwner();
                    good = ownedGood.getGood();
                } else {
                    good = (Good)goodToBuy;
                }
                int buyerFunds = (int)Math.floor(buyer.getFunds());
                int goodPrice = (int)Math.floor(good.getPrice());
                int numberCanAfford = (int)Math.floor(buyerFunds/goodPrice);
                int numberAvailable = ownedGood == null ? good.getAmountUnsold() : ownedGood.getNumberOwned();
                int numberToBuy = Math.min(numberAvailable,numberCanAfford);
                goodsForSale.remove(goodToBuy); //TODO This should really lower the amount of the good, not remove it.
                return new Trade(buyer,seller,good,numberToBuy);
            }
        } catch (Exception e){
            log.severe("Casting error when converting good type during trade binding.");
        }

        return null;
    }

    /**
     * Here we take a look at every good in the market and calculate the supply and demand for them.
     */
    private void adjustMarketPrices(){
        for(Good good : Session.getGoods().values()){
            good.setSupply(0);
            good.setDemand(0);
            for(Agent agent : Session.getAgents().values()){
                if(agent.getFunds() > good.getPrice()){
                    good.setDemand(good.getDemand() + 1);
                }
            }
        }
        for(OwnedGood ownedGood : Session.getOwnerships().values()){
            Good good = ownedGood.getGood();
            good.setSupply(good.getSupply() + ownedGood.getNumberOwned());
        }
        for(Good good : Session.getGoods().values()){
            int demand = good.getDemand();
            int supply = good.getSupply();
            if( demand > supply){
                good.setPrice(good.getPrice() * INCREASE_MULTIPLIER);
            } else if(supply > demand){
                good.setPrice(good.getPrice() * DECREASE_MULTIPLIER);
            }
        }
    }

    private void mutate(){
        Random rand = new Random();
        int chance = rand.nextInt(100);
        if(chance > 75){
            Session.getAgentsToDelete().add(new Agent());
            log.info("Adding new Agent to market.");
        }
        if (chance > 95){
            Session.getGoodsToDelete().add(new Good());
            log.info("Adding new Good to market.");
        }
    }

        public void startTrading(){
        Session.setNumOfRounds(Session.getNumOfRounds()+1);
//        Get all the direct goods that can be sold
//        Get all the agent owned goods
//        For the agent owned goods check the bought at price vs current price
//        If a profit can be made (or the price has significantly dropped), add it to the list to sell
//        Look at all agents, choose the most expensive good that the agent can afford and buy as many as possible
//        Now adjust the market prices and supply/demand
//        Mutate market

        ArrayList<Good> directGoodsForSale = getDirectGoodsToSell();
        log.info("There are " + directGoodsForSale.size() + " direct goods for sale.");
        ArrayList<OwnedGood> ownedGoodsForSale = getAgentOwnedGoods();
        log.info("There are " + ownedGoodsForSale.size() + " owned goods for sale.");
        ArrayList<Trade> trades = createTrades(directGoodsForSale,ownedGoodsForSale);
        for(Trade trade : trades){
            log.info("Executing Trade: " + trade.toString());
            trade.execute();
        }
        adjustMarketPrices();
        mutate();

    }


}
