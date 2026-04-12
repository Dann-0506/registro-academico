package com.academico.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;

public class PdfUtil {
    
    // Fuentes estandarizadas
    public static final Font FUENTE_TITULO = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    public static final Font FUENTE_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    public static final Font FUENTE_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    public static final Font FUENTE_CABECERA = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    
    // Color azul institucional (similar al de tus botones)
    public static final BaseColor COLOR_PRIMARIO = new BaseColor(9, 105, 218); 

    public static PdfPCell crearCeldaCabecera(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_CABECERA));
        celda.setBackgroundColor(COLOR_PRIMARIO);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(6);
        return celda;
    }

    public static PdfPCell crearCeldaNormal(String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_NORMAL));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(4);
        return celda;
    }

    public static PdfPCell crearCeldaSinBorde(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }
}