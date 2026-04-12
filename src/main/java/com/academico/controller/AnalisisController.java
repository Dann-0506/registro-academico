package com.academico.controller;

import com.academico.service.AnalisisService;
import com.academico.service.AnalisisService.KpiDTO;
import com.academico.service.AnalisisService.RendimientoDTO;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class AnalisisController {

    // === ELEMENTOS UI ===
    @FXML private Label lblTotalAlumnos;
    @FXML private Label lblGruposActivos;
    @FXML private Label lblTotalMaterias;
    @FXML private BarChart<String, Number> graficaRendimiento;

    // === SERVICIO ORQUESTADOR ===
    private final AnalisisService analisisService = new AnalisisService();

    @FXML
    public void initialize() {
        cargarDashboard();
    }

    private void cargarDashboard() {
        try {
            KpiDTO kpis = analisisService.obtenerKpisGenerales();
            lblTotalAlumnos.setText(String.valueOf(kpis.totalAlumnos()));
            lblTotalMaterias.setText(String.valueOf(kpis.totalMaterias()));
            lblGruposActivos.setText(String.valueOf(kpis.gruposActivos()));

            XYChart.Series<String, Number> serieAprobados = new XYChart.Series<>();
            serieAprobados.setName("Aprobados");

            XYChart.Series<String, Number> serieReprobados = new XYChart.Series<>();
            serieReprobados.setName("Reprobados");

            for (RendimientoDTO dato : analisisService.obtenerDatosRendimiento()) {
                serieAprobados.getData().add(new XYChart.Data<>(dato.semestre(), dato.aprobados()));
                serieReprobados.getData().add(new XYChart.Data<>(dato.semestre(), dato.reprobados()));
            }

            graficaRendimiento.getData().add(serieAprobados);
            graficaRendimiento.getData().add(serieReprobados);

            // 3. APLICAR ESTILOS VISUALES
            Platform.runLater(() -> {
                for (Node nodo : graficaRendimiento.lookupAll(".default-color0.chart-bar")) {
                    nodo.setStyle("-fx-bar-fill: #2da44e;");
                }
                for (Node nodo : graficaRendimiento.lookupAll(".default-color1.chart-bar")) {
                    nodo.setStyle("-fx-bar-fill: #cf222e;");
                }
            });

        } catch (Exception e) {
            System.err.println("Error al cargar el Dashboard: " + e.getMessage());
        }
    }
}