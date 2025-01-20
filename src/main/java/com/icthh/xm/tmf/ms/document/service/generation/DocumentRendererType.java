package com.icthh.xm.tmf.ms.document.service.generation;

import static com.google.common.collect.ImmutableSortedSet.of;
import static com.icthh.xm.tmf.ms.document.config.Constants.APPLICATION_DOCX;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_XML;

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
@Getter
public enum DocumentRendererType {
    JASPER_REPORTS(of(APPLICATION_PDF, TEXT_XML, APPLICATION_XML, APPLICATION_OCTET_STREAM, APPLICATION_DOCX)),

    CARBONE(of(APPLICATION_PDF));

    private final Set<MediaType> supportedMimeTypes;
}
