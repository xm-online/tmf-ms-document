package com.icthh.xm.tmf.ms.document.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"renderer"})
@Getter
@Setter
@ToString
public class TenantConfigDocumentProperties {

    @JsonProperty("renderer")
    private Renderer renderer = new Renderer();

    @Getter
    @Setter
    @ToString
    public static class Renderer {

        @JsonProperty("carbone")
        private Carbone carbone;

        @Getter
        @Setter
        @ToString
        public static class Carbone {

            @JsonProperty("url")
            private String url;

            @JsonProperty("headers")
            private Map<String, String> headers;

        }
    }
}
