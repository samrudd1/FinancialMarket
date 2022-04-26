package trade;

import agent.Agent;
import good.Good;
import lombok.extern.java.Log;
import session.Session;
import utilities.SQLConnector;

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

        log.info("Please enter the number of trading rounds.");
        int noOfRounds = input.nextInt();

        boolean pass = false;
        while (!pass) {
            log.info("Would you like a live stock chart? [y/n]");
            char liveChart = Character.toLowerCase(input.next().charAt(0));
            if (liveChart == 'y') {
                Exchange.setLiveActive(true);
                pass = true;
            }
            if (liveChart == 'n') {
                Exchange.setLiveActive(false);
                pass = true;
            }
        }

        Session.setNumAgents(startingAgentNo);
        Good.setOutstandingShares(startingGoodNo);
        Good.setDirectlyAvailable(startingGoodNo);
        Session.openSession();

        Agent.setSentiment(20);
        Agent.ID = 1;
        Exchange.setSignalLogging(true); //RSI logging
        TradingCycle.setSignalLogging(true); //Sentiment logging

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
