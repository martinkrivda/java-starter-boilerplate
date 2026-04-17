package com.example.javastarterboilerplate.infrastructure.document;

import com.example.javastarterboilerplate.domain.document.DigitalSignatureDescriptor;
import com.example.javastarterboilerplate.domain.document.DigitalSignaturePreparationResult;
import com.example.javastarterboilerplate.domain.document.DigitalSignatureService;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.DSSDocument;
import jakarta.inject.Singleton;

@Singleton
public class DssDigitalSignatureService implements DigitalSignatureService {

    private final DssProperties dssProperties;

    public DssDigitalSignatureService(DssProperties dssProperties) {
        this.dssProperties = dssProperties;
    }

    @Override
    public DigitalSignatureDescriptor describe() {
        return new DigitalSignatureDescriptor("dss",
                dssProperties.isEnabled()
                        ? "DSS integration is wired for future PAdES/CAdES implementation"
                        : "DSS integration is disabled");
    }

    @Override
    public DigitalSignaturePreparationResult prepare(byte[] documentBytes, String fileName) {
        DSSDocument document = new InMemoryDocument(documentBytes, fileName, MimeTypeEnum.PDF);
        return new DigitalSignaturePreparationResult("dss", fileName, documentBytes.length,
                document.getMimeType().getMimeTypeString());
    }
}
