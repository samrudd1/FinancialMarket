package trade;

import Strategies.*;
import agent.Agent;
import good.Good;
import good.Offer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import session.Session;
import utils.CandleStickChart;
import utils.LineChartMake;

import java.util.ArrayList;
import java.util.Random;

@Log
public class TradingCycle {
    @Getter private int numOfRounds;
    @Getter @Setter public static boolean agentComplete;
    @Getter @Setter private static boolean signalLogging;

    public void startTrading(int numOfRounds) throws Exception {
        this.numOfRounds = numOfRounds;
        Session.setNumOfRounds(Session.getNumOfRounds()+1);
        agentComplete = false;
        synchronized (this) {
            initialOffering();
            Good.getPriceList().clear();
            Good.getAvgPriceList().clear();

            //Exchange.getLiveChart().setVisible(true);
            //Exchange.getLiveChart().toFront();

            Offer inOffer = Good.getAsk().get(0);
            inOffer.setPrice((float) (inOffer.getPrice() * 1.05));
            System.out.println();
            System.out.println("Initial offering complete...");
            System.out.println("Starting trading between agents...");
            System.out.println();
            Thread.sleep(1000);

            for (int i = 0; i < numOfRounds; i++) {
                createTrades(i);
            }
        }

        System.out.println();
        System.out.println("Market funds raised: " + Good.getCompany().getFunds());
        System.out.println("Starting price: " + Good.getStartingPrice());
        System.out.println("Lowest price: " + Good.getLowest());
        System.out.println("Highest price: " + Good.getHighest());
        System.out.println("Average price per share: " + Good.getVwap());
        System.out.println("Total shares traded: " + Good.getVolume());

        Exchange.getInstance().getGoods().get(0).saveGood(false);
        analyzeStrategies();
        System.out.println("Saving...");
        if (signalLogging) {
            System.out.println();
            Exchange.printLog();
            System.out.println();
        }
        makeChart();
        for (Agent agent : Session.getAgents().values()) {
            agent.saveUser(true);
        }
        System.out.println("Finished.");
    }

    private synchronized void initialOffering() throws InterruptedException {
        //This could be used as opening bell type auction, or IPO
        Offer IPO = Good.getAsk().get(0);
        agentComplete = true;
        log.info("There are " + IPO.getNumOffered() + " direct goods for sale at " + IPO.getPrice());
        int startingOffer = Good.getOutstandingShares();
        //synchronized (this) {
            //for (Good good : Exchange.getInstance().getGoods()) {
            for (Agent agent : Session.getAgents().values()) {
                while (!agentComplete) wait();
                agentComplete = false;
                if ((IPO.getPrice() < agent.getTargetPrice()) && (IPO.getNumOffered() > 0)) {
                    int wantToBuy = (startingOffer / (Session.getNumAgents() + 1));
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
            notifyAll();
        //}
    }

    private void createTrades(int roundNum) throws InterruptedException {
        //for (Good good : Exchange.getInstance().getGoods()) {
            for (Agent agent : Session.getAgents().values()) {
                if (agent.getId() != 1) {

                    /*
                    switch(agent.getChance()) {
                        case 1:
                            if (roundNum > 10) {
                                MomentumSwing ms = new MomentumSwing(agent, this, roundNum);
                                Runnable mt = new Thread(ms);
                                mt.run();
                            }
                        case 2:
                            if (roundNum > 10) {
                                SentimentTrend st = new SentimentTrend(agent, this, roundNum);
                                Runnable tt = new Thread(st);
                                tt.run();
                            }
                        case 3:
                            if (roundNum > 16) {
                                RSI rs = new RSI(agent, this, roundNum);
                                Runnable rt = new Thread(rs);
                                rt.run();
                                if (roundNum > 160) {
                                    RSI10 rp = new RSI10(agent, this, roundNum);
                                    rt = new Thread(rp);
                                    rt.run();
                                }
                            }
                        case 4:
                            if (roundNum > 16) {
                                RSI rs = new RSI(agent, this, roundNum);
                                Runnable rt = new Thread(rs);
                                rt.run();
                            }
                        case 5:
                            if (roundNum > 160) {
                                RSI10 rp = new RSI10(agent, this, roundNum);
                                Runnable pr = new Thread(rp);
                                pr.run();
                            }
                        default:
                            DefaultStrategy co = new DefaultStrategy(agent, this, roundNum);
                            Runnable ts = new Thread(co);
                            ts.run();
                    }
                    */


                    if (agent.getChance() == 1) {
                        if (roundNum > 10) {
                            MomentumSwing ms = new MomentumSwing(agent, this, roundNum);
                            Runnable mt = new Thread(ms);
                            mt.run();
                        }
                    } else if (agent.getChance() == 2) {
                        if (roundNum > 10) {
                            SentimentTrend st = new SentimentTrend(agent, this, roundNum);
                            Runnable tt = new Thread(st);
                            tt.run();
                        }
                    } else if (agent.getChance() == 3) {
                        if (roundNum > 16) {
                            RSI br = new RSI(agent, this, roundNum);
                            Runnable rt = new Thread(br);
                            rt.run();
                            if (roundNum > 160) {
                                RSI10 rp = new RSI10(agent, this, roundNum);
                                Runnable rb = new Thread(rp);
                                rb.run();
                            }
                        }
                    } else if (agent.getChance() == 4) {
                        if (roundNum > 16) {
                            RSI rs = new RSI(agent, this, roundNum);
                            Runnable sr = new Thread(rs);
                            sr.run();
                        }
                    } else if (agent.getChance() == 5) {
                        if (roundNum > 160) {
                            RSI10 te = new RSI10(agent, this, roundNum);
                            Runnable fi = new Thread(te);
                            fi.run();
                        }
                        /* high frequency does produce slight profit, but always tiny and suffers in down market, also makes trading for others a lot worse
                    } else if (agent.getChance() == 3) {
                            HighFrequency hf = new HighFrequency(agent, this, roundNum, numOfRounds);
                            Runnable ht = new Thread(hf);
                            ht.run();
                        */
                    } else {
                        DefaultStrategy co = new DefaultStrategy(agent, this, roundNum);
                        Runnable ce = new Thread(co);
                        ce.run();
                    }

                } else {
                    agentComplete = true;
                    Good good = Exchange.getInstance().getGoods().get(0);
                    if ((Good.getBid().size() > 0) && (Good.getAsk().size() > 0)) {
                        if (!((good.getLowestAsk() < (Exchange.lastPrice * 1.1)) && (good.getLowestAsk() > (Exchange.lastPrice * 0.9)))) {
                            if (!((good.getHighestBid() < (Exchange.lastPrice * 1.1)) && (good.getHighestBid() > (Exchange.lastPrice * 0.9)))) {
                                float middle = good.getLowestAsk() - good.getHighestBid();
                                Exchange.lastPrice = (float) ((float) (Math.floor((good.getHighestBid() + (middle * 0.5)) * 100)) * 0.01);
                            }
                        }
                    }
                }
            }
            mut();
            System.out.println("Round " + (roundNum + 1) + " of " + numOfRounds);
            System.out.println("Bid: " + Exchange.getInstance().getGoods().get(0).bidString());
            System.out.println("Ask: " + Exchange.getInstance().getGoods().get(0).askString());
            if (Good.getBid().size() > 0) {
                System.out.println("Highest bid: " + Exchange.getInstance().getGoods().get(0).getHighestBid());
            }
            if (Good.getAsk().size() > 0) {
                System.out.println("Lowest ask: " + Exchange.getInstance().getGoods().get(0).getLowestAsk());
            }
            System.out.println("Trades so far: " + (Good.getNumTrades()));
            System.out.println("Total shares traded: " + (int)Good.getVolume());
            System.out.println("Overall sentiment: " + Agent.getSentiment());
            ///*
            if (Exchange.getRsi() > 0) {
                System.out.println("RSI  : " + Exchange.getRsi());
            }
            if (Exchange.getRsiP() > 0) {
                System.out.println("RSI 5: " + Exchange.getRsiP());
            }
            System.out.println();
             //*/
        //}
    }

    private void analyzeStrategies() {
        analysis("default", 0, Exchange.getDefaultCount());
        analysis("default(2)", 6, Exchange.getDefaultCount());
        analysis("momentum", 1, Exchange.getMomCount());
        analysis("sentiment", 2, Exchange.getSentCount());
        //analysis("high frequency", 3, Exchange.getHighCount());
        analysis("Both RSI", 3, Exchange.getHighCount());
        analysis("RSI", 4, Exchange.getRsiCount());
        analysis("RSIext", 5, Exchange.getRsi10Count());
        System.out.println();
    }
    private void analysis(String name, int id, float count) {
        float TotalNum = 0;
        float AvgValue = 0;
        float Percent;
        float Start = 0;
        for (Agent agent : Session.getAgents().values()) {
            if (agent.getChance() == id) {
                AvgValue *= TotalNum;
                float value = agent.getFunds();
                if (agent.getGoodsOwned().size() > 0) {
                    value += (agent.getGoodsOwned().get(0).getNumOwned() * Exchange.lastPrice);
                }
                Start *= TotalNum;
                Start += agent.getStartingFunds();
                AvgValue += value;
                TotalNum += 1;
                Start = Start / TotalNum;
                AvgValue = AvgValue / TotalNum;
            }
        }
        Percent = AvgValue / Start;
        Percent *= 100;
        Percent -= 100;
        System.out.println();
        System.out.println("Total number agents with " + name + " strategy: " + TotalNum);
        //System.out.println("Average " + name + " strategy value: " + defaultAvgValue);
        System.out.println("Average percentage change for " + name + " strategy: " + Percent + "%");
        System.out.println("Total trades by agents with " + name + " strategy: " + count);
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
        LineChartMake rs = new LineChartMake(Exchange.getRsiList(), "RSI");
        rs.setVisible(true);
        LineChartMake rp = new LineChartMake(Exchange.getRsiPList(), "RSI 5");
        rp.setVisible(true);
        CandleStickChart cs = new CandleStickChart("Candle Stick Chart", Good.getTradeData());
        cs.setVisible(true);
    }

    private void mut() {
        Random rand = new Random();
        int chance = rand.nextInt(1000);
        if (chance > 900) {
            Agent.setSentiment(17);
        }
        if (chance > 950) {
            Agent.setSentiment(16);
        }
        if (chance > 995) {
            Agent.setSentiment(13);
            if (signalLogging) {
                Exchange.negLog();
            }
        }
        if (chance < 100) {
            Agent.setSentiment(22);
        }
        if (chance < 50) {
            Agent.setSentiment(23);
        }
        if (chance < 5) {
            Agent.setSentiment(25);
            if (signalLogging) {
                Exchange.posLog();
            }
        }
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
        if (chance < 2) {
            log.info("Adding new Agent to market.");
            new Agent();
            Session.setNumAgents(Session.getNumAgents() + 1);
        }
    }
}
