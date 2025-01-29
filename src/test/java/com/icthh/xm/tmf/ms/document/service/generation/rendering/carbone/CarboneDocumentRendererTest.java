package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.icthh.xm.tmf.ms.document.util.TestDataUtils.mockTenantContextHolder;
import static com.icthh.xm.tmf.ms.document.web.rest.TestUtil.resourceFileAsBase64String;
import static com.icthh.xm.tmf.ms.document.web.rest.TestUtil.resourceFileAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CarboneDocumentRendererTest {

    private static final String KEY = "TEST_DOCUMENT_CARBONE";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CarboneTemplateHolder templateHolder;
    private TenantConfigService tenantConfigService;
    private RestTemplate restTemplate;

    private CarboneDocumentRenderer renderer;

    private Object data;
    private MediaType mediaType;
    private String templateFileConfigKey;

    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, Object>>> request;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        data = readJsonData();

        templateHolder = spy(new CarboneTemplateHolder(mockTenantContextHolder("TEST")));
        tenantConfigService = mock(TenantConfigService.class);
        restTemplate = mock(RestTemplate.class);
        renderer = new CarboneDocumentRenderer(templateHolder, tenantConfigService, restTemplate);
        mediaType = MediaType.APPLICATION_PDF;

        templateFileConfigKey = buildTemplateFileConfigKey(KEY);
        initTemplateHolderWithTemplate(templateFileConfigKey, "files/test_document_carbone.docx");
    }

    @Test
    public void render_exportToPdf() throws IOException {
        when(tenantConfigService.getConfig()).thenReturn(readConfigData());

        when(restTemplate.exchange(anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(AddRenderTemplateResponse.class))
        ).thenReturn(new ResponseEntity<>(buildRenderResponse(), HttpStatus.OK));

        when(restTemplate.exchange(anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(byte[].class))
        ).thenReturn(new ResponseEntity<>(readCarboneResultResponse(), HttpStatus.OK));

        byte[] renderedDocument = renderer.render(KEY, mediaType, data, null);

        assertThat(renderedDocument).isNotEmpty();

        verify(restTemplate).exchange(anyString(),
            eq(HttpMethod.POST),
            request.capture(),
            eq(AddRenderTemplateResponse.class));

        assertThat(request.getValue().getBody()).isNotNull();
        assertThat(request.getValue().getBody().get("template")).isNotNull();
        assertThat(request.getValue().getBody().get("convertTo")).isEqualTo("pdf");
        assertThat(request.getValue().getHeaders()).isNotNull();
        assertThat(request.getValue().getHeaders().containsKey("carbone-version")).isTrue();
        assertThat(request.getValue().getHeaders().get("carbone-version")).isEqualTo(List.of("4"));
    }

    @Test
    public void render_templateNotExists() {
        templateHolder.onRefresh(templateFileConfigKey, null);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> renderer.render(KEY, mediaType, data, null))
            .withMessage("Template not found for key: " + KEY);
    }

    private void initTemplateHolderWithTemplate(String templateFileConfigKey, String resourceFilePath) {
        ReflectionTestUtils.setField(templateHolder, "carboneTemplatesPathPattern",
            "/config/tenants/{tenantName}/document/templates/carbone/{filename}.*");

        templateHolder.onInit(templateFileConfigKey, resourceFileAsBase64String(resourceFilePath));
    }

    private String buildTemplateFileConfigKey(String key) {
        return String.format("/config/tenants/TEST/document/templates/carbone/%s.docx",
            DocumentGenerationUtils.buildDocumentFilename(key));
    }

    private Map readJsonData() throws java.io.IOException {
        return objectMapper.readValue(resourceFileAsString("files/test_document_data_carbone.json"),
            Map.class);
    }

    private Map<String, Object> readConfigData() throws java.io.IOException {
        return objectMapper.readValue(resourceFileAsString("files/test_document_tenant_config.json"),
            new TypeReference<>() {});
    }

    private byte[] readCarboneResultResponse() throws java.io.IOException {
        return new ClassPathResource("files/test_carbone_response.pdf").getInputStream().readAllBytes();
    }

    private AddRenderTemplateResponse buildRenderResponse() {
        var renderResponseData = new AddRenderTemplateResponse.AddRenderTemplateResponseData();
        renderResponseData.setRenderId("render_id");

        var renderResponse = new AddRenderTemplateResponse();
        renderResponse.setSuccess(true);
        renderResponse.setData(renderResponseData);
        return renderResponse;
    }

}
