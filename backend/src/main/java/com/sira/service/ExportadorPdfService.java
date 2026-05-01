package com.sira.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.sira.dto.CalificacionFinalDto;
import com.sira.dto.ResultadoUnidadDto;
import com.sira.model.Grupo;
import com.sira.model.Unidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
public class ExportadorPdfService {

    private static final Font FUENTE_TITULO    = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font FUENTE_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FUENTE_NORMAL    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FUENTE_CABECERA  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final BaseColor COLOR_PRIMARIO = new BaseColor(9, 105, 218);

    @Autowired private CalificacionService calificacionService;

    // ==========================================
    // ACTA GRUPAL
    // ==========================================

    public byte[] generarActa(Grupo grupo, List<Unidad> unidades, List<CalificacionFinalDto> alumnos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(doc, out);
            doc.open();

            agregarTitulo(doc, "ACTA OFICIAL DE CALIFICACIONES");
            agregarInfoGrupo(doc, grupo);
            doc.add(Chunk.NEWLINE);
            agregarTablaCalificaciones(doc, grupo, unidades, alumnos);
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            agregarFirmas(doc);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el acta PDF: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // BOLETA INDIVIDUAL
    // ==========================================

    public byte[] generarBoleta(Grupo grupo, CalificacionFinalDto cf) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            agregarTitulo(doc, "BOLETA DE CALIFICACIONES");
            doc.add(Chunk.NEWLINE);

            // Datos del alumno
            PdfPTable datosAlumno = new PdfPTable(2);
            datosAlumno.setWidthPercentage(100);
            datosAlumno.addCell(crearCeldaSinBorde("Alumno: " + cf.getAlumnoNombre(), FUENTE_SUBTITULO, Element.ALIGN_LEFT));
            datosAlumno.addCell(crearCeldaSinBorde("Matrícula: " + cf.getAlumnoMatricula(), FUENTE_SUBTITULO, Element.ALIGN_RIGHT));
            datosAlumno.addCell(crearCeldaSinBorde("Materia: " + grupo.getMateria().getNombre() + " (" + grupo.getMateria().getClave() + ")", FUENTE_NORMAL, Element.ALIGN_LEFT));
            datosAlumno.addCell(crearCeldaSinBorde("Grupo: " + grupo.getClave() + " — " + grupo.getSemestre(), FUENTE_NORMAL, Element.ALIGN_RIGHT));
            datosAlumno.addCell(crearCeldaSinBorde("Docente: " + grupo.getMaestro().getUsuario().getNombre(), FUENTE_NORMAL, Element.ALIGN_LEFT));
            datosAlumno.addCell(new PdfPCell());
            doc.add(datosAlumno);
            doc.add(Chunk.NEWLINE);

            // Tabla de unidades
            PdfPTable tablaUnidades = new PdfPTable(4);
            tablaUnidades.setWidthPercentage(100);
            tablaUnidades.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f});
            tablaUnidades.addCell(crearCeldaCabecera("Unidad"));
            tablaUnidades.addCell(crearCeldaCabecera("Base"));
            tablaUnidades.addCell(crearCeldaCabecera("Bonus"));
            tablaUnidades.addCell(crearCeldaCabecera("Final"));

            if (cf.getUnidades() != null) {
                for (ResultadoUnidadDto ru : cf.getUnidades()) {
                    tablaUnidades.addCell(crearCeldaNormal("U" + ru.getUnidadNumero() + ": " + ru.getUnidadNombre(), Element.ALIGN_LEFT));
                    tablaUnidades.addCell(crearCeldaNormal(ru.getResultadoBase() != null ? ru.getResultadoBase().toPlainString() : "-", Element.ALIGN_CENTER));
                    tablaUnidades.addCell(crearCeldaNormal(ru.getBonusPuntos() != null && ru.getBonusPuntos().signum() > 0 ? "+" + ru.getBonusPuntos() : "-", Element.ALIGN_CENTER));
                    tablaUnidades.addCell(crearCeldaNormal(ru.getResultadoFinal() != null ? ru.getResultadoFinal().toPlainString() : "Pte.", Element.ALIGN_CENTER));
                }
            }
            doc.add(tablaUnidades);
            doc.add(Chunk.NEWLINE);

            // Resumen final
            String estadoFinal = calificacionService.determinarEstado(
                    cf.getCalificacionFinal(), grupo.getCalificacionMinimaAprobatoria());
            String calFinal = cf.getCalificacionFinal() != null ? cf.getCalificacionFinal().toPlainString() : "Pendiente";
            if (cf.isEsOverride()) calFinal += " (Manual)";

            PdfPTable resumen = new PdfPTable(2);
            resumen.setWidthPercentage(60);
            resumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumen.addCell(crearCeldaNormal("Calificación Final:", Element.ALIGN_LEFT));
            resumen.addCell(crearCeldaNormal(calFinal, Element.ALIGN_CENTER));
            resumen.addCell(crearCeldaNormal("Estado:", Element.ALIGN_LEFT));
            resumen.addCell(crearCeldaNormal(estadoFinal, Element.ALIGN_CENTER));
            doc.add(resumen);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar la boleta PDF: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // AUXILIARES
    // ==========================================

    private void agregarTitulo(Document doc, String texto) throws DocumentException {
        Paragraph titulo = new Paragraph(texto, FUENTE_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);
    }

    private void agregarInfoGrupo(Document doc, Grupo grupo) throws DocumentException {
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.addCell(crearCeldaSinBorde("Materia: " + grupo.getMateria().getNombre() + " — Grupo: " + grupo.getClave(), FUENTE_SUBTITULO, Element.ALIGN_LEFT));
        info.addCell(crearCeldaSinBorde("Estado: " + grupo.getEstadoEvaluacion(), FUENTE_SUBTITULO, Element.ALIGN_RIGHT));
        info.addCell(crearCeldaSinBorde("Docente: " + grupo.getMaestro().getUsuario().getNombre() + " — Semestre: " + grupo.getSemestre(), FUENTE_NORMAL, Element.ALIGN_LEFT));
        info.addCell(crearCeldaSinBorde("Mínima: " + grupo.getCalificacionMinimaAprobatoria() + "  |  Máxima: " + grupo.getCalificacionMaxima(), FUENTE_NORMAL, Element.ALIGN_RIGHT));
        doc.add(info);
    }

    private void agregarTablaCalificaciones(Document doc, Grupo grupo, List<Unidad> unidades,
                                             List<CalificacionFinalDto> alumnos) throws DocumentException {
        int cols = 4 + unidades.size();
        PdfPTable tabla = new PdfPTable(cols);
        tabla.setWidthPercentage(100);

        float[] anchos = new float[cols];
        anchos[0] = 2f; anchos[1] = 4f;
        for (int i = 0; i < unidades.size(); i++) anchos[2 + i] = 1f;
        anchos[cols - 2] = 1.5f; anchos[cols - 1] = 2f;
        tabla.setWidths(anchos);

        tabla.addCell(crearCeldaCabecera("Matrícula"));
        tabla.addCell(crearCeldaCabecera("Nombre"));
        for (Unidad u : unidades) tabla.addCell(crearCeldaCabecera("U" + u.getNumero()));
        tabla.addCell(crearCeldaCabecera("Final"));
        tabla.addCell(crearCeldaCabecera("Estado"));

        for (CalificacionFinalDto cf : alumnos) {
            tabla.addCell(crearCeldaNormal(cf.getAlumnoMatricula(), Element.ALIGN_CENTER));
            tabla.addCell(crearCeldaNormal(cf.getAlumnoNombre(), Element.ALIGN_LEFT));
            for (Unidad u : unidades) {
                Optional<ResultadoUnidadDto> ru = cf.getUnidades() == null ? Optional.empty()
                        : cf.getUnidades().stream().filter(r -> r.getUnidadId().equals(u.getId())).findFirst();
                String val = ru.map(r -> r.getResultadoFinal() != null ? r.getResultadoFinal().toPlainString() : "-").orElse("-");
                tabla.addCell(crearCeldaNormal(val, Element.ALIGN_CENTER));
            }
            String fin = cf.getCalificacionFinal() != null ? cf.getCalificacionFinal().toPlainString() : "-";
            if (cf.isEsOverride()) fin += " (M)";
            String estado = calificacionService.determinarEstado(
                    cf.getCalificacionFinal(), grupo.getCalificacionMinimaAprobatoria());
            tabla.addCell(crearCeldaNormal(fin, Element.ALIGN_CENTER));
            tabla.addCell(crearCeldaNormal(estado, Element.ALIGN_CENTER));
        }
        doc.add(tabla);
    }

    private void agregarFirmas(Document doc) throws DocumentException {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.addCell(crearCeldaSinBorde("_________________________________\nFirma del Docente", FUENTE_NORMAL, Element.ALIGN_CENTER));
        firmas.addCell(crearCeldaSinBorde("_________________________________\nSello de Servicios Escolares", FUENTE_NORMAL, Element.ALIGN_CENTER));
        doc.add(firmas);
    }

    private PdfPCell crearCeldaCabecera(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, FUENTE_CABECERA));
        c.setBackgroundColor(COLOR_PRIMARIO);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        return c;
    }

    private PdfPCell crearCeldaNormal(String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "-", FUENTE_NORMAL));
        c.setHorizontalAlignment(alineacion);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(4);
        return c;
    }

    private PdfPCell crearCeldaSinBorde(String texto, Font fuente, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(alineacion);
        return c;
    }
}
