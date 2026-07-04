package com.hms.patient.domain;

import com.hms.common.audit.Auditable;
import com.hms.common.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upid", unique = true, nullable = false, length = 20)
    private String upid;

    @Column(name = "abha_number", length = 20)
    private String abhaNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 15)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 15)
    private BloodGroup bloodGroup;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "religion")
    private String religion;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "education")
    private String education;

    @Column(name = "primary_contact_number", unique = true, nullable = false, length = 15)
    private String primaryContactNumber;

    @Column(name = "secondary_contact_number", length = 15)
    private String secondaryContactNumber;

    @Column(name = "email")
    private String email;

    @Embedded
    private Address address;

    @Embedded
    private GeoLocation geoLocation;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "govt_id_type", length = 20)
    private GovtIdType govtIdType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "govt_id_number")
    private String govtIdNumber;

    @Embedded
    private EmergencyContact emergencyContact;

    @Embedded
    private ReferralDetails referralDetails;
}
