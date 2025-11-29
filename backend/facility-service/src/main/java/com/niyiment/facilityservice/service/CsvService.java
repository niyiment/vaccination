package com.niyiment.facilityservice.service;

import com.niyiment.facilityservice.dto.request.OrganisationCsvDto;
import com.niyiment.facilityservice.entity.Organisation;
import com.niyiment.facilityservice.exception.CsvProcessingException;
import com.niyiment.facilityservice.mapper.OrganisationMapper;
import com.niyiment.facilityservice.repository.OrganisationRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {
    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;

    @Transactional
    public List<Organisation> importCsv(MultipartFile file) {
        validateCsvFile(file);
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<OrganisationCsvDto> csvToBean = new CsvToBeanBuilder<OrganisationCsvDto>(reader)
                    .withType(OrganisationCsvDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<OrganisationCsvDto> csvDtos = csvToBean.parse();
            log.info("CSV file parsed successfully. Total records: {}", csvDtos.size());

            return processAndSaveOrganisations(csvDtos);

        } catch (IOException e) {
            throw new CsvProcessingException("Error reading CSV file", e);
        } catch (Exception e) {
            throw new CsvProcessingException("Error processing CSV file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToCsv() {
        List<Organisation> organisations = organisationRepository.findAll();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            StatefulBeanToCsv<OrganisationCsvDto> beanToCsv = new StatefulBeanToCsvBuilder<OrganisationCsvDto>(writer)
                    .build();
            List<OrganisationCsvDto> csvDtos = convertToEnrichedCsvDtos(organisations);
            beanToCsv.write(csvDtos);
            writer.flush();

            log.info("CSV file exported successfully. Total records: {}", csvDtos.size());
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (Exception e) {
            throw new CsvProcessingException("Error exporting CSV file: " + e.getMessage(), e);
        }
    }

    private List<Organisation> processAndSaveOrganisations(List<OrganisationCsvDto> csvDtos) {
        Map<String, Organisation> codeToOrganisationMap = new HashMap<>();
        List<Organisation> savedOrganisations = new ArrayList<>();

        // First pass: Create all organisations without parent relationships
        for (OrganisationCsvDto csvDto : csvDtos) {
            Organisation organisation = organisationMapper.csvToEntity(csvDto);
            Organisation existingOrganisation = codeToOrganisationMap.get(organisation.getCode());
            if (existingOrganisation != null) {
                organisation.setId(existingOrganisation.getId());
            }
            organisationRepository.save(organisation);
            savedOrganisations.add(organisation);
            codeToOrganisationMap.put(organisation.getCode(), organisation);
        }

        // Second pass: Establish parent relationships
        for (int i = 0; i < csvDtos.size(); i++) {
            OrganisationCsvDto csvDto = csvDtos.get(i);
            Organisation organisation = savedOrganisations.get(i);
            if (csvDto.getParentCode() != null) {
                Organisation parent = codeToOrganisationMap.get(csvDto.getParentCode());
                if (parent != null) {
                    organisation.setParentId(parent.getId());
                } else {
                    log.warn("Parent organisation not found for code: {}", csvDto.getParentCode());
                    organisationRepository.findByCode(csvDto.getParentCode())
                            .ifPresent(parentOrganisation -> organisation.setParentId(parentOrganisation.getId()));
                }
            }
            organisationRepository.save(organisation);
        }

        return organisationRepository.saveAll(savedOrganisations);
    }

    private List<OrganisationCsvDto> convertToEnrichedCsvDtos(List<Organisation> organisations) {
        Map<UUID, String> idToCodeMap = new HashMap<>();

        // Build ID to code mapping
        for (Organisation org : organisations) {
            if (org.getCode() != null) {
                idToCodeMap.put(org.getId(), org.getCode());
            }
        }

        return organisations.stream()
                .map(org -> {
                    OrganisationCsvDto dto = organisationMapper.entityToCsv(org);

                    // Set parent code if parent exists
                    if (org.getParentId() != null) {
                        String parentCode = idToCodeMap.get(org.getParentId());
                        dto.setParentCode(parentCode);
                    }
                    dto.setOrganisationType(org.getOrganisationType().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void validateCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CsvProcessingException("CSV file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename != null && !filename.toLowerCase().endsWith(".csv")) {
            throw new CsvProcessingException("CSV file must have .csv extension");
        }
    }


}
