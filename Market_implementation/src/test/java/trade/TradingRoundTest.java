package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import session.Session;

import java.util.ArrayList;

public class TradingRoundTest {

    @BeforeAll
    static void setup(){
        Session.openSession();
    }

    @Test
    void tradingRoundTest(){
        for(int i = 0; i<1; i++){
            Session.getGoodsToDelete().add(new Good());
        }
        for(int i = 0; i<2; i++){
            Session.getAgentsToDelete().add(new Agent());
        }
        for(int i = 0; i<500; i++){
            new TradingRound().startTrading();
        }
        for(Agent agent : Session.getAgents().values()){
            agent.closeAccount();
        }
    }

    @AfterAll
    static void tearDown(){
        ArrayList<OwnedGood> test = Session.getOwnershipsToUpdate();
        Session.closeSession();
    }
}
