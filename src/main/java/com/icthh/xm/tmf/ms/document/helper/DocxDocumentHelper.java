package com.icthh.xm.tmf.ms.document.helper;

import groovy.util.logging.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

import static com.icthh.xm.tmf.ms.document.config.Constants.APPLICATION_DOCX;

@Component
@Slf4j
public class DocxDocumentHelper implements ExportDocumentHelper {

    @Override
    public byte[] exportReport(JasperPrint jasperPrint) throws JRException {
        JRDocxExporter exporter = new JRDocxExporter();

        SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
        configuration.setFlexibleRowHeight(true);
        exporter.setConfiguration(configuration);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();
        return outputStream.toByteArray();
    }

    @Override
    public MediaType getMediaType() {
        return APPLICATION_DOCX;
    }

}
