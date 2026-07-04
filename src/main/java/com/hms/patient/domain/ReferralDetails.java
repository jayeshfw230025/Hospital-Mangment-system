package com.hms.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReferralDetails {

    @Column(name = "referring_doctor_name")
    private String referringDoctorName;

    @Column(name = "referring_hospital_name")
    private String referringHospitalName;

    @Column(name = "referral_date")
    private LocalDate referralDate;

    @Column(name = "referral_reason")
    private String referralReason;

    @Column(name = "referral_contact_number")
    private String referralContactNumber;
}
