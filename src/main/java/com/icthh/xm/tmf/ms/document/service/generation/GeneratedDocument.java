package com.icthh.xm.tmf.ms.document.service.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneratedDocument {

    private String filename;

    private MediaType contentType;

    private ByteArrayResource documentByteResource;
}
