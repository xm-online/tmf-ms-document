package com.icthh.xm.tmf.ms.document.service.generation.rendering.jasper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.tmf.ms.document.config.ApplicationProperties;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRenderer;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRendererType;
import com.icthh.xm.tmf.ms.document.service.generation.rendering.exception.DocumentRenderingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
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
    public byte[] render(String key, MediaType mediaType, Object data) throws DocumentRenderingException {
        byte[] jasperTemplateBytes = templateHolder.getJasperTemplateByKey(key);
        InputStream jasperTemplateInputStream = new ByteArrayInputStream(jasperTemplateBytes);
        JsonDataSource dataSource = asJsonDataSource(data);
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperTemplateInputStream, new HashMap<>(), dataSource);
            return exportDocumentTo(jasperPrint, mediaType);
        } catch (JRException e) {
            String msg = String.format("Failed to render document with key '%s'", key);
            log.error(msg, e);
            throw new DocumentRenderingException(msg + ". " + e.getMessage(), e);
        }
    }

    private JsonDataSource asJsonDataSource(Object data) {
        try {
            return new JsonDataSource(new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(data)));
        } catch (JRException | JsonProcessingException e) {
            throw new DocumentRenderingException("Failed to parse document data as JSON. " + e.getMessage(), e);
        }
    }

    private byte[] exportDocumentTo(JasperPrint jasperPrint, MediaType mediaType) throws JRException {
        if (APPLICATION_PDF.equals(mediaType)) {
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
        if (TEXT_XML.equals(mediaType) || APPLICATION_XML.equals(mediaType)) {
            return JasperExportManager.exportReportToXml(jasperPrint).getBytes(UTF_8);
        }
        throw new IllegalArgumentException(mediaType.toString() + " not supported by JasperReports");
    }

    @Override
    public DocumentRendererType getType() {
        return DocumentRendererType.JASPER_REPORTS;
    }
}
