package com.niyiment.patientservice.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility class for generating QR codes for patient identification.
 */
@Component
@Slf4j
public class QRCodeGenerator {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    /**
     * Generates a QR code for a patient ID.
     *
     * @param patientId the patient UUID
     * @return Base64-encoded PNG image of the QR code
     */
    public String generateQRCode(UUID patientId) {
        try {
            String qrContent = formatQRContent(patientId);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            byte[] qrCodeBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for patient: {}", patientId, e);
            throw new QRCodeGenerationException("Failed to generate QR code", e);
        }
    }

    /**
     * Formats the QR code content with a standardized structure.
     */
    private String formatQRContent(UUID patientId) {
        return String.format("PATIENT:%s", patientId.toString());
    }

    /**
     * Validates and extracts patient ID from QR code content.
     *
     * @param qrContent the scanned QR code content
     * @return the patient UUID, or null if invalid
     */
    public UUID extractPatientId(String qrContent) {
        if (qrContent == null || !qrContent.startsWith("PATIENT:")) {
            return null;
        }
        
        try {
            String uuidString = qrContent.substring(8); // Remove "PATIENT:" prefix
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid patient UUID in QR code: {}", qrContent);
            return null;
        }
    }

    public static class QRCodeGenerationException extends RuntimeException {
        public QRCodeGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}