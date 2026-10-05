
package com.phototherapy;

import com.phototherapy.model.SensorReading;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Route("")
public class DashboardView extends HorizontalLayout {

    private static final double TARGET_OPTICAL_VALUE = 100.0;

    public DashboardView() {

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        getStyle().set("background", "#0B1220");
        getElement().setAttribute("theme", "dark");

        // Sidebar
        VerticalLayout sidebar = new VerticalLayout();

        sidebar.setWidth("230px");
        sidebar.setPadding(true);
        sidebar.setSpacing(true);

        sidebar.getStyle()
                .set("background", "#111827")
                .set("color", "#FFFFFF")
                .set("min-height", "100vh")
                .set("box-sizing", "border-box");

        H3 logo = new H3("NeoLight");

        logo.getStyle()
                .set("color", "#38BDF8")
                .set("margin-bottom", "0");

        Paragraph subtitle = new Paragraph("Phototherapy Monitor");

        subtitle.getStyle()
                .set("color", "#9CA3AF")
                .set("font-size", "12px");

        sidebar.add(
                logo,
                subtitle,
                new Paragraph("●  Dashboard"),
                new Paragraph("   Sensor Readings"),
                new Paragraph("   Anomaly Alerts"),
                new Paragraph("   Performance")
        );

        // Main content
        VerticalLayout content = new VerticalLayout();

        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);

        content.getStyle()
                .set("background", "#0B1220")
                .set("color", "#F9FAFB")
                .set("overflow", "auto")
                .set("box-sizing", "border-box");

        H1 title = new H1("Phototherapy Dashboard");

        title.getStyle().set("margin-bottom", "0");

        Paragraph description = new Paragraph(
                "Sensor readings, optical processing, "
                        + "anomaly detection and performance."
        );

        description.getStyle().set("color", "#9CA3AF");

        content.add(title, description);

        // Load CSV and build dashboard
        try {

            CsvDataSource dataSource = new CsvDataSource();

            List<SensorReading> readings =
                    dataSource.readReadings(
                            "data/simulated_readings.csv"
                    );

            if (readings.isEmpty()) {
                content.add(
                        new Paragraph(
                                "The CSV file contains no readings."
                        )
                );

                add(sidebar, content);
                setFlexGrow(1, content);
                return;
            }

            // Process optical readings
            OpticalProcessor processor = new OpticalProcessor(3);

            List<Double> processedValues =
                    processor.calculateMovingAverage(readings);

            // Calculate metrics
            PerformanceAnalyzer analyzer = new PerformanceAnalyzer();

            double meanOptical =
                    analyzer.calculateMean(readings);

            double meanAbsoluteError =
                    analyzer.calculateMeanAbsoluteError(
                            readings,
                            TARGET_OPTICAL_VALUE
                    );

            double temperatureSum = 0.0;

            for (SensorReading reading : readings) {
                temperatureSum += reading.getTemperature();
            }

            double meanTemperature =
                    temperatureSum / readings.size();

            // Detect anomalies
            AnomalyDetector detector = new AnomalyDetector(3, 2.0);

            List<Integer> anomalyIndexes =
                    detector.detectOpticalAnomalies(readings);

            // Metric cards
            HorizontalLayout metrics = new HorizontalLayout();

            metrics.setWidthFull();
            metrics.setSpacing(true);
            metrics.getStyle().set("flex-wrap", "wrap");

            metrics.add(
                    createMetricCard(
                            "Mean Optical Output",
                            String.format("%.2f", meanOptical),
                            "From CSV sensor readings"
                    ),
                    createMetricCard(
                            "Average Temperature",
                            String.format("%.2f", meanTemperature),
                            "CSV temperature readings"
                    ),
                    createMetricCard(
                            "Mean Absolute Error",
                            String.format("%.2f", meanAbsoluteError),
                            "Target: 100.00"
                    ),
                    createMetricCard(
                            "Anomalies",
                            String.valueOf(anomalyIndexes.size()),
                            "Detected in this dataset"
                    )
            );

            content.add(metrics);

            // Optical graph
            H3 chartHeading =
                    new H3("Optical Readings: Raw vs Processed");

            chartHeading.getStyle()
                    .set("margin-top", "16px")
                    .set("margin-bottom", "0");

            byte[] chartImage =
                    createOpticalChart(readings, processedValues);

            StreamResource chartResource = new StreamResource(
                    "optical-readings.png",
                    () -> new ByteArrayInputStream(chartImage)
            );

            Image graph = new Image(
                    chartResource,
                    "Graph comparing raw and processed optical readings"
            );

            graph.setWidthFull();

            graph.getStyle()
                    .set("max-width", "1100px")
                    .set("border-radius", "12px")
                    .set("background", "#FFFFFF");

            Div chartContainer = new Div(graph);

            chartContainer.setWidthFull();

            chartContainer.getStyle()
                    .set("background", "#111827")
                    .set("border", "1px solid #263244")
                    .set("border-radius", "12px")
                    .set("padding", "12px")
                    .set("box-sizing", "border-box");

            content.add(chartHeading, chartContainer);

            // Sensor readings table
            H3 tableHeading = new H3("Sensor Readings");

            Grid<SensorReading> grid =
                    new Grid<>(SensorReading.class, false);

            grid.addColumn(SensorReading::getTimestamp)
                    .setHeader("Timestamp")
                    .setAutoWidth(true);

            grid.addColumn(SensorReading::getTemperature)
                    .setHeader("Temperature")
                    .setAutoWidth(true);

            grid.addColumn(SensorReading::getOpticalValue)
                    .setHeader("Raw Optical Value")
                    .setAutoWidth(true);

            grid.addColumn(reading -> {
                int index = readings.indexOf(reading);

                if (index >= 0 && index < processedValues.size()) {
                    return String.format(
                            "%.2f",
                            processedValues.get(index)
                    );
                }

                return "N/A";
            }).setHeader("Processed Optical Value")
                    .setAutoWidth(true);

            grid.setItems(readings);
            grid.setWidthFull();
            grid.setHeight("300px");

            content.add(tableHeading, grid);

            // Anomaly details
            H3 anomalyHeading = new H3("Anomaly Alerts");

            VerticalLayout anomalyPanel = new VerticalLayout();

            anomalyPanel.setPadding(true);
            anomalyPanel.setSpacing(true);

            anomalyPanel.getStyle()
                    .set("background", "#111827")
                    .set("border", "1px solid #263244")
                    .set("border-radius", "12px");

            if (anomalyIndexes.isEmpty()) {

                Paragraph noAnomalies = new Paragraph(
                        "No optical anomalies detected in this dataset."
                );

                noAnomalies.getStyle().set("color", "#34D399");

                anomalyPanel.add(noAnomalies);

            } else {

                for (int index : anomalyIndexes) {

                    SensorReading reading = readings.get(index);

                    Paragraph alert = new Paragraph(
                            "Anomaly at timestamp "
                                    + reading.getTimestamp()
                                    + " — optical value: "
                                    + String.format(
                                            "%.2f",
                                            reading.getOpticalValue()
                                    )
                    );

                    alert.getStyle().set("color", "#FBBF24");

                    anomalyPanel.add(alert);
                }
            }

            content.add(anomalyHeading, anomalyPanel);

            // Demo status
            Paragraph status = new Paragraph(
                    "DEMO MODE — Data is loaded from a simulated CSV file. "
                            + "No physical medical device is connected."
            );

            status.getStyle()
                    .set("color", "#FBBF24")
                    .set("font-size", "12px");

            content.add(status);

        } catch (Exception e) {

            Paragraph error = new Paragraph(
                    "Unable to load dashboard data: " + e.getMessage()
            );

            error.getStyle().set("color", "#F87171");

            content.add(error);

            e.printStackTrace();
        }

        add(sidebar, content);
        setFlexGrow(1, content);
    }

    private VerticalLayout createMetricCard(
            String heading,
            String value,
            String description) {

        VerticalLayout card = new VerticalLayout();

        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("220px");

        card.getStyle()
                .set("background", "#111827")
                .set("border", "1px solid #263244")
                .set("border-radius", "12px")
                .set("box-sizing", "border-box");

        Paragraph label = new Paragraph(heading);

        label.getStyle()
                .set("color", "#9CA3AF")
                .set("font-size", "13px");

        H3 number = new H3(value);

        number.getStyle()
                .set("color", "#38BDF8")
                .set("margin", "4px 0");

        Paragraph detail = new Paragraph(description);

        detail.getStyle()
                .set("color", "#9CA3AF")
                .set("font-size", "11px");

        card.add(label, number, detail);

        return card;
    }

    private byte[] createOpticalChart(
            List<SensorReading> readings,
            List<Double> processedValues) throws IOException {

        XYSeries rawSeries = new XYSeries("Raw Optical Value");

        XYSeries processedSeries =
                new XYSeries("Processed Optical Value");

        int count = Math.min(
                readings.size(),
                processedValues.size()
        );

        for (int i = 0; i < count; i++) {

            SensorReading reading = readings.get(i);

            double timestamp = reading.getTimestamp();

            rawSeries.add(
                    timestamp,
                    reading.getOpticalValue()
            );

            processedSeries.add(
                    timestamp,
                    processedValues.get(i)
            );
        }

        XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(rawSeries);
        dataset.addSeries(processedSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Phototherapy Optical Readings",
                "Timestamp",
                "Optical Value",
                dataset
        );

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            // FIX: write the chart to an OutputStream, not a File.
            ChartUtils.writeChartAsPNG(
                    output,
                    chart,
                    1100,
                    450
            );

            return output.toByteArray();
        }
    }
}