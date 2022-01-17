package utils;
import lombok.var;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Map;

public class LineChartMake extends JFrame {

    public LineChartMake(ArrayList<Float> goodMap) {
        initUI(goodMap);
    }

    private void initUI(ArrayList<Float> goodMap) {

        DefaultCategoryDataset dataset = createDataset(goodMap);
        //JFreeChart chart = createChart(dataset);
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
        for (int i = 1; i < goodMap.size(); i ++) {
            series.addValue(goodMap.get(i-1), "price", (Integer)i);
            //series.add(i, goodMap.get(i -1));
        }
        //var dataset = new XYSeriesCollection();
        //dataset.addSeries(series);
        return series;
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset) {

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

        //CategoryPlot plot = chart.getCategoryPlot();

        //AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
        //renderer.setEndType(AreaRendererEndType.LEVEL);

        //plot.setRenderer(renderer);
        //plot.setBackgroundPaint(Color.white);

        //plot.setRangeGridlinesVisible(true);
        //plot.setRangeGridlinePaint(Color.BLACK);

        //plot.setDomainGridlinesVisible(true);
        //plot.setDomainGridlinePaint(Color.BLACK);

        //chart.getLegend().setFrame(BlockBorder.NONE);

        chart.setTitle(new TextTitle("Stock Price",
                        new Font("Serif", java.awt.Font.BOLD, 18)
                )
        );

        return chart;
    }

    /*
    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            var ex = new LineChartEx();
            ex.setVisible(true);
        });
    }
    */
}