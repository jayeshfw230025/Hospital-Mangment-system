package com.hms.clinical.examination.dto;

public record LymphNodeExaminationDto(
        Boolean cervicalNodesPalpable,
        Boolean supraclavicularNodesPalpable,
        Boolean inguinalNodesPalpable,
        String notes
) {
}
