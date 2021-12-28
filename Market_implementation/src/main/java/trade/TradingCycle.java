package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import lombok.extern.java.Log;
import session.Session;
import java.util.ArrayList;
import java.util.Random;


@Log
public class TradingCycle {
    private static final double SELLING_THRESHOLD = 0.9;
    private static final float INCREASE_MULTIPLIER = 1.01f;
    private static final float DECREASE_MULTIPLIER = 0.99f;

    public void startTrading(int numOfRounds){
        Session.setNumOfRounds(Session.getNumOfRounds()+1);
        ArrayList<OwnedGood> ownedGoodsForSale = getAgentOwnedGoods();
        initialOffering();
        for (int i = 0; i < (numOfRounds - 1); i++) {
            initialOffering();
            mutate();
        }
    }

    private void initialOffering() {
        //This could be used as opening bell type auction, or IPO
        log.info("There are " + Good.getDirectlyAvailable() + " direct goods for sale at " + Good.getPrice());
        int startingOffer = Good.getDirectlyAvailable();
        for (Agent agent : Session.getAgents().values()) {
            if ((Good.getPrice() < agent.getTargetPrice()) && (Good.getDirectlyAvailable() > 0)) {
                int wantToBuy = (startingOffer / Session.getNumAgents());

                if(Good.getDirectlyAvailable() >= wantToBuy) {
                    if (agent.getFunds() < (wantToBuy * Good.getPrice())) {
                        wantToBuy = (int) Math.floor(agent.getFunds() / Good.getPrice());
                    }
                    InitialTrade it1 = new InitialTrade(agent, Session.getGoods().get(0), wantToBuy, Good.getPrice());
                    Thread t1 = new Thread(it1);
                    t1.start();
                    //if (trade == null) trade = bindAgentAndGood(ownedGoodsForSale, agent); //direct good and owned goods for sale should be together
                }
            }
        }
    }

    private void createTrades() {
        //This could be used as opening bell type auction, or IPO
        for (Agent agent : Session.getAgents().values()) {
            if ((Good.getPrice() < agent.getTargetPrice()) && (Good.getDirectlyAvailable() > 0)) {
                int wantToBuy = (Good.getOutstandingShares() / Session.getNumAgents());
                log.info(agent.getName() + " wants " + wantToBuy + " stocks, there are " + Good.getDirectlyAvailable() + " available directly");
                log.info("there are " + Session.getNumAgents() + " agents");

                if(Good.getDirectlyAvailable() >= wantToBuy) {
                    if (agent.getFunds() < (wantToBuy * Good.getPrice())) {
                        wantToBuy = (int) Math.floor(agent.getFunds() / Good.getPrice());
                    }
                    //purchaseDirect(agent, wantToBuy);
                        //if (trade == null) trade = bindAgentAndGood(ownedGoodsForSale, agent); //direct good and owned goods for sale should be together
                }
            }
        }
    }

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

    //adds randomness to market, from original version
    private void mutate(){
        Random rand = new Random();
        int chance = rand.nextInt(100);
        if(chance > 75){
            Session.getAgentsToDelete().add(new Agent());
            log.info("Adding new Agent to market.");
            Session.setNumAgents(Session.getNumAgents() + 1);
        }
        if (chance > 95){
            Session.getGoodsToDelete().add(new Good(true));
            log.info("Adding new Good to market.");
            Good.setOutstandingShares(Good.getOutstandingShares() + 1);
            //sell for under market price
        }
    }


    /*
    //could move to trade class, use on separate thread
    private void purchaseDirect(Agent buyer, int amount){
        for (int i = 0; i < amount; i++) {
            Good good = Session.getDirectGoods().get(0);
            buyer.getGoodsOwned().add(new OwnedGood(buyer, good, Good.getPrice())); //getPrice will change for auction in future
            Session.getDirectGoods().remove(0);
            Session.getDirectGoods().trimToSize();
            buyer.setFunds(buyer.getFunds() - Good.getPrice());
            Session.setMarketFunds(Session.getMarketFunds() + Good.getPrice());
            log.info("executing trade with " + buyer.getName() + " directly for " + amount + " share/s at a price of " + Good.getPrice());
        }


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
                int numberAvailable = ownedGood == null ? Good.getDirectlyAvailable() : ownedGood.getNumberOwned();
                int numberToBuy = Math.min(numberAvailable,numberCanAfford);
                goodsForSale.remove(goodToBuy); //TODO This should really lower the amount of the good, not remove it.
                return new Trade(buyer,seller,good,numberToBuy);
            }
        } catch (Exception e){
            log.severe("Casting error when converting good type during trade binding.");
        }
    }

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
    */
}
