package com.hms.patient.dto;

import com.hms.patient.domain.BloodGroup;
import com.hms.patient.domain.Gender;
import com.hms.patient.domain.GovtIdType;
import com.hms.patient.domain.MaritalStatus;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String upid,
        String abhaNumber,
        String fullName,
        LocalDate dateOfBirth,
        Gender gender,
        MaritalStatus maritalStatus,
        BloodGroup bloodGroup,
        String nationality,
        String religion,
        String occupation,
        String education,
        String primaryContactNumber,
        String secondaryContactNumber,
        String email,
        AddressDto address,
        GeoLocationDto geoLocation,
        GovtIdType govtIdType,
        EmergencyContactDto emergencyContact,
        ReferralDetailsDto referralDetails,
        Instant createdAt
) {
}
