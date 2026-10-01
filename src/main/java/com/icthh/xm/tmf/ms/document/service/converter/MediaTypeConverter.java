package com.icthh.xm.tmf.ms.document.service.converter;

import static com.icthh.xm.tmf.ms.document.web.rest.util.MediaTypeUtil.parseMediaType;

import org.springframework.http.MediaType;
import tools.jackson.databind.util.StdConverter;

/**
 * Convert string to {@link MediaType} by parsing it with {@link MediaType#parseMediaType}.
 */
public class MediaTypeConverter extends StdConverter<String, MediaType> {

    @Override
    public MediaType convert(String value) {
        return parseMediaType(value);
    }
}
