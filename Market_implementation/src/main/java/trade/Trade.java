package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import lombok.Getter;
import lombok.ToString;
import session.Session;

import java.util.ArrayList;
import java.util.logging.Logger;

@ToString
public class Trade {
    private static final Logger LOGGER = Logger.getLogger(Trade.class.getName());
    @Getter private Agent buyer;
    private Agent seller;
    @Getter private Good good;
    @Getter private int amountBought;

    /**
     * This constructor is used for executing trades between two agents
     * @param buyer the Agent buying the ownedGood
     * @param seller the Agent selling the ownedGood
     * @param good the Good being sold
     * @param amountBought the amount of that ownedGood being sold
     */
    public Trade(Agent buyer, Agent seller, Good good, int amountBought){
        this.buyer = buyer;
        this.seller = seller;
        this.good = good;
        this.amountBought = amountBought;
    }

    /**
     * This constructor will be used when a ownedGood is being initially sold into the market, the ownedGood will be sold by the Good object itself
     * @param buyer the Agent buying the ownedGood
     * @param good The Good being offered
     * @param amountBought The number of the ownedGood being sold
     */
    public Trade(Agent buyer, Good good, int amountBought){
        this.buyer = buyer;
        this.good = good;
        this.amountBought = amountBought;
    }

    /**
     * This takes the trade object and creates an OwnedGood.
     * @return an indicator as to whether the execution was a success.
     */
    public boolean execute(){
        boolean didExecute = true;
        try{
            float boughtAt = good.getPrice();
            String ownedGoodKey = buyer.getId() + "-" + good.getId() + "-" + boughtAt;
            OwnedGood trade;
            if(Session.getOwnerships().containsKey(ownedGoodKey)){
                int numberOwned = Session.getOwnerships().get(ownedGoodKey).getNumberOwned();
                trade = new OwnedGood(buyer,good,numberOwned + amountBought,boughtAt,true);
                trade.save(false);
            } else {
                trade = new OwnedGood(buyer,good,amountBought,boughtAt,false);
            }
            buyer.getGoodsOwned().add(trade);
            //if the seller is null then the ownedGood has been bought directly.
            if(seller == null){
                //edit the ownedGood
                int numberOfGoodLeft = good.getAmountUnsold()-amountBought;
                good.setAmountUnsold(numberOfGoodLeft);
                good.saveGood(false);
            } else {
                //edit the sellers funds
                //here we assume we are selling the one that was bought cheapest if multiple are owned
                OwnedGood sellersGood = getCheapestOwnedGood(seller, good);
                int numberLeft = sellersGood.getNumberOwned() - amountBought;
                if(numberLeft > 0){
                    sellersGood.setNumberOwned(numberLeft);
                    sellersGood.save(false);
                } else {
                    Session.getOwnershipsToDelete().add(sellersGood);
                    Session.getOwnerships().remove(seller.getId() + "-" + good.getId() + "-" + sellersGood.getBoughtAt());
                }
                float newFunds = seller.getFunds() + (good.getPrice() * amountBought);
                seller.setFunds(newFunds);
                seller.saveUser(false);
            }
            //edit the buyers funds
            float amountSpent = amountBought*boughtAt;
            float amountLeft = buyer.getFunds()-amountSpent;
            buyer.setFunds(amountLeft);
            buyer.saveUser(false);
        } catch (Exception e){
            LOGGER.info("Error executing trade: " + e.getMessage());
            e.printStackTrace();
            didExecute = false;
        }
        return didExecute;
    }

    private OwnedGood getCheapestOwnedGood(Agent agent, Good good){
        ArrayList<String> matchingKeys = new ArrayList<>();
        for(String key : Session.getOwnerships().keySet()){
            String[] keyParts = key.split("-");
            if(keyParts[0].equals(String.valueOf(agent.getId())) && keyParts[1].equals(String.valueOf(good.getId()))){
                matchingKeys.add(key);
            }
        }
        float lowestBoughtAt = -1;
        for(String key : matchingKeys){
            float boughtAt = Float.parseFloat(key.split("-")[2]);
            if(lowestBoughtAt == -1){
                lowestBoughtAt = boughtAt;
            } else if (boughtAt < lowestBoughtAt) lowestBoughtAt = boughtAt;
        }
        String finalKey = agent.getId() + "-" + good.getId() + "-" + lowestBoughtAt;
        return Session.getOwnerships().get(finalKey);
    }

}
