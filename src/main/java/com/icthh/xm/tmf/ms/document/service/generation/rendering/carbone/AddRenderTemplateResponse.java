package com.icthh.xm.tmf.ms.document.service.generation.rendering.carbone;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRenderTemplateResponse {

    boolean success;

    AddRenderTemplateResponseData data;

    @Getter
    @Setter
    public static class AddRenderTemplateResponseData {
        String renderId;
    }
}
