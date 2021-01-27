package com.icthh.xm.tmf.ms.document.service.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationSpec.SubDocumentDetail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

public class DocumentRenderingServiceTest {

    private DocumentRenderingService renderingService;
    private DocumentRendererFactory rendererFactory;
    private DocumentRenderer renderer;

    private MediaType mediaType;

    private DocumentGenerationSpec spec;
    private DocumentRendererType rendererType;

    private byte[] renderedDocumentBytes = new byte[5];
    private String key;

    @Before
    public void setUp() {
        key = "TEST_DOCUMENT";
        mediaType = MediaType.APPLICATION_PDF;
        rendererType = DocumentRendererType.JASPER_REPORTS;
        spec = buildSpec(rendererType);

        renderer = mock(DocumentRenderer.class);
        doReturn(true).when(renderer).supportsMediaType(eq(mediaType));
        doReturn(rendererType).when(renderer).getType();
        rendererFactory = spy(new DocumentRendererFactory(Collections.singletonList(renderer)));
        renderingService = new DocumentRenderingService(rendererFactory);
        doReturn(renderedDocumentBytes).when(renderer).render(anyString(), any(), any(), any());
    }

    @Test
    public void render() {
        byte[] result = renderingService.render(key, spec, mediaType, new Object());

        assertThat(result).isEqualTo(renderedDocumentBytes);
    }

    @Test
    public void render_RendererNotSupportsMediaType() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> renderingService.render(key, spec, MediaType.ALL, new Object()))
            .withMessage(String.format("Renderer of type %s doesn't support '%s' media type", rendererType, MediaType.ALL_VALUE));
    }

    @Test
    public void render_RendererNotFound() {
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> renderingService.render(key, buildSpec(null), mediaType, new Object()))
            .withMessageStartingWith("Renderer implementation not found");
    }

    private DocumentGenerationSpec buildSpec(DocumentRendererType rendererType) {
        DocumentGenerationSpec spec = new DocumentGenerationSpec();
        spec.setRenderer(rendererType);
        spec.setSubDocuments(List.of(buildSubDocument()));
        return spec;
    }

    private SubDocumentDetail buildSubDocument() {
        SubDocumentDetail subDocumentDetail = new SubDocumentDetail();
        subDocumentDetail.setRefKey("TEST_SUB_DOCUMENT");
        subDocumentDetail.setTemplateInjectionKey("subDocument");
        return subDocumentDetail;
    }
}
