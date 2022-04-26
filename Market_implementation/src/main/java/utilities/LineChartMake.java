package utilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LineChartMake extends JFrame {
    float lowest;
    float highest;

    public LineChartMake(ArrayList<Float> goodMap, float lowest, float highest) {
        this.lowest = lowest;
        this.highest = highest;
        initUI(goodMap);
    }
    public LineChartMake(ArrayList<Float> goodMap) {
        this.lowest = 0;
        this.highest = 100;
        initUI(goodMap);
    }
    public LineChartMake(ArrayList<Float> goodMap, String title) {
        this.lowest = 0;
        this.highest = 100;
        DefaultCategoryDataset dataset = createDataset(goodMap);
        JFreeChart chart = ChartFactory.createLineChart(
                title,
                "Trades",
                "Price ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.getCategoryPlot().getRangeAxis().setRange((lowest * 0.99), (highest * 1.01));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        add(chartPanel);

        pack();
        setTitle("Line chart");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUI(ArrayList<Float> goodMap) {
        DefaultCategoryDataset dataset = createDataset(goodMap);
        JFreeChart chart = ChartFactory.createLineChart(
                "Stock price",
                "Trades",
                "Price ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.getCategoryPlot().getRangeAxis().setRange((lowest * 0.99), (highest * 1.01));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        add(chartPanel);

        pack();
        setTitle("Line chart");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private DefaultCategoryDataset createDataset(ArrayList<Float> goodMap) {
        DefaultCategoryDataset series = new DefaultCategoryDataset();
        for (int i = 1; i < goodMap.size(); i++) {
            series.addValue(goodMap.get(i-1), "price", (Integer)i);
            //series.add(i, goodMap.get(i -1));
        }
        //var dataset = new XYSeriesCollection();
        //dataset.addSeries(series);
        return series;
    }
}