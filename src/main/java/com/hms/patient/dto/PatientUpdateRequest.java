package com.hms.patient.dto;

import com.hms.patient.domain.BloodGroup;
import com.hms.patient.domain.Gender;
import com.hms.patient.domain.GovtIdType;
import com.hms.patient.domain.MaritalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PatientUpdateRequest(

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotNull(message = "Gender is required")
        Gender gender,

        MaritalStatus maritalStatus,

        BloodGroup bloodGroup,

        String nationality,

        String religion,

        String occupation,

        String education,

        @NotBlank(message = "Primary contact number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Primary contact number must be a 10-digit number")
        String primaryContactNumber,

        @Pattern(regexp = "^[0-9]{10}$", message = "Secondary contact number must be a 10-digit number")
        String secondaryContactNumber,

        @Email(message = "Email must be valid")
        String email,

        @Valid
        AddressDto address,

        @Valid
        GeoLocationDto geoLocation,

        String aadhaarNumber,

        GovtIdType govtIdType,

        String govtIdNumber,

        @Valid
        EmergencyContactDto emergencyContact,

        @Valid
        ReferralDetailsDto referralDetails
) {
}
