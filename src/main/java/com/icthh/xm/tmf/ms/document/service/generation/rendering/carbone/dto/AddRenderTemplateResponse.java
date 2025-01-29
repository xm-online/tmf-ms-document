package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRenderTemplateResponse {

    private boolean success;

    private AddRenderTemplateResponseData data;

    @Getter
    @Setter
    public static class AddRenderTemplateResponseData {
        private String renderId;
    }
}
