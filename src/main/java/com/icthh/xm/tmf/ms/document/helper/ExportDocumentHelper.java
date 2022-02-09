package com.icthh.xm.tmf.ms.document.helper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.http.MediaType;

public interface ExportDocumentHelper {

    byte[] exportReport(JasperPrint jasperPrint) throws JRException;

    MediaType getMediaType();
}
