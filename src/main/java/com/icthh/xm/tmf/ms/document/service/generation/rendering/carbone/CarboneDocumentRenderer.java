package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone;

import com.icthh.xm.tmf.ms.document.domain.TenantProperties;
import com.icthh.xm.tmf.ms.document.service.TenantPropertiesService;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationSpec;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRenderer;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRendererType;
import com.icthh.xm.tmf.ms.document.service.generation.rendering.exception.DocumentRenderingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.toMultiValueMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarboneDocumentRenderer implements DocumentRenderer {

    private static final String URL_PATH_SEGMENT_RENDER = "render";
    private static final String URL_PATH_SEGMENT_TEMPLATE = "template";

    private final CarboneTemplateHolder templateHolder;
    private final TenantPropertiesService tenantPropertiesService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public byte[] render(String key,
                         MediaType mediaType,
                         Object data,
                         List<DocumentGenerationSpec.SubDocument> subDocuments) throws DocumentRenderingException {
        TenantProperties tenantProperties = tenantPropertiesService.getTenantProps();
        byte[] template = templateHolder.getTemplateByKey(key);

        String baseUrl = tenantProperties.getRenderer().getCarbone().getUrl();
        HttpHeaders headers = mapCarboneHeaders(tenantProperties.getRenderer().getCarbone().getHeaders());

        Map<String, Object> requestBody = mapRenderRequestBody(data, template);
        AddRenderTemplateResponse renderResponse = callAddRender(baseUrl, requestBody, headers);

        return callGetDocument(baseUrl, renderResponse.data.renderId, headers);
    }

    @Override
    public DocumentRendererType getType() {
        return DocumentRendererType.CARBONE;
    }

    private AddRenderTemplateResponse callAddRender(String baseUrl,
                                                    Map<String, Object> requestBody,
                                                    HttpHeaders headers) {
        var url = collectUrl(baseUrl, URL_PATH_SEGMENT_RENDER, URL_PATH_SEGMENT_TEMPLATE);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, AddRenderTemplateResponse.class).getBody();
    }

    private byte[] callGetDocument(String baseUrl, String renderId, HttpHeaders headers) {
        String url = collectUrl(baseUrl, URL_PATH_SEGMENT_RENDER, renderId);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, byte[].class).getBody();
    }

    private String collectUrl(String baseUrl, String... pathSegments) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment(pathSegments).toUriString();
    }

    private HttpHeaders mapCarboneHeaders(Map<String, String> headers) {
        if (headers == null) {
            return new HttpHeaders();
        }

        Map<String, List<String>> collect = headers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, v -> Arrays.asList(v.getValue().split(", "))));

        return new HttpHeaders(toMultiValueMap(collect));
    }

    private Map<String, Object> mapRenderRequestBody(Object data, byte[] template) {
        Map<String, Object> requestData = toMap(data);
        requestData.put("template", encodedContent(template));
        return requestData;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object data) {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        throw new IllegalArgumentException("Unexpected document data type: " + data.getClass());
    }

    private String encodedContent(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

}
