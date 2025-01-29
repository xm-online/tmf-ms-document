package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AddRenderTemplateRequest {
    private String reportName;
    private String convertTo;
    private String timezone;
    private String template;
    private Map<String, Object> data;
}
