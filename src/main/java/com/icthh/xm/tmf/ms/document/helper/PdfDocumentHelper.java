package com.icthh.xm.tmf.ms.document.helper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class PdfDocumentHelper implements ExportDocumentHelper {
    @Override
    public byte[] exportReport(JasperPrint jasperPrint) throws JRException {
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.APPLICATION_PDF;
    }
}
