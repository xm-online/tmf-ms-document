package com.icthh.xm.tmf.ms.document.service.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

/**
 * Convert string to {@link MediaType} by parsing it with {@link MediaType#parseMediaType}.
 */
public class MediaTypeConverter implements Converter<String, MediaType> {

    @Override
    public MediaType convert(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return MediaType.parseMediaType(value);
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(String.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(MediaType.class);
    }
}
