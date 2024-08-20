package com.rbcsystem.demo.models;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;

public class Plot extends JFrame {

    public Plot(Color color, String title, String xAxisLabel, String yAxisLabel, double[] points, double xAxisLowerBound, double xAxisUpperBound, double yAxisLowerBound, double yAxisUpperBound) {
        super(title);

        // Create dataset
        XYSeriesCollection dataset = createDataset(title, points);

        // Create chart
        JFreeChart chart = ChartFactory.createScatterPlot(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Set rendering hints to improve text clarity
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Customize plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);

        // Customize gridlines
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);

        // Customize renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);

        // Customize point shape and color
        renderer.setSeriesPaint(0, color); // Semi-transparent red
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8)); // Circle with radius 4

        // Customize axes
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(xAxisLowerBound, xAxisUpperBound);
        xAxis.setTickUnit(new NumberTickUnit(5));
        xAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        xAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(yAxisLowerBound, yAxisUpperBound);
        yAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        yAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        // Customize chart title
        chart.setTitle(new TextTitle(title, new Font("SansSerif", Font.BOLD, 16)));

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        setContentPane(panel);
    }

    private XYSeriesCollection createDataset(String title, double[] points) {
        XYSeries series = new XYSeries(title);

        for (int i = 0; i < points.length; i++) {
            series.add(i, points[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    public void setPlotParms(Plot plot) {
        plot.setSize(800, 400);
        plot.setLocationRelativeTo(null);
        plot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        plot.setVisible(true);
    }
    public static void main(String[] args) {
        double[] val = new double[]{2.3, 3.4, 0.7, 34.9};
        
    }
}