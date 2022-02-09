package com.icthh.xm.tmf.ms.document.service.generation.rendering.jasper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.tmf.ms.document.config.ApplicationProperties;
import com.icthh.xm.tmf.ms.document.helper.ExportDocumentHelper;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationSpec.SubDocument;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRenderer;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRendererType;
import com.icthh.xm.tmf.ms.document.service.generation.rendering.exception.DocumentRenderingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
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
    private final List<ExportDocumentHelper> exportDocumentHelpersList;
    private Map<MediaType, ExportDocumentHelper> exportDocumentHelpers = new HashMap<>();

    @PostConstruct
    public void setup() {
        exportDocumentHelpersList.forEach(exportDocumentHelper ->
            exportDocumentHelpers.put(exportDocumentHelper.getMediaType(), exportDocumentHelper));
    }

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

        JsonDataSource dataSource = asJsonDataSource(data);
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperTemplateInputStream, parameters, dataSource);
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

    private byte[] exportDocumentTo(JasperPrint jasperPrint, MediaType mediaType) throws JRException {
        if (!exportDocumentHelpers.containsKey(mediaType)) {
            throw new IllegalArgumentException(mediaType.toString() + " not supported by JasperReports");
        }
        return exportDocumentHelpers
            .get(mediaType)
            .exportReport(jasperPrint);
    }

    @Override
    public DocumentRendererType getType() {
        return DocumentRendererType.JASPER_REPORTS;
    }
}
