package trade;

import agent.Agent;
import good.Good;
import lombok.extern.java.Log;
import session.Session;
import utils.SQLConnector;

import java.util.Scanner;

@Log
public class RunMarket {
    public static void main(String[] args) throws Exception {

        try(SQLConnector connector = new SQLConnector()){
            connector.runUpdate("DELETE FROM good;","marketdb");
            connector.runUpdate("DELETE FROM agent;","marketdb");
            connector.runUpdate("DELETE FROM ownership;","marketdb");
        } catch (Exception e) {
            log.info("Error clearing tables: " + e.getMessage());
        }

        Scanner input = new Scanner(System.in);

        log.info("Please enter the number of starting agents.");
        int startingAgentNo = input.nextInt();

        log.info("Please enter the number of starting goods.");
        int startingGoodNo = input.nextInt();

        log.info("please enter the number of trading rounds.");
        int noOfRounds = input.nextInt();

        Session.setNumAgents(startingAgentNo);
        Good.setOutstandingShares(startingGoodNo);
        Good.setDirectlyAvailable(startingGoodNo);
        Session.openSession();
        Agent.setSentiment(18);
        Agent.ID = 1;
        //Agent.setSentiment(9);
        //log.info("Stock starting price: " + Good.getStartingPrice());
        new Good(true);
        for(int i = 0; i<startingAgentNo; i++){
            new Agent();
        }
        new TradingCycle().startTrading(noOfRounds);

        /*
        for(Agent agent : Session.getAgents().values()){
            agent.closeAccount();
        }
        */
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.info("error trying to sleep");
        }
        Session.closeSession();
    }
}
