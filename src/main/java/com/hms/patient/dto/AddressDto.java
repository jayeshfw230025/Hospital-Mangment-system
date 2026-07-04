package com.hms.patient.dto;

public record AddressDto(
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String district,
        String pinCode,
        String country
) {
}
