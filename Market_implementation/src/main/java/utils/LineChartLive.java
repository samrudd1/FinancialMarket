package utils;

import good.Good;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LineChartLive extends JFrame implements Runnable {
    boolean active = false;
    JFreeChart chart;
    ChartPanel chartPanel;
    float lowest;
    float highest;
    float dataLow;
    float dataHigh;
    final int CHARTSIZE = 1000;
    public LineChartLive() {
        initUI(Good.getPriceList());
    }

    @Override
    public synchronized void run() {
        if (!active) {
            active = true;
            lowest = Good.getLowest();
            highest = Good.getHighest();
            chart.getCategoryPlot().setDataset(createDataset(Good.getAvgPriceList()));
            chart.getCategoryPlot().getRangeAxis().setRange((lowest * 0.99), (highest * 1.01));
            active = false;
        }
        return;
    }

    private void initUI(ArrayList<Float> goodMap) {
        DefaultCategoryDataset dataset = createDataset(goodMap);
        chart = ChartFactory.createLineChart(
                "Stock price",
                "Trades",
                "Price ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        //chart.getCategoryPlot().getRangeAxis().setRange((lowest * 0.95), (highest * 1.05));
        chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chartPanel.setBackground(Color.white);
        add(chartPanel);

        pack();
        setTitle("Line chart");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private DefaultCategoryDataset createDataset(ArrayList<Float> goodMap) {
        DefaultCategoryDataset series = new DefaultCategoryDataset();
        goodMap.trimToSize();
        int startNum = 1;
        if (goodMap.size() > CHARTSIZE) {
            dataHigh = 1;
            dataLow = 10000;
            startNum = (goodMap.size() - CHARTSIZE);
            for (int i = startNum; i < goodMap.size(); i++) {
                series.addValue(goodMap.get(i - 1), "price", (Integer) i);
                if (goodMap.get(i - 1) > dataHigh) {
                    dataHigh = goodMap.get(i - 1);
                }
                if (goodMap.get(i - 1) < dataLow) {
                    dataLow = goodMap.get(i - 1);
                }
                //series.add(i, goodMap.get(i -1));
            }
            lowest = dataLow;
            highest = dataHigh;
        } else {
            for (int i = startNum; i < goodMap.size(); i++) {
                series.addValue(goodMap.get(i - 1), "price", (Integer) i);
            }
        }
        //var dataset = new XYSeriesCollection();
        //dataset.addSeries(series);
        return series;
    }
}