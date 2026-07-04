package com.hms.patient.dto;

import java.time.LocalDate;

public record ReferralDetailsDto(
        String referringDoctorName,
        String referringHospitalName,
        LocalDate referralDate,
        String referralReason,
        String referralContactNumber
) {
}
