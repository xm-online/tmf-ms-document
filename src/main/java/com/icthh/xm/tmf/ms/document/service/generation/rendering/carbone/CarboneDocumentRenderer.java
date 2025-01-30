package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.tmf.ms.document.config.ApplicationProperties;
import com.icthh.xm.tmf.ms.document.domain.TenantConfigDocumentProperties;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentGenerationSpec;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRenderer;
import com.icthh.xm.tmf.ms.document.service.generation.DocumentRendererType;
import com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone.dto.AddRenderTemplateRequest;
import com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone.dto.AddRenderTemplateResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.util.CollectionUtils.toMultiValueMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarboneDocumentRenderer implements DocumentRenderer {

    private static final String URL_PATH_SEGMENT_RENDER = "render";
    private static final String URL_PATH_SEGMENT_TEMPLATE = "template";

    private static final String TENANT_CONFIG_KEY = "document";

    private final CarboneTemplateHolder templateHolder;
    private final TenantConfigService tenantConfigService;
    private final RestTemplate vanillaRestTemplate;
    private final ApplicationProperties applicationProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] render(String key,
                         MediaType mediaType,
                         Object data,
                         List<DocumentGenerationSpec.SubDocument> subDocuments) throws DocumentRenderingException {
        TenantConfigDocumentProperties tenantProperties = resolveTenantProperties();
        String template = templateHolder.getTemplateByKey(key);

        String baseUrl = tenantProperties.getRenderer().getCarbone().getUrl();
        HttpHeaders headers = mapCarboneHeaders(tenantProperties.getRenderer().getCarbone().getHeaders());

        AddRenderTemplateRequest requestBody = mapRenderRequestBody(key, mediaType, data, template);
        AddRenderTemplateResponse renderResponse = callAddRender(baseUrl, requestBody, headers);

        return callGetDocument(baseUrl, renderResponse.getData().getRenderId(), headers);
    }

    @Override
    public DocumentRendererType getType() {
        return DocumentRendererType.CARBONE;
    }

    private TenantConfigDocumentProperties resolveTenantProperties() {
        Object properties = tenantConfigService.getConfig().get(TENANT_CONFIG_KEY);
        return objectMapper.convertValue(properties, TenantConfigDocumentProperties.class);
    }

    private HttpHeaders mapCarboneHeaders(Map<String, String> headers) {
        headers = enrichWithDefaultCarboneHeaders(headers);

        Map<String, List<String>> collect = headers.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, v -> Arrays.asList(v.getValue().split(", "))));

        return new HttpHeaders(toMultiValueMap(collect));
    }

    private Map<String, String> enrichWithDefaultCarboneHeaders(Map<String, String> headers) {
        headers = requireNonNullElse(headers, new HashMap<>());
        headers.putIfAbsent(
            applicationProperties.getCarbone().getApiVersionKey(),
            applicationProperties.getCarbone().getApiVersionValue()
        );
        return headers;
    }

    private AddRenderTemplateRequest mapRenderRequestBody(String key,
                                                          MediaType mediaType,
                                                          Object data,
                                                          String template) {
        var request = objectMapper.convertValue(data, AddRenderTemplateRequest.class);

        request.setReportName(requireNonNullElse(request.getConvertTo(), key));
        request.setConvertTo(requireNonNullElse(request.getConvertTo(), mediaType.getSubtype()));
        request.setTimezone(requireNonNullElse(request.getConvertTo(), TimeZone.getDefault().toZoneId().getId()));

        request.setTemplate(template);
        return request;
    }

    private AddRenderTemplateResponse callAddRender(String baseUrl,
                                                    AddRenderTemplateRequest requestBody,
                                                    HttpHeaders headers) {
        String url = collectUrl(baseUrl, URL_PATH_SEGMENT_RENDER, URL_PATH_SEGMENT_TEMPLATE);
        HttpEntity<AddRenderTemplateRequest> request = new HttpEntity<>(requestBody, headers);
        return vanillaRestTemplate.exchange(url, HttpMethod.POST, request, AddRenderTemplateResponse.class).getBody();
    }

    private byte[] callGetDocument(String baseUrl, String renderId, HttpHeaders headers) {
        String url = collectUrl(baseUrl, URL_PATH_SEGMENT_RENDER, renderId);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
        return vanillaRestTemplate.exchange(url, HttpMethod.GET, request, byte[].class).getBody();
    }

    private String collectUrl(String baseUrl, String... pathSegments) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment(pathSegments).toUriString();
    }

}
