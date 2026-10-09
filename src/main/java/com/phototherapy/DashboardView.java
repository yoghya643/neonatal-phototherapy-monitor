package com.phototherapy;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.dependency.CssImport;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route("")
public class DashboardView extends HorizontalLayout {

    private static final double TARGET_OPTICAL_VALUE = 100.0;
    private static final double REFERENCE_ILLUMINATION = 100.0;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final Div dashboardData = new Div();
    private final Paragraph lastUpdated = new Paragraph();
    private final VerticalLayout content = new VerticalLayout();
    private final VerticalLayout sidebar = new VerticalLayout();
    private final Div dashboardSection = new Div();
    private final Div readingsSection = new Div();
    private final Div anomalySection = new Div();
    private final Div performanceSection = new Div();
    private final Span themeStatus = new Span();

    private boolean darkMode = true;
    private boolean compactMode = false;

    public DashboardView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#0B1220").set("color", "#F9FAFB");
        getElement().setAttribute("theme", "dark");
        getStyle().set("overflow", "hidden");

        buildSidebar();
        buildContent();

        add(sidebar, content);
        setFlexGrow(1, content);

        // Important: no polling or automatic page rebuild. This prevents scroll jumps.
        refreshDashboard();
    }

    private void buildSidebar() {
        sidebar.setWidth("245px");
        sidebar.setMinWidth("245px");
        sidebar.setPadding(true);
        sidebar.setSpacing(true);
        sidebar.getStyle()
                .set("background", "#111827")
                .set("color", "#FFFFFF")
                .set("height", "100vh")
                .set("overflow-y", "auto")
                .set("box-sizing", "border-box");

        H3 logo = new H3("✦ NeoLight");
        logo.getStyle().set("color", "#38BDF8").set("margin-bottom", "0");

        Paragraph subtitle = new Paragraph("Phototherapy Monitor");
        subtitle.getStyle().set("color", "#9CA3AF").set("font-size", "12px");

        sidebar.add(logo, subtitle, makeNavButton("▦  Dashboard", dashboardSection),
                makeNavButton("≋  Sensor Readings", readingsSection),
                makeNavButton("⚠  Anomaly Alerts", anomalySection),
                makeNavButton("∿  Performance & Formulas", performanceSection));

        sidebar.add(new Div());
        H3 appearanceTitle = new H3("Appearance");
        appearanceTitle.getStyle().set("font-size", "15px").set("margin-top", "18px");
        Button themeButton = new Button("☼  Switch to Light Mode");
        styleSecondaryButton(themeButton);
        themeButton.setWidthFull();
        themeButton.addClickListener(e -> {
            darkMode = !darkMode;
            applyTheme(themeButton);
            // Rebuild styled components so labels, cards, and the Grid all use the new palette.
            refreshDashboard();
        });

        Checkbox compact = new Checkbox("Compact layout");
        compact.setValue(false);
        compact.getStyle().set("color", "#D1D5DB");
        compact.addValueChangeListener(e -> {
            compactMode = Boolean.TRUE.equals(e.getValue());
            applyCompactMode();
        });

        Paragraph hint = new Paragraph("Choose a section above. Use Refresh Data when you want to reload readings.");
        hint.getStyle().set("color", "#9CA3AF").set("font-size", "11px");
        sidebar.add(appearanceTitle, themeButton, compact, hint);
    }

    private Button makeNavButton(String text, Div target) {
        Button button = new Button(text);
        button.setWidthFull();
        button.getStyle()
                .set("text-align", "left")
                .set("justify-content", "flex-start")
                .set("background", "transparent")
                .set("color", "#D1D5DB")
                .set("border", "1px solid transparent")
                .set("border-radius", "8px")
                .set("padding", "12px")
                .set("cursor", "pointer");
        button.addClickListener(e -> target.getElement().executeJs(
                "this.scrollIntoView({behavior:'smooth', block:'start'});"));
        return button;
    }

    private void buildContent() {
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle()
                .set("background", "#0B1220")
                .set("color", "#F9FAFB")
                .set("overflow-y", "auto")
                .set("box-sizing", "border-box");

        // NeoLight hero banner. Put phototherapy-hero.png in
        // src/main/resources/static/images/ so Spring Boot serves it at /images/phototherapy-hero.png.
        Div hero = new Div();
        hero.setWidthFull();
        hero.setMinHeight("235px");
        hero.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "28px 30px")
                .set("border-radius", "18px")
                .set("overflow", "hidden")
                .set("background-color", "#102B55")
                .set("background-image", "linear-gradient(90deg, rgba(5,18,45,0.96) 0%, rgba(5,25,62,0.82) 42%, rgba(5,25,62,0.10) 100%), url('/images/phototherapy-hero.png')")
                .set("background-size", "cover")
                .set("background-position", "center right")
                .set("background-repeat", "no-repeat")
                .set("box-shadow", "0 12px 30px rgba(2, 8, 23, 0.18)");

        H1 title = new H1("NeoLight");
        title.getStyle().set("color", "#FFFFFF")
                .set("font-size", "clamp(34px, 4vw, 52px)")
                .set("font-weight", "800")
                .set("letter-spacing", "-1px")
                .set("margin", "0 0 4px 0");

        H3 heroSubtitle = new H3("Phototherapy Monitoring System");
        heroSubtitle.getStyle().set("color", "#FFFFFF")
                .set("font-size", "clamp(18px, 2vw, 26px)")
                .set("margin", "0 0 10px 0");

        Paragraph heroDescription = new Paragraph(
                "Real-time monitoring for safer, smarter neonatal care");
        heroDescription.getStyle().set("color", "#DCEBFF")
                .set("font-size", "14px")
                .set("margin", "0");
        Div heroText = new Div(title, heroSubtitle, heroDescription);
        heroText.setWidth("min(620px, 100%)");
        heroText.getStyle().set("position", "relative").set("z-index", "1");
        hero.add(heroText);

        Paragraph description = new Paragraph(
                "Optical monitoring, moving-average filtering, simulated temperature, "
                        + "controller calculations and anomaly detection.");
        description.getStyle().set("color", "#9CA3AF");

        Paragraph demoNotice = new Paragraph(
                "DEMO MODE: readings are simulated or loaded from the project database. "
                        + "No physical sensor or LED controller is connected.");
        demoNotice.getStyle().set("color", "#FBBF24").set("font-size", "12px");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        lastUpdated.getStyle().set("color", "#9CA3AF").set("font-size", "12px");

        Button refreshButton = new Button("↻  Refresh Data", e -> refreshDashboard());
        stylePrimaryButton(refreshButton);
        toolbar.add(lastUpdated, refreshButton);

        dashboardData.setWidthFull();
        dashboardSection.setWidthFull();
        readingsSection.setWidthFull();
        anomalySection.setWidthFull();
        performanceSection.setWidthFull();

        content.add(hero, description, demoNotice, toolbar, dashboardSection,
                readingsSection, anomalySection, performanceSection);
    }

    private void refreshDashboard() {
        dashboardData.removeAll();
        dashboardSection.removeAll();
        readingsSection.removeAll();
        anomalySection.removeAll();
        performanceSection.removeAll();

        try {
            List<SensorDatabase.DatabaseReading> dbRows = SensorDatabase.getAllReadings();
            boolean usingSimulation = dbRows.isEmpty();
            List<DisplayReading> readings = new ArrayList<>();

            if (usingSimulation) {
                readings.addAll(createDemoReadings());
            } else {
                int i = 0;
                for (SensorDatabase.DatabaseReading r : dbRows) {
                    double optical = r.getProcessedOpticalValue();
                    double pwm = calculateSimulatedPwm(optical);
                    readings.add(new DisplayReading(
                            String.valueOf(r.getTimestamp()),
                            r.getRawOpticalValue(),
                            r.getProcessedOpticalValue(),
                            r.isAnomaly(),
                            pwm,
                            simulatedTemperature(i, r.getRawOpticalValue())));
                    i++;
                }
            }

            lastUpdated.setText("Last refreshed: " + LocalDateTime.now().format(TIME_FORMAT));
            int count = readings.size();
            if (count == 0) {
                dashboardSection.add(new Paragraph("No readings available. Run App.java or use demo data."));
                return;
            }

            double opticalSum = 0, errorSum = 0, temperatureSum = 0, cfSum = 0, pwmSum = 0;
            int anomalies = 0;
            for (DisplayReading r : readings) {
                opticalSum += r.rawOptical;
                errorSum += Math.abs(TARGET_OPTICAL_VALUE - r.processedOptical);
                temperatureSum += r.temperature;
                cfSum += r.processedOptical / REFERENCE_ILLUMINATION;
                pwmSum += r.pwm;
                if (r.anomaly) anomalies++;
            }

            HorizontalLayout metrics = new HorizontalLayout();
            metrics.setWidthFull();
            metrics.getStyle().set("flex-wrap", "wrap").set("gap", compactMode ? "8px" : "14px");
            metrics.add(
                    metricCard("Mean Optical Output", fmt(opticalSum / count), "Average raw reading"),
                    metricCard("Average Temperature", fmt(temperatureSum / count) + " °C", "Simulated temperature"),
                    metricCard("Mean Absolute Error", fmt(errorSum / count), "Target: 100.00"),
                    metricCard("Anomalies", String.valueOf(anomalies), "Flagged readings"),
                    metricCard("Correction Factor (CF)", fmt(cfSum / count), "Processed / reference"),
                    metricCard("Average PWM", fmt(pwmSum / count) + "%", "Simulated controller output")
            );
            dashboardSection.add(sectionTitle("Dashboard Overview"), metrics,
                    createStatusCard(usingSimulation, count, anomalies));

            readingsSection.add(sectionTitle("Sensor Readings"),
                    new Paragraph("Raw readings are compared with the smoothed moving-average output."));
            byte[] chartBytes = createChart(readings);
            StreamResource chartResource = new StreamResource(
                    "phototherapy-chart-" + System.currentTimeMillis() + ".png",
                    () -> new ByteArrayInputStream(chartBytes));
            Image chartImage = new Image(chartResource, "Raw and processed optical readings chart");
            chartImage.setWidthFull();
            chartImage.getStyle().set("border-radius", "12px").set("background", "#FFFFFF");
            Div chartBox = new Div(chartImage);
            chartBox.setWidthFull();
            chartBox.getStyle().set("background", "#FFFFFF").set("padding", "10px")
                    .set("border-radius", "12px").set("box-sizing", "border-box");

            Grid<DisplayReading> grid = new Grid<>(DisplayReading.class, false);
            grid.addColumn(r -> r.timestamp).setHeader("Timestamp").setAutoWidth(true);
            grid.addColumn(r -> fmt(r.rawOptical)).setHeader("Raw Optical").setAutoWidth(true);
            grid.addColumn(r -> fmt(r.processedOptical)).setHeader("Processed Optical").setAutoWidth(true);
            grid.addColumn(r -> fmt(r.temperature) + " °C").setHeader("Temperature*").setAutoWidth(true);
            grid.addColumn(r -> fmt(TARGET_OPTICAL_VALUE - r.processedOptical)).setHeader("Control Error").setAutoWidth(true);
            grid.addColumn(r -> fmt(r.processedOptical / REFERENCE_ILLUMINATION)).setHeader("CF").setAutoWidth(true);
            grid.addColumn(r -> fmt(r.pwm) + "%").setHeader("PWM Output*").setAutoWidth(true);
            grid.addColumn(r -> fmt(100.0 - r.pwm) + "%").setHeader("OFF Time*").setAutoWidth(true);
            grid.addColumn(r -> r.anomaly ? "⚠ Yes" : "No").setHeader("Anomaly").setAutoWidth(true);
            grid.setItems(readings);
            grid.getElement().setAttribute("theme", darkMode ? "dark" : "light");
            grid.getStyle()
                    .set("--lumo-base-color", darkMode ? "#111827" : "#FFFFFF")
                    .set("--lumo-body-text-color", darkMode ? "#E5E7EB" : "#1F2937")
                    .set("--lumo-secondary-text-color", darkMode ? "#9CA3AF" : "#4B5563")
                    .set("--lumo-header-text-color", darkMode ? "#F9FAFB" : "#111827")
                    .set("--lumo-contrast", darkMode ? "#F9FAFB" : "#111827")
                    .set("--lumo-contrast-60pct", darkMode ? "#D1D5DB" : "#4B5563")
                    .set("--lumo-contrast-10pct", darkMode ? "#374151" : "#E5E7EB");
            grid.setWidthFull();
            grid.setHeight(Math.max(180, Math.min(readings.size(), 8) * 42 + 50) + "px");
            readingsSection.add(chartBox, grid);
            Paragraph note = new Paragraph("* Temperature and controller output are simulated for demonstration; no hardware is controlled.");
            note.getStyle().set("color", "#9CA3AF").set("font-size", "11px");
            readingsSection.add(note);

            VerticalLayout alertPanel = new VerticalLayout();
            alertPanel.setPadding(true);
            alertPanel.setSpacing(true);
            stylePanel(alertPanel);
            int shown = 0;
            for (DisplayReading r : readings) {
                if (r.anomaly) {
                    Paragraph p = new Paragraph("⚠  " + r.timestamp + " — optical reading " + fmt(r.rawOptical));
                    p.getStyle().set("color", "#FBBF24").set("margin", "2px 0");
                    alertPanel.add(p);
                    shown++;
                }
            }
            if (shown == 0) {
                Paragraph p = new Paragraph("✓ No anomalies flagged in the currently displayed readings.");
                p.getStyle().set("color", "#34D399");
                alertPanel.add(p);
            }
            anomalySection.add(sectionTitle("Anomaly Alerts"), alertPanel);

            DisplayReading latest = readings.get(count - 1);
            double error = TARGET_OPTICAL_VALUE - latest.processedOptical;
            double cf = latest.processedOptical / REFERENCE_ILLUMINATION;
            VerticalLayout formulas = new VerticalLayout();
            formulas.setPadding(true);
            formulas.setSpacing(true);
            stylePanel(formulas);
            formulas.add(
                    formulaLine("Target illumination (Isp)", fmt(TARGET_OPTICAL_VALUE)),
                    formulaLine("Measured illumination (Ic)", fmt(latest.processedOptical)),
                    formulaLine("Control error e(t) = Isp − Ic", fmt(error)),
                    formulaLine("Correction factor CF = measured / reference", fmt(cf)),
                    formulaLine("Illustrative proportional response", fmt(latest.pwm) + "% PWM"),
                    formulaLine("Complementary OFF fraction", fmt(100 - latest.pwm) + "%"),
                    formulaLine("Moving average", "Mean of the latest available readings in the filter window")
            );
            Paragraph explain = new Paragraph(
                    "Judge explanation: the moving average smooths short-term variation; error is the difference "
                            + "between target and measured illumination; CF is a ratio. PWM is a simulated output "
                            + "limited to 0–100% and is not sent to a real LED.");
            explain.getStyle().set("color", "#9CA3AF").set("font-size", "12px");
            performanceSection.add(sectionTitle("Performance & Formula Monitor"), formulas, explain);

            applyTheme(null);
            applyCompactMode();
        } catch (Exception ex) {
            Paragraph error = new Paragraph("Could not load dashboard data: " + ex.getMessage());
            error.getStyle().set("color", "#F87171");
            dashboardSection.add(error);
            ex.printStackTrace();
        }
    }

    private Div createStatusCard(boolean usingSimulation, int count, int anomalies) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle().set("margin-top", "14px").set("padding", "16px")
                .set("border-radius", "12px").set("border", "1px solid #263244")
                .set("background", darkMode ? "#111827" : "#FFFFFF");
        Paragraph p = new Paragraph((usingSimulation ? "DEMO DATA" : "DATABASE DATA")
                + "  •  " + count + " readings  •  " + anomalies + " anomalies flagged");
        p.getStyle().set("margin", "0").set("color", darkMode ? "#E5E7EB" : "#1F2937");
        card.add(p);
        return card;
    }

    private List<DisplayReading> createDemoReadings() {
        List<DisplayReading> list = new ArrayList<>();
        double[] raw = {88.4, 91.7, 94.3, 97.6, 103.2, 99.1, 101.4, 96.8, 98.7, 100.6, 104.1, 99.4};
        double[] smooth = {88.4, 90.05, 91.47, 94.53, 98.37, 99.97, 101.23, 99.10, 98.97, 98.70, 101.13, 101.37};
        for (int i = 0; i < raw.length; i++) {
            double pwm = calculateSimulatedPwm(smooth[i]);
            list.add(new DisplayReading("DEMO-" + String.format("%02d", i + 1),
                    raw[i], smooth[i], i == 4 || i == 10, pwm, simulatedTemperature(i, raw[i])));
        }
        return list;
    }

    private double calculateSimulatedPwm(double measured) {
        // Illustrative proportional control response for display only, not clinical tuning.
        double error = TARGET_OPTICAL_VALUE - measured;
        return clamp(52.0 + (error * 0.65), 8.0, 92.0);
    }

    private double simulatedTemperature(int index, double optical) {
        double[] values = {36.5, 36.6, 36.7, 36.6, 36.8, 36.7, 36.6, 36.5, 36.7, 36.8, 36.9, 36.7};
        return values[index % values.length] + Math.max(0, optical - 100) * 0.01;
    }

    private H3 sectionTitle(String text) {
        H3 title = new H3(text);
        title.getStyle().set("margin", "12px 0 8px 0")
                .set("color", darkMode ? "#F9FAFB" : "#111827");
        return title;
    }

    private VerticalLayout metricCard(String heading, String value, String detail) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth(compactMode ? "185px" : "220px");
        card.getStyle().set("background", darkMode ? "#111827" : "#FFFFFF")
                .set("border", "1px solid " + (darkMode ? "#263244" : "#E5E7EB"))
                .set("border-radius", "14px").set("box-sizing", "border-box")
                .set("box-shadow", darkMode ? "0 8px 24px rgba(0,0,0,0.12)" : "0 8px 24px rgba(15,23,42,0.06)");
        Paragraph label = new Paragraph(heading);
        label.getStyle().set("color", darkMode ? "#9CA3AF" : "#6B7280").set("font-size", "12px").set("margin", "0");
        H3 number = new H3(value);
        number.getStyle().set("color", darkMode ? "#38BDF8" : "#0369A1").set("margin", "8px 0 4px 0");
        Paragraph detailText = new Paragraph(detail);
        detailText.getStyle().set("color", darkMode ? "#9CA3AF" : "#6B7280").set("font-size", "11px").set("margin", "0");
        card.add(label, number, detailText);
        return card;
    }

    private Paragraph formulaLine(String label, String value) {
        Paragraph p = new Paragraph(label + " : " + value);
        p.getStyle().set("color", darkMode ? "#E5E7EB" : "#1F2937").set("margin", "2px 0");
        return p;
    }

    private void stylePanel(VerticalLayout panel) {
        panel.getStyle().set("background", darkMode ? "#111827" : "#FFFFFF")
                .set("border", "1px solid " + (darkMode ? "#263244" : "#E5E7EB"))
                .set("border-radius", "12px").set("box-sizing", "border-box");
    }

    private void applyTheme(Button themeButton) {
        String background = darkMode ? "#0B1220" : "#F3F6FB";
        String foreground = darkMode ? "#F9FAFB" : "#111827";
        String panelBackground = darkMode ? "#111827" : "#FFFFFF";
        String border = darkMode ? "#263244" : "#E5E7EB";
        getStyle().set("background", background).set("color", foreground);
        // Apply the Vaadin/Lumo theme globally as well as changing the page background.
        getElement().executeJs(
                "document.documentElement.setAttribute('theme', $0);",
                darkMode ? "dark" : "light");
        content.getStyle().set("background", background).set("color", foreground);
        sidebar.getStyle().set("background", darkMode ? "#111827" : "#E8EEF7")
                .set("color", foreground);
        if (themeButton != null) {
            themeButton.setText(darkMode ? "☼  Switch to Light Mode" : "☾  Switch to Dark Mode");
            styleSecondaryButton(themeButton);
        }
        updateTreeTheme(dashboardSection, darkMode, panelBackground, foreground, border);
        updateTreeTheme(readingsSection, darkMode, panelBackground, foreground, border);
        updateTreeTheme(anomalySection, darkMode, panelBackground, foreground, border);
        updateTreeTheme(performanceSection, darkMode, panelBackground, foreground, border);
    }

    private void updateTreeTheme(com.vaadin.flow.component.HasElement component,
                                 boolean dark, String panelBackground, String foreground, String border) {
        // Section headings/cards are styled when rebuilt. Set the section base colors here.
        if (component instanceof Div div) {
            div.getStyle().set("color", foreground);
        }
    }

    private void applyCompactMode() {
        String spacing = compactMode ? "8px" : "16px";
        content.getStyle().set("gap", spacing);
        dashboardSection.getStyle().set("gap", spacing);
        readingsSection.getStyle().set("gap", spacing);
        anomalySection.getStyle().set("gap", spacing);
        performanceSection.getStyle().set("gap", spacing);
    }

    private void stylePrimaryButton(Button button) {
        button.getStyle().set("background", "#0284C7").set("color", "#FFFFFF")
                .set("border-radius", "8px").set("font-weight", "600");
    }

    private void styleSecondaryButton(Button button) {
        button.getStyle().set("background", darkMode ? "#1F2937" : "#FFFFFF")
                .set("color", darkMode ? "#F9FAFB" : "#111827")
                .set("border", "1px solid " + (darkMode ? "#374151" : "#D1D5DB"))
                .set("border-radius", "8px");
    }

    private byte[] createChart(List<DisplayReading> readings) throws IOException {
        XYSeries rawSeries = new XYSeries("Raw Optical");
        XYSeries processedSeries = new XYSeries("Processed / Moving Average");
        for (int i = 0; i < readings.size(); i++) {
            rawSeries.add(i + 1, readings.get(i).rawOptical);
            processedSeries.add(i + 1, readings.get(i).processedOptical);
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(rawSeries);
        dataset.addSeries(processedSeries);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Phototherapy Optical Readings", "Reading Number", "Optical Value", dataset);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(output, chart, 1100, 420);
            return output.toByteArray();
        }
    }

    private String fmt(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class DisplayReading {
        private final String timestamp;
        private final double rawOptical;
        private final double processedOptical;
        private final boolean anomaly;
        private final double pwm;
        private final double temperature;

        private DisplayReading(String timestamp, double rawOptical, double processedOptical,
                               boolean anomaly, double pwm, double temperature) {
            this.timestamp = timestamp;
            this.rawOptical = rawOptical;
            this.processedOptical = processedOptical;
            this.anomaly = anomaly;
            this.pwm = pwm;
            this.temperature = temperature;
        }
    }
}
