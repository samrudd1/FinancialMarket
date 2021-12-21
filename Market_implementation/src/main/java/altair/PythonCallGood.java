package altair;

import good.Good;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Map;

@Log
public class PythonCallGood {
    private static final String COMMAND = "python altair_plot_goods.py";
    private int goodId;
    private Map<Integer,Float> fundData;

    public PythonCallGood(Good good){
        this.goodId = good.getId();
        this.fundData = good.getPriceData();
    }

    public void execute(){
        String dataString = fundData.toString().replace("{","").replace("}","").replace(" ","");

        try {
            Runtime.getRuntime().exec(COMMAND + " " + dataString + " " + goodId);
        } catch (IOException e) {
            log.severe("Error calling python charting script.");
        }
    }

}
