package com.phototherapy;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;

public class SensorGraph {
public static void createOpticalGraph(
        double[] rawValues,
        double[] processedValues) throws IOException {

    XYSeries rawSeries = new XYSeries("Raw Optical Value");
    XYSeries processedSeries = new XYSeries("Processed Optical Value");

    int count = Math.min(rawValues.length, processedValues.length);

    for (int i = 0; i < count; i++) {
        rawSeries.add(i, rawValues[i]);
        processedSeries.add(i, processedValues[i]);
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(rawSeries);
    dataset.addSeries(processedSeries);

    JFreeChart chart = ChartFactory.createXYLineChart(
            "Phototherapy Optical Readings",
            "Reading Number",
            "Optical Value",
            dataset
    );

    File outputFile = new File("output/optical_readings.png");
    File parent = outputFile.getParentFile();

    if (parent != null) {
        parent.mkdirs();
    }

    ChartUtils.saveChartAsPNG(outputFile, chart, 1000, 600);

    System.out.println(
            "Optical graph saved to: "
                    + outputFile.getAbsolutePath()
    );
}


}
