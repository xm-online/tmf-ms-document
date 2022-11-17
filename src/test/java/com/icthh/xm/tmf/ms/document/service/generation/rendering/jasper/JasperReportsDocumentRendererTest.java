package com.icthh.xm.tmf.ms.document.service.generation.rendering.jasper;

import static com.icthh.xm.tmf.ms.document.util.TestDataUtils.mockTenantContextHolder;
import static com.icthh.xm.tmf.ms.document.web.rest.TestUtil.resourceFileAsString;
import static com.icthh.xm.tmf.ms.document.web.rest.TestUtil.writeFileToBuildResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.tmf.ms.document.config.Constants;
import com.icthh.xm.tmf.ms.document.helper.ApplicationXmlDocumentHelper;
import com.icthh.xm.tmf.ms.document.helper.DocxDocumentHelper;
import com.icthh.xm.tmf.ms.document.helper.PdfDocumentHelper;
import com.icthh.xm.tmf.ms.document.helper.TextXmlDocumentHelper;
import com.icthh.xm.tmf.ms.document.helper.XlsxDocumentHelper;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationSpec.SubDocument;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationUtils;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

public class JasperReportsDocumentRendererTest {

    private JasperReportsTemplateHolder templateHolder;

    private JasperReportsDocumentRenderer renderer;

    private String key;
    private Object data;
    private MediaType mediaType;
    private String templateFileConfigKey;

    @Before
    public void setUp() throws Exception {
        key = "TEST_DOCUMENT";
        data = readJsonData();

        templateHolder = spy(new JasperReportsTemplateHolder(mockTenantContextHolder("TEST")));
        renderer = new JasperReportsDocumentRenderer(templateHolder, List.of(new PdfDocumentHelper(),
            new TextXmlDocumentHelper(), new ApplicationXmlDocumentHelper(), new XlsxDocumentHelper(), new DocxDocumentHelper()));
        renderer.setup();
        mediaType = MediaType.APPLICATION_PDF;
        templateFileConfigKey = buildTemplateFileConfigKey(key);

        initTemplateHolderWithTemplate(templateFileConfigKey, "files/test_document.jrxml");
        initTemplateHolderWithTemplate(buildTemplateFileConfigKey("TEST_PRIMARY_DOCUMENT"),
            "files/test_primary_document.jrxml");
        initTemplateHolderWithTemplate(buildTemplateFileConfigKey("TEST_SUB_DOCUMENT"),
            "files/test_sub_document.jrxml");
    }

    @Test
    public void render_exportToPdf() {
        byte[] renderedDocument = renderer.render(key, mediaType, data, null);
        assertThat(renderedDocument).isNotEmpty();
        // see rendered file in build/resources/files/test_document_result.pdf
        writeFileToBuildResource(renderedDocument, "files", "test_document_result.pdf");
    }

    @Test
    public void render_exportToXlsx() {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
        byte[] renderedDocument = renderer.render(key, mediaType, data, null);

        assertThat(renderedDocument).isNotEmpty();
        // see rendered file in build/resources/files/test_document_result.xlsx
        writeFileToBuildResource(renderedDocument, "files", "test_document_result.xlsx");
    }

    @Test
    public void render_exportToDocx() {
        mediaType = Constants.APPLICATION_DOCX;
        byte[] renderedDocument = renderer.render(key, mediaType, data, null);

        assertThat(renderedDocument).isNotEmpty();
        // see rendered file in build/resources/test/files/test_document_result.docx
        writeFileToBuildResource(renderedDocument, "files", "test_document_result.docx");
    }

    @Test
    public void render_withSubDocument_exportToPdf() {
        List<SubDocument> subDocuments = List.of(buildSubDocument());
        byte[] renderedDocument = renderer.render("TEST_PRIMARY_DOCUMENT", mediaType, data, subDocuments);

        assertThat(renderedDocument).isNotEmpty();
        // see rendered file in build/resources/files/test_sub_document_result.pdf
        writeFileToBuildResource(renderedDocument, "files", "test_sub_document_result.pdf");
    }

    @Test
    public void render_exportToXml() {
        byte[] renderedDocument = renderer.render(key, MediaType.TEXT_XML, data, null);

        assertThat(renderedDocument).isNotEmpty();
        // see rendered file in build/resources/files/test_document_result.pdf
        writeFileToBuildResource(renderedDocument, "files", "test_document_result.xml");
    }

    @Test
    public void render_templateNotExists() {
        templateHolder.onRefresh(templateFileConfigKey, null);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> renderer.render(key, mediaType, data, null))
            .withMessageStartingWith("JasperReports template not found");
    }

    @Test
    public void render_invalidTemplate() {
        // expect no exception raised, only logged
        templateHolder.onRefresh(templateFileConfigKey, "invalid template file");

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> renderer.render(key, mediaType, data, null));
    }

    private void initTemplateHolderWithTemplate(String templateFileConfigKey, String resourceFilePath) {
        ReflectionTestUtils.setField(templateHolder, "jasperTemplatesPathPattern",
            "/config/tenants/{tenantName}/document/templates/jasper/{filename}.jrxml");
        templateHolder.onInit(templateFileConfigKey,
            resourceFileAsString(resourceFilePath));
    }

    private String buildTemplateFileConfigKey(String key) {
        return String.format("/config/tenants/TEST/document/templates/jasper/%s.jrxml",
            DocumentGenerationUtils.buildDocumentFilename(key));
    }

    private SubDocument buildSubDocument() {
        SubDocument subDocument = new SubDocument();
        subDocument.setRefKey("TEST_SUB_DOCUMENT");
        subDocument.setTemplateInjectionKey("subDocument");
        return subDocument;
    }

    private Map readJsonData() throws java.io.IOException {
        return new ObjectMapper().readValue(resourceFileAsString("files/test_document_data.json"), Map.class);
    }
}
