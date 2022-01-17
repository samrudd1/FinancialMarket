package good;

import agent.Agent;
import lombok.*;

import java.util.Comparator;

public class Offer implements Comparable<Offer> {
    @Getter @Setter private float price;
    @Getter private Agent offerMaker;
    @Getter private Good good;
    @Getter @Setter private int numOffered;

    public Offer(float price, Agent offerMaker, Good good, int numOffered) {
        this.price = price;
        this.offerMaker = offerMaker;
        this.good = good;
        this.numOffered = numOffered;
    }

    @Override
    public int compareTo(Offer o) {
        return Math.round((price - o.getPrice()) * 100);
    }
}
