package com.phototherapy;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route("")
public class DashboardView extends HorizontalLayout {

    private static final double TARGET_OPTICAL_VALUE = 100.0;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public DashboardView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        getStyle()
                .set("background", "#0B1220")
                .set("color", "#F9FAFB");

        getElement().setAttribute("theme", "dark");

        // SIDEBAR
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

        // MAIN CONTENT
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

        // REFRESH TOOLBAR
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(
                FlexComponent.JustifyContentMode.BETWEEN
        );

        Paragraph lastUpdated = new Paragraph(
                "Last refreshed: "
                        + LocalDateTime.now().format(TIME_FORMAT)
        );
        lastUpdated.getStyle()
                .set("color", "#9CA3AF")
                .set("font-size", "12px");

        Button refreshButton = new Button(
                "Refresh Dashboard",
                event -> UI.getCurrent().getPage().reload()
        );

        refreshButton.getStyle()
                .set("background", "#0284C7")
                .set("color", "#FFFFFF")
                .set("border-radius", "6px")
                .set("cursor", "pointer");

        toolbar.add(lastUpdated, refreshButton);
        content.add(toolbar);

        // LOAD READINGS FROM SQLITE
        try {
            List<SensorDatabase.DatabaseReading> readings =
                    SensorDatabase.getAllReadings();

            if (readings.isEmpty()) {
                Paragraph emptyMessage = new Paragraph(
                        "No readings found in the database. "
                                + "Run App.java to save simulated readings."
                );

                emptyMessage.getStyle().set("color", "#FBBF24");
                content.add(emptyMessage);

                add(sidebar, content);
                setFlexGrow(1, content);
                return;
            }

            // CALCULATE METRICS
            double opticalSum = 0.0;
            double errorSum = 0.0;
            int anomalyCount = 0;

            for (SensorDatabase.DatabaseReading reading : readings) {
                opticalSum += reading.getRawOpticalValue();

                errorSum += Math.abs(
                        TARGET_OPTICAL_VALUE
                                - reading.getRawOpticalValue()
                );

                if (reading.isAnomaly()) {
                    anomalyCount++;
                }
            }

            double meanOptical = opticalSum / readings.size();
            double meanAbsoluteError = errorSum / readings.size();

            // METRIC CARDS
            HorizontalLayout metrics = new HorizontalLayout();
            metrics.setWidthFull();
            metrics.setSpacing(true);
            metrics.getStyle().set("flex-wrap", "wrap");

            metrics.add(
                    createMetricCard(
                            "Mean Optical Output",
                            format(meanOptical),
                            "Calculated from SQLite readings"
                    ),
                    createMetricCard(
                            "Average Temperature",
                            "Not stored",
                            "Temperature is not in the database"
                    ),
                    createMetricCard(
                            "Mean Absolute Error",
                            format(meanAbsoluteError),
                            "Target: 100.00"
                    ),
                    createMetricCard(
                            "Anomalies",
                            String.valueOf(anomalyCount),
                            "Anomaly flags saved in SQLite"
                    )
            );

            content.add(metrics);

            // GRAPH
            H3 chartHeading =
                    new H3("Optical Readings: Raw vs Processed");

            chartHeading.getStyle()
                    .set("margin-top", "16px")
                    .set("margin-bottom", "0");

            byte[] chartImage = createOpticalChart(readings);

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

            // SENSOR READINGS TABLE
            H3 tableHeading = new H3("Sensor Readings");

            Grid<SensorDatabase.DatabaseReading> grid =
                    new Grid<>(SensorDatabase.DatabaseReading.class, false);

            grid.addColumn(
                    SensorDatabase.DatabaseReading::getTimestamp
            ).setHeader("Timestamp").setAutoWidth(true);

            grid.addColumn(
                    SensorDatabase.DatabaseReading::getRawOpticalValue
            ).setHeader("Raw Optical Value").setAutoWidth(true);

            grid.addColumn(
                    reading -> format(reading.getProcessedOpticalValue())
            ).setHeader("Processed Optical Value").setAutoWidth(true);

            grid.addColumn(
                    reading -> reading.isAnomaly() ? "Yes" : "No"
            ).setHeader("Anomaly").setAutoWidth(true);

            grid.addColumn(
                    reading -> format(reading.getControllerOutput())
            ).setHeader("Controller Output").setAutoWidth(true);

            grid.setItems(readings);
            grid.setWidthFull();

            // SHOW UP TO SIX ROWS; SCROLL WHEN THERE ARE MORE
            int visibleRows = Math.min(readings.size(), 6);

            // Approx. 42 px per row, plus space for the header.
            int tableHeight = 42 * visibleRows + 50;

            grid.setHeight(tableHeight + "px");

            grid.getStyle()
                    .set("flex-shrink", "0");

            content.add(tableHeading, grid);

            // ANOMALY ALERTS
            H3 anomalyHeading = new H3("Anomaly Alerts");

            VerticalLayout anomalyPanel = new VerticalLayout();
            anomalyPanel.setPadding(true);
            anomalyPanel.setSpacing(true);

            anomalyPanel.getStyle()
                    .set("background", "#111827")
                    .set("border", "1px solid #263244")
                    .set("border-radius", "12px");

            boolean foundAnomaly = false;

            for (SensorDatabase.DatabaseReading reading : readings) {
                if (reading.isAnomaly()) {
                    foundAnomaly = true;

                    Paragraph alert = new Paragraph(
                            "Anomaly at "
                                    + reading.getTimestamp()
                                    + " — optical value: "
                                    + format(reading.getRawOpticalValue())
                    );

                    alert.getStyle().set("color", "#FBBF24");
                    anomalyPanel.add(alert);
                }
            }

            if (!foundAnomaly) {
                Paragraph noAnomalies = new Paragraph(
                        "No anomaly flags were found in the database."
                );

                noAnomalies.getStyle().set("color", "#34D399");
                anomalyPanel.add(noAnomalies);
            }

            content.add(anomalyHeading, anomalyPanel);

            // STATUS
            Paragraph status = new Paragraph(
                    "DATABASE MODE — Readings are loaded from SQLite. "
                            + "This is a demonstration dashboard; "
                            + "no physical medical device is connected."
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

    // CREATE METRIC CARD
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

    // CREATE GRAPH FROM DATABASE VALUES
    private byte[] createOpticalChart(
            List<SensorDatabase.DatabaseReading> readings)
            throws IOException {

        XYSeries rawSeries = new XYSeries("Raw Optical Value");
        XYSeries processedSeries =
                new XYSeries("Processed Optical Value");

        for (int i = 0; i < readings.size(); i++) {
            SensorDatabase.DatabaseReading reading = readings.get(i);

            rawSeries.add(i + 1, reading.getRawOpticalValue());

            processedSeries.add(
                    i + 1,
                    reading.getProcessedOpticalValue()
            );
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

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            ChartUtils.writeChartAsPNG(
                    output,
                    chart,
                    1100,
                    450
            );

            return output.toByteArray();
        }
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}

