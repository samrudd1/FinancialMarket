package altair;

import agent.Agent;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Map;

@Log
public class PythonCallAgent {
    private static final String COMMAND = "python altair_plot_agent.py";
    private int agentId;
    private Map<Integer,Float> fundData;

    public PythonCallAgent(Agent agent){
        this.agentId = agent.getId();
        this.fundData = agent.getFundData();
    }

    public void execute(){
        String dataString = fundData.toString().replace("{","").replace("}","").replace(" ","");

        try {
            Runtime.getRuntime().exec(COMMAND + " " + dataString + " " + agentId);
        } catch (IOException e) {
            log.severe("Error calling python charting script.");
        }
    }

}
