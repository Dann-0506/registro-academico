package com.academico.service;

import com.academico.model.*;
import com.academico.util.PdfUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

public class ExportadorPdfService {

    private final CalificacionService calificacionService = new CalificacionService();

    public void generarActa(File destino, Grupo grupo, List<Unidad> unidades, List<CalificacionFinal> alumnos) throws Exception {
        // Formato Horizontal (PageSize.A4.rotate()) con márgenes
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(documento, new FileOutputStream(destino));

        documento.open();

        // 1. Título Central
        Paragraph titulo = new Paragraph("ACTA OFICIAL DE CALIFICACIONES", PdfUtil.FUENTE_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        documento.add(Chunk.NEWLINE);

        // 2. Información General del Grupo
        PdfPTable tablaInfo = new PdfPTable(2);
        tablaInfo.setWidthPercentage(100);

        tablaInfo.addCell(PdfUtil.crearCeldaSinBorde("Materia/Grupo: " + grupo.getClave(), PdfUtil.FUENTE_SUBTITULO, Element.ALIGN_LEFT));
        tablaInfo.addCell(PdfUtil.crearCeldaSinBorde("Estado: " + grupo.getEstadoEvaluacion(), PdfUtil.FUENTE_SUBTITULO, Element.ALIGN_RIGHT));

        tablaInfo.addCell(PdfUtil.crearCeldaSinBorde("Criterios de Evaluación aplicados:", PdfUtil.FUENTE_NORMAL, Element.ALIGN_LEFT));
        tablaInfo.addCell(PdfUtil.crearCeldaSinBorde("Mínima Aprobatoria: " + grupo.getCalificacionMinimaAprobatoria() + "  |  Máxima: " + grupo.getCalificacionMaxima(), PdfUtil.FUENTE_NORMAL, Element.ALIGN_RIGHT));

        documento.add(tablaInfo);
        documento.add(Chunk.NEWLINE);

        // 3. Configuración Dinámica de la Tabla de Calificaciones
        int numColumnas = 5 + unidades.size(); // Matricula, Nombre, [U1..Un], Promedio, Final, Estado
        PdfPTable tabla = new PdfPTable(numColumnas);
        tabla.setWidthPercentage(100);

        // Ajustar anchos (El nombre ocupa más espacio que una calificación)
        float[] anchos = new float[numColumnas];
        anchos[0] = 2f; // Matricula
        anchos[1] = 4f; // Nombre
        for (int i = 0; i < unidades.size(); i++) anchos[2 + i] = 1f; // Unidades
        anchos[numColumnas - 3] = 1.2f; // Promedio Base
        anchos[numColumnas - 2] = 1.2f; // Final
        anchos[numColumnas - 1] = 2f;   // Estado (Aprobado/Reprobado)
        tabla.setWidths(anchos);

        // 4. Cabeceras
        tabla.addCell(PdfUtil.crearCeldaCabecera("Matrícula"));
        tabla.addCell(PdfUtil.crearCeldaCabecera("Nombre del Alumno"));
        for (Unidad u : unidades) {
            tabla.addCell(PdfUtil.crearCeldaCabecera("U" + u.getNumero()));
        }
        tabla.addCell(PdfUtil.crearCeldaCabecera("Prom."));
        tabla.addCell(PdfUtil.crearCeldaCabecera("Final"));
        tabla.addCell(PdfUtil.crearCeldaCabecera("Estado"));

        // 5. Llenado de Datos de Alumnos
        for (CalificacionFinal cf : alumnos) {
            tabla.addCell(PdfUtil.crearCeldaNormal(cf.getAlumnoMatricula(), Element.ALIGN_CENTER));
            tabla.addCell(PdfUtil.crearCeldaNormal(cf.getAlumnoNombre(), Element.ALIGN_LEFT));

            // Imprimir unidades
            for (Unidad u : unidades) {
                Optional<ResultadoUnidad> ru = cf.getUnidades().stream().filter(res -> res.getUnidadId() == u.getId()).findFirst();
                String val = ru.map(r -> r.getResultadoFinal() != null ? r.getResultadoFinal().toString() : "-").orElse("-");
                tabla.addCell(PdfUtil.crearCeldaNormal(val, Element.ALIGN_CENTER));
            }

            // Promedios y estado
            String prom = cf.getCalificacionCalculada() != null ? cf.getCalificacionCalculada().toString() : "-";
            String fin = cf.getCalificacionFinal() != null ? cf.getCalificacionFinal().toString() : "-";
            if (cf.isEsOverride()) fin += " (M)"; // MarcarOverrides en el Acta Oficial

            String estado = "ERROR";
            try {
                estado = calificacionService.determinarEstado(cf.getCalificacionFinal(), grupo.getCalificacionMinimaAprobatoria());
            } catch (Exception ignored) {}

            tabla.addCell(PdfUtil.crearCeldaNormal(prom, Element.ALIGN_CENTER));
            tabla.addCell(PdfUtil.crearCeldaNormal(fin, Element.ALIGN_CENTER));
            tabla.addCell(PdfUtil.crearCeldaNormal(estado, Element.ALIGN_CENTER));
        }
        documento.add(tabla);
        documento.add(Chunk.NEWLINE);
        documento.add(Chunk.NEWLINE);

        // 6. Pie de Página (Firmas Oficiales)
        PdfPTable tablaFirmas = new PdfPTable(2);
        tablaFirmas.setWidthPercentage(100);
        tablaFirmas.addCell(PdfUtil.crearCeldaSinBorde("_________________________________\nFirma del Docente", PdfUtil.FUENTE_NORMAL, Element.ALIGN_CENTER));
        tablaFirmas.addCell(PdfUtil.crearCeldaSinBorde("_________________________________\nSello de Servicios Escolares", PdfUtil.FUENTE_NORMAL, Element.ALIGN_CENTER));
        documento.add(tablaFirmas);

        documento.close();
    }
}