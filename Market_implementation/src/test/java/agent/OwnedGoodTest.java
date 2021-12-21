package agent;

import good.Good;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import session.Session;
import trade.Trade;
import trade.TradingRound;


public class OwnedGoodTest {

    @BeforeAll
    static void setup(){
        Session.openSession();
    }

    @Test
    void newOwnedGoodtest(){
        Agent agent1 = new Agent();
        Good good1 = new Good();
        Session.getAgentsToDelete().add(agent1);
        Session.getGoodsToDelete().add(good1);
        new TradingRound().startTrading();
        Trade trade = new Trade(agent1,good1,1);
        trade.execute();
        System.out.println("Trade executed.");
        new TradingRound().startTrading();
    }

    @AfterAll
    static void clearUp(){
        Session.closeSession();
    }

}
