package com.icthh.xm.tmf.ms.document.helper;

import static java.nio.charset.StandardCharsets.UTF_8;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TextXmlDocumentHelper implements ExportDocumentHelper {
    @Override
    public byte[] exportReport(JasperPrint jasperPrint) throws JRException {
        return JasperExportManager.exportReportToXml(jasperPrint).getBytes(UTF_8);
    }

    @Override
    public MediaType getMediaType() {
        return MediaType.TEXT_XML;
    }
}
