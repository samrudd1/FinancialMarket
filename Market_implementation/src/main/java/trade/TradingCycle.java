package trade;

import agent.Agent;
import good.Good;
import good.Offer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import session.Session;
import utils.LineChartMake;

import java.util.ArrayList;
import java.util.Random;

@Log
public class TradingCycle {
    private static final double SELLING_THRESHOLD = 0.9;
    private static final float INCREASE_MULTIPLIER = 1.01f;
    private static final float DECREASE_MULTIPLIER = 0.99f;
    private int numOfRounds;
    @Getter @Setter public static boolean roundComplete;
    @Getter @Setter public static boolean agentComplete;

    public void startTrading(int numOfRounds) throws Exception {
        this.numOfRounds = numOfRounds;
        Session.setNumOfRounds(Session.getNumOfRounds()+1);
        roundComplete = false;
        agentComplete = false;
        //ArrayList<OwnedGood> ownedGoodsForSale = getAgentOwnedGoods();
        synchronized (this) {
            initialOffering();
            Good.getPriceList().clear();
            Good.getAvgPriceList().clear();

            //Exchange.getLiveChart().setVisible(true);
            //Exchange.getLiveChart().toFront();

            Offer inOffer = Good.getAsk().get(0);
            inOffer.setPrice((float) (inOffer.getPrice() * 1.05));
            for (int i = 0; i < (numOfRounds - 1); i++) {
                //while (!roundComplete) wait();
                createTrades(i);
                //Thread.sleep(100);
                //liveC.run();
                //Thread.sleep(10);
            }
        }
        System.out.println();
        System.out.println("Market funds raised: " + Good.getCompany().getFunds());
        System.out.println("starting price: " + Good.getStartingPrice());
        System.out.println("lowest: " + Good.getLowest());
        System.out.println("highest: " + Good.getHighest());
        System.out.println("average price per share: " + Good.getVwap());
        System.out.println("total shares traded: " + Good.getVolume());

        Exchange.getInstance().getGoods().get(0).saveGood(false);
        analyzeStrategies();
        makeChart();
        System.out.println("Saving...");
        for (Agent agent : Session.getAgents().values()) {
            agent.saveUser(false);
        }
        System.out.println("Finished");
        //PythonCallGood pcg = new PythonCallGood(Exchange.getInstance().getGoods().get(0));
        //pcg.execute();
        //runChart();
        //Good.runUpdate(false);
        //LineChartMake lp = new LineChartMake(Good.getPriceList());
        //lp.setVisible(true);
    }

    private synchronized void initialOffering() throws InterruptedException {
        //This could be used as opening bell type auction, or IPO
        Offer IPO = Good.getAsk().get(0);
        agentComplete = true;
        roundComplete = false;
        log.info("There are " + IPO.getNumOffered() + " direct goods for sale at " + IPO.getPrice());
        int startingOffer = Good.getOutstandingShares();
        synchronized (this) {
            //for (Good good : Exchange.getInstance().getGoods()) {
            for (Agent agent : Session.getAgents().values()) {
                while (!agentComplete) wait();
                agentComplete = false;
                if ((IPO.getPrice() < agent.getTargetPrice()) && (IPO.getNumOffered() > 0)) {
                    int wantToBuy = (startingOffer / Session.getNumAgents());
                    if (wantToBuy > 0) {
                        if (IPO.getNumOffered() >= wantToBuy) {
                            agent.CheckInitial(wantToBuy, this);
                        } else if (IPO.getNumOffered() < wantToBuy) {
                            wantToBuy = IPO.getNumOffered();
                            agent.CheckInitial(wantToBuy, this);
                        }
                    } else {
                        agentComplete = true;
                    }
                } else {
                    agentComplete = true;
                }
                notifyAll();
            }
            roundComplete = true;
            notifyAll();
        }
    }

    private void createTrades(int roundNum) throws InterruptedException {
        //for (Good good : Exchange.getInstance().getGoods()) {
        roundComplete = false;
            for (Agent agent : Session.getAgents().values()) {
                if (agent.getId() != 1) {
                    if (agent.getChance() == 1) {
                        if (roundNum == 10) {
                            agent.setPrevSentiment(Agent.getSentiment());
                        } else if (roundNum > 10) {
                            SentimentSwing sw = new SentimentSwing(agent, this);
                            Runnable tw = new Thread(sw);
                            tw.run();
                        }
                    } else if (agent.getChance() == 2) {
                        if (roundNum > 10) {
                            MomentumSwing ms = new MomentumSwing(agent, this);
                            Runnable mt = new Thread(ms);
                            mt.run();
                        }
                    } else {
                        DefaultStrategy co = new DefaultStrategy(agent, this);
                        Runnable ts = new Thread(co);
                        ts.run();
                    }
                } else { agentComplete = true; }
            }
            mut();
            System.out.println();
            System.out.println("End of round " + (roundNum + 1) + " of " + numOfRounds);
            System.out.println("bid: " + Exchange.getInstance().getGoods().get(0).bidString());
            System.out.println("ask: " + Exchange.getInstance().getGoods().get(0).askString());
            if (Good.getBid().size() > 0) {
                System.out.println("highest bid: " + Exchange.getInstance().getGoods().get(0).getHighestBid());
            }
            if (Good.getAsk().size() > 1) {
                System.out.println("lowest ask: " + Exchange.getInstance().getGoods().get(0).getLowestAsk());
            }
            System.out.println("trades so far: " + (Good.getNumTrades() / 2));
            System.out.println("total volume: " + (int)Good.getVolume());
        //}
    }

    private void analyzeStrategies() {
        float defaultTotalNum = 0;
        float defaultAvgValue = 0;
        float defaultPercent = 0;
        float defaultStart = 0;
        float sentTotalNum = 0;
        float sentAvgValue = 0;
        float sentPercent = 0;
        float sentStart = 0;
        float momTotalNum = 0;
        float momAvgValue = 0;
        float momPercent = 0;
        float momStart = 0;
        for (Agent agent : Session.getAgents().values()) {
            if (agent.getChance() == 1) {
                sentAvgValue *= sentTotalNum;
                float value = agent.getFunds();
                if (agent.getGoodsOwned().size() > 0) {
                    value += (agent.getGoodsOwned().get(0).getNumOwned() * Exchange.lastPrice);
                }
                sentStart *= sentTotalNum;
                sentStart += agent.getStartingFunds();
                sentAvgValue += value;
                sentTotalNum += 1;
                sentStart = sentStart / sentTotalNum;
                sentAvgValue = sentAvgValue / sentTotalNum;
            } else if (agent.getChance() == 2) {
                momAvgValue *= momTotalNum;
                float value = agent.getFunds();
                if (agent.getGoodsOwned().size() > 0) {
                    value += (agent.getGoodsOwned().get(0).getNumOwned() * Exchange.lastPrice);
                }
                momStart *= momTotalNum;
                momStart += agent.getStartingFunds();
                momAvgValue += value;
                momTotalNum += 1;
                momStart = momStart / momTotalNum;
                momAvgValue = momAvgValue / momTotalNum;
            } else {
                defaultAvgValue *= defaultTotalNum;
                float value = agent.getFunds();
                if (agent.getGoodsOwned().size() > 0) {
                    value += (agent.getGoodsOwned().get(0).getNumOwned() * Exchange.lastPrice);
                }
                defaultStart *= defaultTotalNum;
                defaultStart += agent.getStartingFunds();
                defaultAvgValue += value;
                defaultTotalNum += 1;
                defaultStart = defaultStart / defaultTotalNum;
                defaultAvgValue = defaultAvgValue / defaultTotalNum;
            }
        }
        sentPercent = sentAvgValue / sentStart;
        sentPercent *= 100;
        sentPercent -= 100;
        momPercent = momAvgValue / momStart;
        momPercent *= 100;
        momPercent -= 100;
        defaultPercent = defaultAvgValue / defaultStart;
        defaultPercent *= 100;
        defaultPercent -= 100;
        System.out.println();
        System.out.println("Total number agents with default strategy: " + defaultTotalNum);
        //System.out.println("Average default strategy value: " + defaultAvgValue);
        System.out.println("Average percentage change for default strategy: " + defaultPercent + "%");
        System.out.println("Total number agents with sentiment strategy: " + sentTotalNum);
        //System.out.println("Average sentiment strategy value: " + sentAvgValue);
        System.out.println("Average percentage change for sentiment strategy: " + sentPercent + "%");
        System.out.println("Total number agents with momentum strategy: " + momTotalNum);
        //System.out.println("Average momentum strategy value: " + momAvgValue);
        System.out.println("Average percentage change for momentum strategy: " + momPercent + "%");
        System.out.println();
    }

    private void makeChart() {
        ArrayList<Float> avg10 = new ArrayList<>();
        float addNum = 0;
        float lowest = 9999;
        float highest = 0;
        for (int i = 0; i < Good.getAvgPriceList().size(); i++) {
            if (i % 10 == 9) {
                avg10.add((addNum / 10));
                addNum = 0;
            } else {
                addNum += Good.getAvgPriceList().get(i);
            }
        }
        for (Float aFloat : avg10) {
            if (aFloat < lowest) {
                lowest = aFloat;
            }
            if (aFloat > highest) {
                highest = aFloat;
            }
        }
        LineChartMake lc = new LineChartMake(avg10, lowest, highest);
        lc.setVisible(true);
    }

    private void mut() {
        Random rand = new Random();
        int chance = rand.nextInt(1000);
        if (chance > 900) {
            Agent.setSentiment(17);
        }
        if (chance > 950) {
            Agent.setSentiment(15);
            log.info("Adding new Agent to market.");
            new Agent();
            Session.setNumAgents(Session.getNumAgents() + 1);
        }
        if (chance > 995) {
            Agent.setSentiment(10);
            //for (Agent agent : Session.getAgents().values()) {
                //if (agent.getId() != 1) {
                //    agent.changeTargetPrice();
                //}
            //}
            //Agent.setSentiment(18);
        }
        if (chance < 100) {
            Agent.setSentiment(22);
        }
        if (chance < 50) {
            Agent.setSentiment(23);
        }
        if (chance < 5) {
            Agent.setSentiment(25);
            //for (Agent agent : Session.getAgents().values()) {
            //    if (agent.getId() != 1) {
            //        agent.changeTargetPrice();
            //        //agent.changeTargetPrice();
            //    }
            //}
        }
        /*
        if (chance == 400) {
            Runnable mu = new Thread(new ResetVol());
            mu.run();
            Agent.setSentiment(20);
        }
        */
        if ((chance > 400) && (chance < 450)) {
            Agent.setSentiment(18);
        }
        if ((chance > 450) && (chance < 500)) {
            Agent.setSentiment(19);
        }
        if ((chance > 500) && (chance < 550)) {
            Agent.setSentiment(20);
        }
        if ((chance > 550) && (chance < 600)) {
            Agent.setSentiment(21);
        }
    }

    //adds randomness to market, from original version
    private class ResetVol implements Runnable {
        public ResetVol() {}
        @Override
        public void run() {
            for (Agent agent : Session.getAgents().values()) {
                if (agent.getId() != 1) {
                    agent.createTargetPrice();
                }
                agent.setPlacedAsk(false);
                agent.setPlacedBid(false);
            }
        }
    }
}
