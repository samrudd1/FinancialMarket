package trade;

import agent.Agent;
import agent.OwnedGood;
import good.Good;
import good.Offer;
import lombok.Getter;
import lombok.Setter;
import utilities.LineChartLive;

import java.util.ArrayList;

public class Exchange {
    private static final Exchange exchange = new Exchange();
    @Getter private ArrayList<Good> goods = new ArrayList<Good>();
    public Exchange() {}
    public static Exchange getInstance() { return exchange; }
    private static boolean exchangeLock = false;
    public void addGood(Good good) {
        goods.add(good);
    }
    @Getter private ArrayList<Float> avg20 = new ArrayList<Float>() ;
    private float lastAvg = 0;
    @Getter @Setter private float priceCheck;
    @Getter private static LineChartLive liveChart = new LineChartLive();
    @Getter @Setter private static boolean liveActive = false;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static float lastPrice;
    @Getter private static float defaultCount = 0;
    @Getter private static float sentCount = 0;
    @Getter private static float momCount = 0;
    @Getter private static float highCount = 0;
    @Getter private static float rsiCount = 0;
    @Getter private static float rsi10Count = 0;
    @Getter private static float offerCount = 0;
    @Getter private static int round = 0;
    private static boolean newRound = false;
    @Getter private static ArrayList<Float> roundFinalPrice = new ArrayList<>();
    @Getter private static float roundPrice = 0;
    @Getter private static ArrayList<Float> rsiList = new ArrayList<>();
    @Getter private static float rsi = 0;
    @Getter private static ArrayList<Float> rsiPList = new ArrayList<>();
    @Getter private static float rsiP = 0;
    @Getter private static ArrayList<String> signalLog = new ArrayList<>();
    @Getter @Setter private static boolean signalLogging;

    public synchronized boolean execute(Agent buyer, Agent seller, Offer offer, int amountBought, TradingCycle tc, int roundNum) throws InterruptedException {
        boolean complete = true;
        if (buyer == seller) {
            TradingCycle.setAgentComplete(true);
            tc.notifyAll();
            return false;
        } else {
            while (exchangeLock) wait();
            exchangeLock = true;
            complete = true;

            if (lastAvg > 0) {
                priceCheck = lastAvg;
            } else {
                priceCheck = lastPrice;
            }
            if (Good.getBid().contains(offer) || Good.getAsk().contains(offer)) {
                if ((offer.getPrice() < (priceCheck * 1.05)) && (offer.getPrice() > (priceCheck * 0.96))) {
                    if (offer.getNumOffered() > 0) {

                        try {
                            if (offer.getNumOffered() < amountBought) {
                                amountBought = offer.getNumOffered();
                            }
                            if ((buyer.getGoodsOwned().isEmpty())) {
                                buyer.getGoodsOwned().add(0, new OwnedGood(buyer, offer.getGood(), amountBought, amountBought, (((float) Math.round(offer.getPrice() * 100)) / 100), true));
                            } else {
                                OwnedGood tempOwned = buyer.getGoodsOwned().get(0);
                                float newBoughtAt = (float) Math.round(((tempOwned.getBoughtAt() * tempOwned.getNumOwned()) + (amountBought * offer.getPrice())) * 100) / ((tempOwned.getNumOwned() + amountBought) * 100);
                                int newNumOwned = (tempOwned.getNumOwned() + amountBought);
                                int newAvailable = (tempOwned.getNumAvailable() + amountBought);
                                OwnedGood newOne = new OwnedGood(buyer, offer.getGood(), newNumOwned, newAvailable, newBoughtAt, false);
                                buyer.getGoodsOwned().set(0, newOne);
                            }
                            offer.getGood().setPrice(offer, amountBought);
                            if (amountBought == offer.getNumOffered()) {
                                if (Good.getAsk().contains(offer)) {
                                    offer.getGood().removeAsk(offer);
                                    offer.setNumOffered(0);
                                } else {
                                    offer.getGood().removeBid(offer);
                                    offer.setNumOffered(0);
                                }
                            } else {
                                offer.setNumOffered(offer.getNumOffered() - amountBought);
                            }
                            buyer.setFunds(buyer.getFunds() - (offer.getPrice() * amountBought));
                            seller.setFunds(seller.getFunds() + (offer.getPrice() * amountBought));
                        } catch (Exception e) {
                            complete = false;
                        }
                    } else {
                        if (Good.getBid().contains(offer)) {
                            offer.getGood().removeBid(offer);
                        } else {
                            offer.getGood().removeAsk(offer);
                        }
                    }
                    if (roundNum > round) {
                        roundFinalPrice.add(lastPrice);
                        roundPrice = lastPrice;
                        newRound = true;
                    }
                    round = roundNum;
                    lastPrice = offer.getPrice();
                }
                TradingCycle.setAgentComplete(true);
                exchangeLock = false;
                notifyAll();
                tc.notifyAll();

                Good.addTradeData(offer.getPrice(), amountBought, roundNum);
                //Good.addTradeData(offer.getPrice(), amountBought, ((int)(Math.floor(roundNum * 0.01)+2)));
                //Good.addTradeData(offer.getPrice(), amountBought, (int) Math.floor(Good.getNumTrades() * 0.01));

                if (avg20.size() < 20) {
                    avg20.add(offer.getPrice());
                } else {
                    avg20.remove(0);
                    avg20.add(offer.getPrice());
                    float avgResult = 0;
                    float total = 0;
                    for (float num : avg20) {
                        avgResult += num;
                        total++;
                    }
                    avgResult = avgResult / total;
                    lastAvg = avgResult;
                    Good.getAvgPriceList().add(avgResult);
                }

                //RSI
                if ((roundFinalPrice.size() > 15) && (newRound)) {
                    float avgGain = 0;
                    float avgLoss = 0;
                    float diff;
                    for (int i = (roundFinalPrice.size() - 1); i >= (roundFinalPrice.size() - 14); i--) {
                        diff = (roundFinalPrice.get(i) / roundFinalPrice.get(i - 1)) - 1;
                        if (diff > 0) {
                            avgGain += diff;
                        } else if (diff < 0) {
                            avgLoss += (diff * -1);
                        }
                    }
                    rsi = 100 - (100 / (1 + (avgGain / avgLoss)));
                    rsiList.add(rsi);

                    if (signalLogging) {
                        if (rsi > 80) {
                            signalLog.add(ANSI_RED + "Round " + roundNum + ": RSI Over-Bought: " + rsi + ", Sentiment: " + Agent.getSentiment() + ANSI_RESET);
                        } else if (rsi < 20) {
                            signalLog.add(ANSI_GREEN + "Round " + roundNum + ": RSI Over-Sold: " + rsi + ", Sentiment: " + Agent.getSentiment() + ANSI_RESET);
                        }
                    }

                    if (roundFinalPrice.size() > 150) {
                        avgGain = 0;
                        avgLoss = 0;
                        for (int i = (roundFinalPrice.size() - 1); i >= (roundFinalPrice.size() - 140); i-=10) {
                            diff = (roundFinalPrice.get(i) / roundFinalPrice.get(i - 10)) - 1;
                            if (diff > 0) {
                                avgGain += diff;
                            } else if (diff < 0) {
                                avgLoss += (diff * -1);
                            }
                        }
                        rsiP = 100 - (100 / (1 + (avgGain / avgLoss)));
                        rsiPList.add(rsiP);

                        if (signalLogging) {
                            if (rsiP > 70) {
                                signalLog.add(ANSI_RED + "Round " + roundNum + ": RSI 10 Over-Bought: " + rsiP + ", Sentiment: " + Agent.getSentiment() + ANSI_RESET);
                            } else if (rsiP < 30) {
                                signalLog.add(ANSI_GREEN + "Round " + roundNum + ": RSI 10 Over-Sold: " + rsiP + ", Sentiment: " + Agent.getSentiment() + ANSI_RESET);
                            }
                        }
                    }
                    newRound = false;
                }

                /*
                if (complete) {
                    if (!buyer.getName().equalsIgnoreCase(seller.getName())) {
                        if (offer.getPrice() > Good.getPrevPrice()) {
                            System.out.println(ANSI_GREEN + "trade executed between " + buyer.getName() + " and "
                                    + seller.getName() + " for " + amountBought + " share/s at a price of "
                                    + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                    + offer.getNumOffered() + " left on the offer." + ANSI_RESET);
                        } else if (offer.getPrice() < Good.getPrevPrice()) {
                            System.out.println(ANSI_RED + "trade executed between " + buyer.getName() + " and "
                                    + seller.getName() + " for " + amountBought + " share/s at a price of "
                                    + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                    + offer.getNumOffered() + " left on the offer." + ANSI_RESET);
                        } else {
                            System.out.println("trade executed between " + buyer.getName() + " and "
                                    + seller.getName() + " for " + amountBought + " share/s at a price of "
                                    + ((float) Math.round(offer.getPrice() * 100) / 100) + " each. There are "
                                    + offer.getNumOffered() + " left on the offer." + ANSI_RESET);
                        }
                    }
                }
                */

                tradeTally(buyer.getChance());
                tradeTally(seller.getChance());

                //buyer.saveUser(false);
                //seller.saveUser(false);
            } else {
                TradingCycle.setAgentComplete(true);
                exchangeLock = false;
                notifyAll();
                tc.notifyAll();
            }
        }
        if (liveActive) {
            if (Good.getNumTrades() % 10 == 0) {
                Runnable lc = new Thread(liveChart);
                lc.run();
            }
        }
        return complete;
    }

    public static void addLog(String log) { signalLog.add(log); }
    public static void printLog() {
        for (int i = 0; i < signalLog.size() - 1; i++) {
            System.out.println(signalLog.get(i));
        }
    }
    private void tradeTally(int chance) {
        if (chance == 1) {
            momCount += 1;
        } else if (chance == 2) {
            sentCount += 1;
        } else if (chance == 3) {
            highCount += 1;
        } else if (chance == 4) {
            rsiCount += 1;
        } else if (chance == 5) {
            rsi10Count += 1;
        } else if (chance == 6) {
            offerCount += 1;
        } else {
            defaultCount += 1;
        }
    }
}
