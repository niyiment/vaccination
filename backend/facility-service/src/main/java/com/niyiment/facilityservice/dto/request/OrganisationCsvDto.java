package com.niyiment.facilityservice.dto.request;


import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationCsvDto {

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvBindByName(column = "code")
    private String code;

    @CsvBindByName(column = "parent_code")
    private String parentCode;

    @CsvBindByName(column = "organisation_type", required = true)
    private String organisationType;

    @CsvBindByName(column = "address")
    private String address;
}
