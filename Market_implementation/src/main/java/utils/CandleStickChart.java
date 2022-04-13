package utils;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;

import org.jfree.data.xy.DefaultHighLowDataset;
import trade.Time;
import trade.TradeData;

/**
 * The Class JfreeCandlestickChart.
 *
 * @author ashraf
 */
@SuppressWarnings("serial")
public class CandleStickChart extends JPanel implements Runnable {

    private static final DateFormat READABLE_TIME_FORMAT = new SimpleDateFormat("kk:mm:ss");

    private OHLCSeries ohlcSeries;
    private TimeSeries volumeSeries;
    CandlestickRenderer candlestickRenderer;
    ArrayList<CandleData> candles;
    //final JFreeChart candlestickChart;
    DefaultHighLowDataset candlestickDataset;

    private static final int MIN = 60000;
    // Every minute
    private int timeInterval = 1;
    private double open = 0.0;
    private double close = 0.0;
    private double low = 0.0;
    private double high = 0.0;
    private long volume = 0;

    public CandleStickChart(String title, ArrayList<TradeData> trades) {
        // Create new chart
        // Create new chart panel
        //OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
        ohlcSeries = new OHLCSeries("Price");
        TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
        volumeSeries = new TimeSeries("Volume");
        addData(trades);
        volumeDataset.addSeries(volumeSeries);
        //candlestickDataset.addSeries(ohlcSeries);

        JFreeChart chart = ChartFactory.createCandlestickChart("Stock", "Round", "Price", candlestickDataset, false);

        // 3. Set chart background
        chart.setBackgroundPaint(Color.white);
        chart.setBorderVisible(false);

        // 4. Set a few custom plot features
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE); // light yellow = new Color(0xffffe0)
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);

        // 5. Skip week-ends on the date axis
        //((DateAxis) plot.getDomainAxis()).setTimeline();

        // 6. No volume drawn
        ((CandlestickRenderer) plot.getRenderer()).setDrawVolume(true);
        ((CandlestickRenderer) plot.getRenderer()).setVolumePaint(Color.blue);
        ((CandlestickRenderer) plot.getRenderer()).setUseOutlinePaint(false);
        ((CandlestickRenderer) plot.getRenderer()).setSeriesStroke(0, new BasicStroke(0.1f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        ((CandlestickRenderer) plot.getRenderer()).setSeriesPaint(0, Color.black);

        // 7. Create and display full-screen JFrame
        JFrame myFrame = new JFrame();
        myFrame.setResizable(true);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.add(new ChartPanel(chart), BorderLayout.CENTER);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Insets insets = kit.getScreenInsets(myFrame.getGraphicsConfiguration());
        Dimension screen = kit.getScreenSize();
        myFrame.setSize((int) (screen.getWidth() - insets.left - insets.right), (int) (screen.getHeight() - insets.top - insets.bottom));
        myFrame.setLocation((int) (insets.left), (int) (insets.top));
        myFrame.setVisible(true);

        /*
        DateAxis dateAxis = new DateAxis("Time");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
        // reduce the default left/right margin from 0.05 to 0.02
        dateAxis.setLowerMargin(0.02);
        dateAxis.setUpperMargin(0.02);
        // Create mainPlot
        CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
        mainPlot.setGap(10.0);
        mainPlot.add(candlestickSubplot, 3);
        mainPlot.add(volumeSubplot, 1);
        mainPlot.setOrientation(PlotOrientation.VERTICAL);
         */
    }

    /*
    private JFreeChart createChart(String chartTitle, ArrayList<TradeData> trades) {

        // 2. Create chart
        OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
        ohlcSeries = new OHLCSeries("Price");
        TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
        volumeSeries = new TimeSeries("Volume");
        addData(trades);
        volumeDataset.addSeries(volumeSeries);
        candlestickDataset.addSeries(ohlcSeries);

        JFreeChart chart = ChartFactory.createCandlestickChart("Stock", "Round", "Price", candlestickDataset, false);

        // 3. Set chart background
        chart.setBackgroundPaint(Color.white);

        // 4. Set a few custom plot features
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE); // light yellow = new Color(0xffffe0)
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false);

        // 5. Skip week-ends on the date axis
        //((DateAxis) plot.getDomainAxis()).setTimeline();

        // 6. No volume drawn
        ((CandlestickRenderer) plot.getRenderer()).setDrawVolume(true);

        // 7. Create and display full-screen JFrame
        JFrame myFrame = new JFrame();
        myFrame.setResizable(true);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.add(new ChartPanel(chart), BorderLayout.CENTER);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Insets insets = kit.getScreenInsets(myFrame.getGraphicsConfiguration());
        Dimension screen = kit.getScreenSize();
        myFrame.setSize((int) (screen.getWidth() - insets.left - insets.right), (int) (screen.getHeight() - insets.top - insets.bottom));
        myFrame.setLocation((int) (insets.left), (int) (insets.top));
        myFrame.setVisible(true);
        return candlestickChart;
    }

     */




/*
        // Create OHLCSeriesCollection as a price dataset for candlestick chart
        OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
        ohlcSeries = new OHLCSeries("Price");
        TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
        volumeSeries = new TimeSeries("Volume");
        addData(trades);
        volumeDataset.addSeries(volumeSeries);
        candlestickDataset.addSeries(ohlcSeries);
        // Create candlestick chart priceAxis
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        // Create candlestick chart renderer
        CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
        //CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE, false, new BoxAndWhiskerXYToolTipGenerator()), new DecimalFormat("0.000")));
        // Create candlestickSubplot
        XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
        candlestickSubplot.setBackgroundPaint(Color.white);

        // creates TimeSeriesCollection as a volume dataset for volume chart
        // Create volume chart volumeAxis
        NumberAxis volumeAxis = new NumberAxis("Volume");
        volumeAxis.setAutoRangeIncludesZero(false);
        // Set to no decimal
        volumeAxis.setNumberFormatOverride(new DecimalFormat("0"));
        // Create volume chart renderer
        XYBarRenderer timeRenderer = new XYBarRenderer();
        timeRenderer.setShadowVisible(false);
        timeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Time={1} Size={2}",
                new SimpleDateFormat("kk:mm"), new DecimalFormat("0")));
        // Create volumeSubplot
        XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
        volumeSubplot.setBackgroundPaint(Color.white);

        // Creating charts common dateAxis
        DateAxis dateAxis = new DateAxis("Time");
        dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
        // reduce the default left/right margin from 0.05 to 0.02
        dateAxis.setLowerMargin(0.02);
        dateAxis.setUpperMargin(0.02);
        // Create mainPlot
        CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
        mainPlot.setGap(10.0);
        mainPlot.add(candlestickSubplot, 3);
        mainPlot.add(volumeSubplot, 1);
        mainPlot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, mainPlot, true);
        chart.removeLegend();
        return chart;
    }
    */

    public void addCandle(long time, double o, double h, double l, double c, long v) {
        // Add bar to the data. Let's repeat the same bar
        Time t = new Time(time);
        ohlcSeries.add(t, o, h, l, c);
        volumeSeries.add(t, v);
    }

    public void addData(ArrayList<TradeData> trades) {
        trades.trimToSize();
        Collections.sort(trades);
        candles = CandleData.createCandles(trades);
        int maxCandle = candles.size() - 1;
        Date[] date = new Date[maxCandle];
        double[] high = new double[maxCandle];
        double[] low = new double[maxCandle];
        double[] open = new double[maxCandle];
        double[] close = new double[maxCandle];
        double[] volume = new double[maxCandle];

        for (int i = 0; i < candles.size() - 1; i++) {
            date[i] = new Date(candles.get(i).getRound());
            high[i] = candles.get(i).getHigh();
            low[i] = candles.get(i).getLow();
            open[i] = candles.get(i).getOpen();
            close[i] = candles.get(i).getClose();
            volume[i] = candles.get(i).getVolume();

        }
        candlestickDataset = new DefaultHighLowDataset("", date, high, low, open, close, volume);
        //for (CandleData candle: candles) {
            //Time t = new Time(candle.getRound());
            //ohlcSeries.add(t, candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose());
            //volumeSeries.add(new Time(candle.getRound()), candle.getVolume());
        //}
    }

    @Override
    public void run() {
        return;
    }
}