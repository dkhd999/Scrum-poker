package controlador;

import java.awt.Color;
import java.io.File;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import modelo.HistoriaUsuario;
import modelo.MetricasVoto;
import modelo.ReporteSala;
import modelo.Voto;

public class ReportePDF {

	private ReportePDF() { }

	public static void generar(ReporteSala reporte, File archivo) throws Exception {
		if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
			archivo = new File(archivo.getAbsolutePath() + ".pdf");
		}

		Document documento = new Document(PageSize.A4, 36, 36, 42, 42);
		PdfWriter.getInstance(documento, new java.io.FileOutputStream(archivo));
		try {
			documento.open();
			agregarEncabezado(documento, reporte);
			agregarResumen(documento, reporte);
			agregarHistorias(documento, reporte);
			agregarVotos(documento, reporte);
		} finally {
			documento.close();
		}
	}

	private static void agregarEncabezado(Document documento, ReporteSala reporte) throws DocumentException {
		Font titulo = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(31, 78, 121));
		Paragraph encabezado = new Paragraph("REPORTE DE SCRUM POKER", titulo);
		encabezado.setAlignment(Element.ALIGN_CENTER);
		documento.add(encabezado);
		Paragraph sala = new Paragraph("Sala " + valor(reporte.getCodigoSala())
				+ "  |  ID: " + reporte.getIdSala(), fuente(11, Font.NORMAL));
		sala.setAlignment(Element.ALIGN_CENTER);
		documento.add(sala);
		documento.add(new Paragraph(" "));
	}

	private static void agregarResumen(Document documento, ReporteSala reporte) throws DocumentException {
		agregarTitulo(documento, "Resumen de la votación");
		PdfPTable tabla = new PdfPTable(2);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[]{1.4f, 2.6f});
		agregarFila(tabla, "Estado de cartas", reporte.isCartasReveladas() ? "Reveladas" : "Ocultas");
		MetricasVoto metricas = reporte.getMetricas();
		agregarFila(tabla, "Promedio", formatear(metricas.getPromedio()));
		agregarFila(tabla, "Mediana", formatear(metricas.getMediana()));
		agregarFila(tabla, "Consenso", valor(metricas.getConsenso()));
		documento.add(tabla);
		documento.add(new Paragraph(" "));
	}

	private static void agregarHistorias(Document documento, ReporteSala reporte) throws DocumentException {
		agregarTitulo(documento, "Historias de usuario");
		PdfPTable tabla = new PdfPTable(5);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[]{1.3f, 2.5f, 1.2f, 1.1f, 1.0f});
		String[] encabezados = {"ID", "Título y descripción", "Prioridad", "Puntos", "Estado"};
		for (String encabezado : encabezados) agregarEncabezado(tabla, encabezado);
		for (HistoriaUsuario historia : reporte.getHistorias()) {
			agregarCelda(tabla, String.valueOf(historia.getIdHistoria()));
			agregarCelda(tabla, valor(historia.getTitulo()) + "\n" + valor(historia.getDescripcion()));
			agregarCelda(tabla, valor(historia.getPrioridad()));
			agregarCelda(tabla, valor(historia.getPuntosEstimados()));
			agregarCelda(tabla, historia.isActiva() ? "Activa" : "Cerrada");
		}
		documento.add(tabla);
		documento.add(new Paragraph(" "));
	}

	private static void agregarVotos(Document documento, ReporteSala reporte) throws DocumentException {
		agregarTitulo(documento, "Votos y participantes");
		PdfPTable tabla = new PdfPTable(3);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[]{2.5f, 2.0f, 1.5f});
		String[] encabezados = {"Nickname", "Rol", "Carta"};
		for (String encabezado : encabezados) agregarEncabezado(tabla, encabezado);
		for (Voto voto : reporte.getVotos()) {
			agregarCelda(tabla, valor(voto.getNickname()));
			agregarCelda(tabla, "PRODUCT_OWNER_MODERADOR".equals(voto.getRol()) ? "Moderador" : "Votante");
			agregarCelda(tabla, voto.isCartasReveladas() ? valor(voto.getCarta()) : "Oculta");
		}
		documento.add(tabla);
	}

	private static void agregarTitulo(Document documento, String texto) throws DocumentException {
		Font fuente = fuente(13, Font.BOLD, new Color(31, 78, 121));
		documento.add(new Paragraph(texto, fuente));
	}

	private static void agregarEncabezado(PdfPTable tabla, String texto) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, fuente(10, Font.BOLD, Color.WHITE)));
		celda.setBackgroundColor(new Color(31, 78, 121));
		celda.setHorizontalAlignment(Element.ALIGN_CENTER);
		celda.setPadding(6);
		tabla.addCell(celda);
	}

	private static void agregarFila(PdfPTable tabla, String etiqueta, String valor) {
		agregarCelda(tabla, etiqueta, true);
		agregarCelda(tabla, valor, false);
	}

	private static void agregarCelda(PdfPTable tabla, String texto) {
		agregarCelda(tabla, texto, false);
	}

	private static void agregarCelda(PdfPTable tabla, String texto, boolean destacado) {
		PdfPCell celda = new PdfPCell(new Phrase(valor(texto), fuente(9, destacado ? Font.BOLD : Font.NORMAL)));
		celda.setPadding(5);
		celda.setVerticalAlignment(Element.ALIGN_TOP);
		tabla.addCell(celda);
	}

	private static Font fuente(float tamaño, int estilo) {
		return fuente(tamaño, estilo, Color.BLACK);
	}

	private static Font fuente(float tamaño, int estilo, Color color) {
		return new Font(Font.HELVETICA, tamaño, estilo, color);
	}

	private static String valor(String texto) { return texto == null || texto.isEmpty() ? "-" : texto; }
	private static String formatear(Double valor) {
		return valor == null ? "-" : String.format(java.util.Locale.US, "%.2f", valor);
	}
}
