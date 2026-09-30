package com.icthh.xm.tmf.ms.document.web.rest.errors;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;

// Jackson 3 puts constructor-bound properties first; keep the order the error body had before the migration
@JsonPropertyOrder({"objectName", "field", "message"})
public class FieldErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String objectName;

    private final String field;

    private final String message;

    public FieldErrorVM(String dto, String field, String message) {
        this.objectName = dto;
        this.field = field;
        this.message = message;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

}
