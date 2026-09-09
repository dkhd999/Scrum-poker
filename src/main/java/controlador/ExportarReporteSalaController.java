package controlador;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import modelo.HistoriaUsuario;
import modelo.MetricasVoto;
import modelo.ReporteSala;
import modelo.Voto;

public class ExportarReporteSalaController {

    private final int idSala;

    public ExportarReporteSalaController(int idSala) {
        this.idSala = idSala;
    }

    public void exportar(javax.swing.JFrame padre) {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("reporte_sala_" + idSala + ".pdf"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return;

        try {
            ReporteSala reporte = ReporteSala.obtener(idSala);
            generarPdf(reporte, selector.getSelectedFile());
            JOptionPane.showMessageDialog(padre, "Reporte exportado correctamente.",
                    "Exportación", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, "No se pudo exportar el reporte:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarPdf(ReporteSala reporte, File archivo) throws Exception {
        Document documento = new Document();
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph("Reporte de Scrum Poker"));
            documento.add(new Paragraph("Sala: " + reporte.getCodigoSala() +
                    " (ID: " + reporte.getIdSala() + ")"));
            documento.add(new Paragraph("Cartas reveladas: " +
                    (reporte.isCartasReveladas() ? "Sí" : "No")));
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph("Métricas"));
            MetricasVoto metricas = reporte.getMetricas();
            PdfPTable tablaMetricas = new PdfPTable(3);
            tablaMetricas.addCell(new Phrase("Promedio"));
            tablaMetricas.addCell(new Phrase("Mediana"));
            tablaMetricas.addCell(new Phrase("Consenso"));
            tablaMetricas.addCell(formatear(metricas.getPromedio()));
            tablaMetricas.addCell(formatear(metricas.getMediana()));
            tablaMetricas.addCell(metricas.getConsenso() == null ? "-" : metricas.getConsenso());
            documento.add(tablaMetricas);
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph("Historias de usuario"));
            PdfPTable tablaHistorias = new PdfPTable(4);
            for (String encabezado : new String[]{"Título", "Descripción", "Prioridad", "Puntos"}) {
                tablaHistorias.addCell(encabezado);
            }
            for (HistoriaUsuario historia : reporte.getHistorias()) {
                tablaHistorias.addCell(valor(historia.getTitulo()));
                tablaHistorias.addCell(valor(historia.getDescripcion()));
                tablaHistorias.addCell(valor(historia.getPrioridad()));
                tablaHistorias.addCell(valor(historia.getPuntosEstimados()));
            }
            documento.add(tablaHistorias);
            documento.add(new Paragraph(" "));

            documento.add(new Paragraph("Votos y participantes"));
            PdfPTable tablaVotos = new PdfPTable(4);
            for (String encabezado : new String[]{"Nickname", "Rol", "Carta", "Estado"}) {
                tablaVotos.addCell(encabezado);
            }
            for (Voto voto : reporte.getVotos()) {
                tablaVotos.addCell(valor(voto.getNickname()));
                tablaVotos.addCell("PRODUCT_OWNER_MODERADOR".equals(voto.getRol()) ? "Moderador" : "Votante");
                tablaVotos.addCell(voto.isCartasReveladas() ? valor(voto.getCarta()) : "Oculta");
                tablaVotos.addCell(voto.isCartasReveladas() ? "Revelado" : "Pendiente");
            }
            documento.add(tablaVotos);
        } finally {
            documento.close();
        }
    }

    private String valor(String texto) { return texto == null || texto.isEmpty() ? "-" : texto; }
    private String formatear(Double valor) { return valor == null ? "-" : String.format("%.2f", valor); }
}