package com.icthh.xm.tmf.ms.document.service.generation.rendering.jasper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.tmf.ms.document.config.ApplicationProperties;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationSpec.SubDocument;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRenderer;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRendererType;
import com.icthh.xm.tmf.ms.document.service.generation.rendering.exception.DocumentRenderingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRXlsxDataSource;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Document renderer that uses JasperReports for document rendering.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JasperReportsDocumentRenderer implements DocumentRenderer {

    private final JasperReportsTemplateHolder templateHolder;

    /**
     * Look for jasper template file, feel it with {@code data} content and export
     * to supported {@code mediaType} format.
     *
     * @see DocumentRenderer#render
     * @see ApplicationProperties.DocumentGeneration properties.
     */
    @Override
    public byte[] render(String key, MediaType mediaType, Object data, List<SubDocument> subDocuments) throws DocumentRenderingException {
        byte[] jasperTemplateBytes = templateHolder.getJasperTemplateByKey(key);
        InputStream jasperTemplateInputStream = new ByteArrayInputStream(jasperTemplateBytes);

        Map<String, Object> parameters = new HashMap<>();
        if (subDocuments != null) {
            fillSubDocumentParams(parameters, subDocuments);
        }

        try {
            JasperPrint jasperPrint = new JasperPrint();
            if (APPLICATION_PDF.equals(mediaType)) {
                JsonDataSource jsonDataSource = asJsonDataSource(data);
                jasperPrint = JasperFillManager.fillReport(jasperTemplateInputStream, parameters, jsonDataSource);
            }
            if (APPLICATION_OCTET_STREAM.equals(mediaType)) {
                JRXlsxDataSource xlsxDataSource = asXlsxDataSource(data);
                jasperPrint = JasperFillManager.fillReport(jasperTemplateInputStream, parameters, xlsxDataSource);
            }
            return exportDocumentTo(jasperPrint, mediaType);
        } catch (JRException e) {
            String msg = String.format("Failed to render document with key '%s'", key);
            log.error(msg, e);
            throw new DocumentRenderingException(msg + ". " + e.getMessage(), e);
        }
    }

    private void fillSubDocumentParams(Map<String, Object> parameters, List<SubDocument> subDocuments) {
        subDocuments.forEach(it -> {
            byte[] jasperTemplateBytes = templateHolder.getJasperTemplateByKey(it.getRefKey());
            InputStream jasperTemplateInputStream = new ByteArrayInputStream(jasperTemplateBytes);

            parameters.put(it.getTemplateInjectionKey(), jasperTemplateInputStream);
        });
    }

    private JsonDataSource asJsonDataSource(Object data) {
        try {
            return new JsonDataSource(new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(data)));
        } catch (JRException | JsonProcessingException e) {
            throw new DocumentRenderingException("Failed to parse document data as JSON. " + e.getMessage(), e);
        }
    }

    private JRXlsxDataSource asXlsxDataSource(Object data) {
        try {
            return new JRXlsxDataSource(new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(data)));
        } catch (JRException | IOException e) {
            throw new DocumentRenderingException("Failed to parse document data as Excel. " + e.getMessage(), e);
        }
    }

    private byte[] exportDocumentTo(JasperPrint jasperPrint, MediaType mediaType) throws JRException {
        if (APPLICATION_OCTET_STREAM.equals(mediaType)) {
            return configureJRXlsxExporter(jasperPrint).toByteArray();
        }
        if (APPLICATION_PDF.equals(mediaType)) {
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
        if (TEXT_XML.equals(mediaType) || APPLICATION_XML.equals(mediaType)) {
            return JasperExportManager.exportReportToXml(jasperPrint).getBytes(UTF_8);
        }
        throw new IllegalArgumentException(mediaType.toString() + " not supported by JasperReports");
    }

    private ByteArrayOutputStream configureJRXlsxExporter(JasperPrint jasperPrint) {
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(true);
        configuration.setDetectCellType(true);
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setConfiguration(configuration);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        exporter.getExporterOutput();
        return baos;
    }

    @Override
    public DocumentRendererType getType() {
        return DocumentRendererType.JASPER_REPORTS;
    }
}
