package com.icthh.xm.tmf.ms.document.util;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.icthh.xm.commons.tenant.TenantContext;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantKey;
import com.icthh.xm.tmf.ms.document.service.generation.GeneratedDocument;
import com.icthh.xm.tmf.ms.document.web.rest.DocumentGenerationResource.GenerateDocumentRequest;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

@UtilityClass
public class TestDataUtils {

    public static TenantContextHolder mockTenantContextHolder(String tenantKey) {
        TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        TenantContext tenantContext = mock(TenantContext.class);
        doReturn(Optional.of(new TenantKey(tenantKey))).when(tenantContext).getTenantKey();
        doReturn(tenantContext).when(tenantContextHolder).getContext();
        return tenantContextHolder;
    }

    public static GenerateDocumentRequest createGenerateDocumentRequest(String key, Object context, MediaType mediaType) {
        GenerateDocumentRequest request = new GenerateDocumentRequest();
        request.setKey(key);
        request.setContext(context);
        request.setDocumentMimeType(mediaType);
        return request;
    }

    public static GeneratedDocument createGeneratedDocument(String filename, MediaType mediaType, byte[] content) {
        GeneratedDocument generatedDocument = new GeneratedDocument();
        generatedDocument.setFilename(filename);
        generatedDocument.setContentType(mediaType);
        generatedDocument.setDocumentByteResource(new ByteArrayResource(content));
        return generatedDocument;
    }
}
