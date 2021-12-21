package trade;

import agent.Agent;
import good.Good;
import lombok.extern.java.Log;
import session.Session;

import java.util.Scanner;

@Log
public class RunMarket {
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);

        log.info("Please enter the number of starting agents.");
        int startingAgentNo = input.nextInt();

        log.info("Please enter the number of starting goods.");
        int startingGoodNo = input.nextInt();

        log.info("please enter the number of trading rounds.");
        int noOfRounds = input.nextInt();

        log.info("Do you want to keep the data after session close? [y/n]");
        String keepDataString = "n";
        boolean keepData = false;
        if (input.hasNext()) {
            keepDataString = input.nextLine();
            keepData = keepDataString.equalsIgnoreCase("y");
        }

            Session.openSession();
            if(!keepData){
                for(int i = 0; i<startingAgentNo; i++){
                    Session.getAgentsToDelete().add(new Agent());
                }
                for(int i = 0; i<startingGoodNo; i++){
                    Session.getGoodsToDelete().add(new Good());
                }
                for(int i = 0; i<noOfRounds; i++){
                    new TradingRound().startTrading();
                }
            } else {
                for(int i = 0; i<startingAgentNo; i++){
                    new Agent();
                }
                for(int i = 0; i<startingGoodNo; i++){
                   new Good();
                }
                for(int i = 0; i<noOfRounds; i++){
                    new TradingRound().startTrading();
                }
            }

            for(Agent agent : Session.getAgents().values()){
                agent.closeAccount();
            }
            Session.closeSession();
    }
}
