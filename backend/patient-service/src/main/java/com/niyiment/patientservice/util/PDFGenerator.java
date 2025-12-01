package com.niyiment.patientservice.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.niyiment.patientservice.entity.Guardian;
import com.niyiment.patientservice.entity.Patient;
import com.niyiment.patientservice.entity.PatientProgram;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Utility class for generating PDF documents for patient records.
 */
@Component
@Slf4j
public class PDFGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /**
     * Generates a PDF for a patient's complete record.
     *
     * @param patient the patient entity
     * @return Base64-encoded PDF document
     */
    public String generatePatientRecordPDF(Patient patient) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            addTitle(document);
            addPatientInformation(document, patient);
            addGuardianInformation(document, patient);
            addProgramInformation(document, patient);
            document.close();

            byte[] pdfBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pdfBytes);
        } catch (Exception e) {
            log.error("Failed to generate PDF for patient: {}", patient.getId(), e);
            throw new PDFGenerationException("Failed to generate PDF", e);
        }
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Patient Medical Record", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private void addPatientInformation(Document document, Patient patient) throws DocumentException {
        document.add(new Paragraph("Patient Information", HEADER_FONT));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableRow(table, "Patient ID:", patient.getId().toString());
        addTableRow(table, "Name:", patient.getFirstName() + " " + patient.getLastName());
        addTableRow(table, "Date of Birth:", patient.getDateOfBirth().format(DATE_FORMATTER));
        addTableRow(table, "Gender:", patient.getGender() != null ? patient.getGender().name() : "N/A");
        addTableRow(table, "Patient Type:", patient.getPatientType().name());
        addTableRow(table, "National ID:", patient.getNationalId() != null ? patient.getNationalId() : "N/A");
        addTableRow(table, "Phone:", patient.getPhone() != null ? patient.getPhone() : "N/A");
        addTableRow(table, "Email:", patient.getEmail() != null ? patient.getEmail() : "N/A");
        addTableRow(table, "Address:", patient.getAddress() != null ? patient.getAddress() : "N/A");
        addTableRow(table, "State:", patient.getState() != null ? patient.getState() : "N/A");
        addTableRow(table, "LGA:", patient.getLga() != null ? patient.getLga() : "N/A");
        addTableRow(table, "Registered:", patient.getCreatedAt().format(DATETIME_FORMATTER));

        document.add(table);
    }

    private void addGuardianInformation(Document document, Patient patient) throws DocumentException {
        if (patient.getGuardians() == null || patient.getGuardians().isEmpty()) {
            return;
        }

        document.add(new Paragraph("Guardian Information", HEADER_FONT));
        document.add(Chunk.NEWLINE);

        for (Guardian guardian : patient.getGuardians()) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingAfter(10);

            addTableRow(table, "Name:", guardian.getName());
            addTableRow(table, "Relationship:", guardian.getRelationship() != null ? guardian.getRelationship() : "N/A");
            addTableRow(table, "Phone:", guardian.getPhone() != null ? guardian.getPhone() : "N/A");
            addTableRow(table, "Email:", guardian.getEmail() != null ? guardian.getEmail() : "N/A");
            addTableRow(table, "Primary:", guardian.getIsPrimary() ? "Yes" : "No");

            document.add(table);
        }
    }

    private void addProgramInformation(Document document, Patient patient) throws DocumentException {
        if (patient.getPrograms() == null || patient.getPrograms().isEmpty()) {
            return;
        }

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Vaccination Programs", HEADER_FONT));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        
        addHeaderCell(table, "Program Code");
        addHeaderCell(table, "Program Name");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Enrolled");

        for (PatientProgram program : patient.getPrograms()) {
            addTableCell(table, program.getProgramCode());
            addTableCell(table, program.getProgramName() != null ? program.getProgramName() : "N/A");
            addTableCell(table, program.getStatus().name());
            addTableCell(table, program.getEnrolledAt().format(DATETIME_FORMATTER));
        }

        document.add(table);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(5);
        table.addCell(cell);
    }

    public static class PDFGenerationException extends RuntimeException {
        public PDFGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}