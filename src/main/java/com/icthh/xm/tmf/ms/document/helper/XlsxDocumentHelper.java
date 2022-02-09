package com.icthh.xm.tmf.ms.document.helper;

import groovy.util.logging.Slf4j;
import java.io.ByteArrayOutputStream;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class XlsxDocumentHelper implements ExportDocumentHelper {
    @Override
    public byte[] exportReport(JasperPrint jasperPrint) throws JRException {
        return configureJRXlsxExporter(jasperPrint);
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private byte[] configureJRXlsxExporter(JasperPrint jasperPrint) throws JRException {
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(true);
        configuration.setDetectCellType(true);
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setConfiguration(configuration);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        exporter.exportReport();
        return baos.toByteArray();
    }
}
