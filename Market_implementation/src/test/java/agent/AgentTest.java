package agent;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import session.Session;


class AgentTest {
    private static int currID = 1;

    @BeforeAll
    static void setup(){
        Session.openSession();
    }

    @Test
    void newAgentTest(){
        //create new agent
        Agent testAgent = new Agent();
        Session.getAgentsToDelete().add(testAgent);
        assertEquals("Agent" + testAgent.getId(),testAgent.getName());
        float funds = testAgent.getFunds();
        assertTrue(funds >= 1000 && funds <= 10000);
        assertTrue(testAgent.getGoodsOwned().isEmpty());
        currID++;
    }

    @Test
    void newNamedAgent(){
        Agent namedAgent = new Agent("dummy");
        Session.getAgentsToDelete().add(namedAgent);
        assertEquals("Dummy",namedAgent.getName());
        currID++;
    }

    @Test
    void idTest(){
        Agent agent1 = new Agent();
        Agent agent2 = new Agent();
        Session.getAgentsToDelete().add(agent1);
        Session.getAgentsToDelete().add(agent2);
        currID++;
        currID++;
        assertEquals("Agent" + agent1.getId(),agent1.getName());
        assertEquals("Agent" + agent2.getId(),agent2.getName());
    }

    @AfterAll
    static void clearUp(){
        Session.closeSession();
    }

}
