package altair;

import agent.Agent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import session.Session;

public class PythonTest {

    @BeforeAll
    static void setup(){
        Session.openSession();
    }

    @Test
    void chartTest(){
        Agent agent = new Agent();
        Session.getAgentsToDelete().add(agent);
        agent.setFunds(2000);
        agent.setFunds(1800);
        agent.setFunds(500);
        agent.setFunds(100);
        agent.setFunds(1500);
        agent.setFunds(1800);
        agent.setFunds(200);
        agent.setFunds(50);
        agent.setFunds(2500);

        PythonCallAgent call = new PythonCallAgent(agent);
        call.execute();
    }

    @AfterAll
    static void clearUp(){
        Session.closeSession();
    }
}
