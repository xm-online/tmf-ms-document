package com.icthh.xm.tmf.ms.document.service.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.icthh.xm.tmf.ms.document.service.converter.MediaTypeConverter;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;

@Data
public class DocumentGenerationSpec {

    @JsonProperty(access = Access.READ_ONLY)
    private String key;

    @JsonDeserialize(contentConverter = MediaTypeConverter.class)
    private Set<MediaType> allowedDocumentMimeTypes;

    @JsonDeserialize(converter = MediaTypeConverter.class)
    private MediaType defaultDocumentMimeType;

    private DocumentRendererType renderer;

    private List<SubDocumentDetail> subDocuments;

    @Getter
    @Setter
    public static class SubDocumentDetail {
        private String refKey;
        private String templateInjectionKey;
    }

}
