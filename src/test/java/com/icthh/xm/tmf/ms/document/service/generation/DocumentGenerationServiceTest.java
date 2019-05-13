package com.icthh.xm.tmf.ms.document.service.generation;

import static com.icthh.xm.tmf.ms.document.util.TestDataUtils.mockTenantContextHolder;
import static com.icthh.xm.tmf.ms.document.web.rest.TestUtil.resourceFileAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.tmf.ms.document.web.rest.errors.InternalServerErrorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerationServiceTest {

    private DocumentGenerationContextMappingService mappingService;

    private DocumentRenderingService renderingService;

    private TenantContextHolder tenantContextHolder;

    private DocumentGenerationSpecificationHolder specificationHolder;

    private DocumentGenerationService documentGenerationService;

    private String key;
    private Object context;
    private MediaType mediaType;
    private byte[] renderedDocumentBytes;
    private String specFileConfigKey;

    @Before
    public void setUp() throws Exception {
        initTestData();
        mockDependencies();
        initSpecificationHolder();
    }

    @Test
    public void generate() {
        GeneratedDocument result = documentGenerationService.generate(key, context, mediaType);

        assertThat(result.getFilename()).isEqualTo(key.toLowerCase());
        assertThat(result.getContentType()).isEqualTo(mediaType);
        assertThat(result.getDocumentByteResource()).isEqualTo(new ByteArrayResource(renderedDocumentBytes));
    }

    @Test
    public void generate_DocumentMimeTypeNotSupported() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> documentGenerationService.generate(key, context, MediaType.ALL))
            .withMessageContaining("mime type not supported");
    }

    @Test
    public void generate_FailedToProcessContext() {
        doThrow(IllegalStateException.class)
            .when(mappingService)
            .toRenderingData(anyString(), any());

        assertThatExceptionOfType(InternalServerErrorException.class)
            .isThrownBy(() -> documentGenerationService.generate(key, context, mediaType))
            .withMessageStartingWith("Failed to process document context");
    }

    @Test
    public void generate_FailedToRenderDocument() {
        doThrow(IllegalStateException.class).when(renderingService).render(anyString(), any(), any(), any());

        assertThatExceptionOfType(InternalServerErrorException.class)
            .isThrownBy(() -> documentGenerationService.generate(key, context, mediaType))
            .withMessageStartingWith("Failed to render document");
    }

    @Test
    public void generate_SpecificationNotExists() {
        specificationHolder.onRefresh(specFileConfigKey, null);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> documentGenerationService.generate(key, context, mediaType))
            .withMessageStartingWith("Document generation specification not found");
    }

    private void initSpecificationHolder() throws IOException {
        ReflectionTestUtils.setField(specificationHolder, "specificationPathPattern", "/config/tenants/{tenantName}/document/documents.yml");
        specFileConfigKey = "/config/tenants/TEST/document/documents.yml";
        String specYaml = resourceFileAsString("spec/documents.yml");
        specificationHolder.onInit(specFileConfigKey, specYaml);
    }

    private void initTestData() {
        key = "TEST_DOCUMENT";
        context = createTestContext();
        mediaType = MediaType.APPLICATION_PDF;
        renderedDocumentBytes = new byte[5];
    }

    private void mockDependencies() {
        mappingService = spy(new DocumentGenerationContextMappingService());
        tenantContextHolder = mockTenantContextHolder("TEST");
        specificationHolder = spy(new DocumentGenerationSpecificationHolder(tenantContextHolder));
        renderingService = mockRenderingService();
        documentGenerationService = new DocumentGenerationService(mappingService, renderingService, specificationHolder);
    }

    private DocumentRenderingService mockRenderingService() {
        DocumentRenderingService renderingService = mock(DocumentRenderingService.class);
        doReturn(renderedDocumentBytes).when(renderingService).render(eq(key), any(), any(), any());
        return renderingService;
    }

    private Object createTestContext() {
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("first field", "First field value");
        contextMap.put("second field", "Second field value");
        return contextMap;
    }
}
