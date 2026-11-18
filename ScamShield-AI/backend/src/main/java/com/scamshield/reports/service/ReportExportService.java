package com.scamshield.reports.service;

import org.springframework.stereotype.Service;

import com.scamshield.reports.util.ReportPdfGenerator;

@Service
public class ReportExportService {
    private final ReportAccessService accessService;
    private final ReportPdfGenerator pdfGenerator;

    public ReportExportService(ReportAccessService accessService, ReportPdfGenerator pdfGenerator) {
        this.accessService = accessService;
        this.pdfGenerator = pdfGenerator;
    }

    public byte[] download(String ownerId, String reportId) {
        return pdfGenerator.generate(accessService.get(ownerId, reportId));
    }
}
