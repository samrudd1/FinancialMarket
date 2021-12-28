package agent;

import good.Good;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import session.Session;
import trade.InitialTrade;
import trade.Trade;
import trade.TradingCycle;


public class OwnedGoodTest {

    @BeforeAll
    static void setup(){
        Session.openSession();
    }

    @Test
    void newOwnedGoodtest(){
        Agent agent1 = new Agent();
        Good good1 = new Good(true);
        Session.getAgentsToDelete().add(agent1);
        Session.getGoodsToDelete().add(good1);
        new TradingCycle().startTrading(1);
        InitialTrade trade = new InitialTrade(agent1,good1,1, 1);
        Thread t1 = new Thread(trade);
        t1.start();
        System.out.println("Trade executed.");
    }

    @AfterAll
    static void clearUp(){
        Session.closeSession();
    }

}
