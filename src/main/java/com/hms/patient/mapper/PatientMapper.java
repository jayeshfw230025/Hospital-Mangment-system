package com.hms.patient.mapper;

import com.hms.patient.domain.Address;
import com.hms.patient.domain.EmergencyContact;
import com.hms.patient.domain.GeoLocation;
import com.hms.patient.domain.Patient;
import com.hms.patient.domain.ReferralDetails;
import com.hms.patient.dto.AddressDto;
import com.hms.patient.dto.EmergencyContactDto;
import com.hms.patient.dto.GeoLocationDto;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.dto.PatientUpdateRequest;
import com.hms.patient.dto.ReferralDetailsDto;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public Patient toEntity(PatientRegistrationRequest request) {
        Patient patient = new Patient();
        patient.setFullName(request.fullName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setMaritalStatus(request.maritalStatus());
        patient.setBloodGroup(request.bloodGroup());
        patient.setNationality(request.nationality());
        patient.setReligion(request.religion());
        patient.setOccupation(request.occupation());
        patient.setEducation(request.education());
        patient.setPrimaryContactNumber(request.primaryContactNumber());
        patient.setSecondaryContactNumber(request.secondaryContactNumber());
        patient.setEmail(request.email());
        patient.setAadhaarNumber(request.aadhaarNumber());
        patient.setGovtIdType(request.govtIdType());
        patient.setGovtIdNumber(request.govtIdNumber());

        if (request.address() != null) {
            AddressDto a = request.address();
            patient.setAddress(new Address(a.addressLine1(), a.addressLine2(), a.city(),
                    a.state(), a.district(), a.pinCode(),
                    a.country() != null ? a.country() : "India"));
        }

        if (request.geoLocation() != null) {
            GeoLocationDto g = request.geoLocation();
            patient.setGeoLocation(new GeoLocation(g.latitude(), g.longitude()));
        }

        if (request.emergencyContact() != null) {
            EmergencyContactDto e = request.emergencyContact();
            patient.setEmergencyContact(new EmergencyContact(e.name(), e.contactNumber(), e.relation()));
        }

        if (request.referralDetails() != null) {
            ReferralDetailsDto r = request.referralDetails();
            patient.setReferralDetails(new ReferralDetails(r.referringDoctorName(), r.referringHospitalName(),
                    r.referralDate(), r.referralReason(), r.referralContactNumber()));
        }

        return patient;
    }

    public void updateEntity(Patient patient, PatientUpdateRequest request) {
        patient.setFullName(request.fullName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setMaritalStatus(request.maritalStatus());
        patient.setBloodGroup(request.bloodGroup());
        patient.setNationality(request.nationality());
        patient.setReligion(request.religion());
        patient.setOccupation(request.occupation());
        patient.setEducation(request.education());
        patient.setPrimaryContactNumber(request.primaryContactNumber());
        patient.setSecondaryContactNumber(request.secondaryContactNumber());
        patient.setEmail(request.email());
        patient.setAadhaarNumber(request.aadhaarNumber());
        patient.setGovtIdType(request.govtIdType());
        patient.setGovtIdNumber(request.govtIdNumber());

        patient.setAddress(request.address() == null ? null : new Address(
                request.address().addressLine1(), request.address().addressLine2(), request.address().city(),
                request.address().state(), request.address().district(), request.address().pinCode(),
                request.address().country() != null ? request.address().country() : "India"));

        patient.setGeoLocation(request.geoLocation() == null ? null
                : new GeoLocation(request.geoLocation().latitude(), request.geoLocation().longitude()));

        patient.setEmergencyContact(request.emergencyContact() == null ? null : new EmergencyContact(
                request.emergencyContact().name(), request.emergencyContact().contactNumber(),
                request.emergencyContact().relation()));

        patient.setReferralDetails(request.referralDetails() == null ? null : new ReferralDetails(
                request.referralDetails().referringDoctorName(), request.referralDetails().referringHospitalName(),
                request.referralDetails().referralDate(), request.referralDetails().referralReason(),
                request.referralDetails().referralContactNumber()));
    }

    public PatientResponse toResponse(Patient patient) {
        Address a = patient.getAddress();
        GeoLocation g = patient.getGeoLocation();
        EmergencyContact e = patient.getEmergencyContact();
        ReferralDetails r = patient.getReferralDetails();

        return new PatientResponse(
                patient.getId(),
                patient.getUpid(),
                patient.getAbhaNumber(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getMaritalStatus(),
                patient.getBloodGroup(),
                patient.getNationality(),
                patient.getReligion(),
                patient.getOccupation(),
                patient.getEducation(),
                patient.getPrimaryContactNumber(),
                patient.getSecondaryContactNumber(),
                patient.getEmail(),
                a == null ? null : new AddressDto(a.getAddressLine1(), a.getAddressLine2(), a.getCity(),
                        a.getState(), a.getDistrict(), a.getPinCode(), a.getCountry()),
                g == null ? null : new GeoLocationDto(g.getLatitude(), g.getLongitude()),
                patient.getGovtIdType(),
                e == null ? null : new EmergencyContactDto(e.getName(), e.getContactNumber(), e.getRelation()),
                r == null ? null : new ReferralDetailsDto(r.getReferringDoctorName(), r.getReferringHospitalName(),
                        r.getReferralDate(), r.getReferralReason(), r.getReferralContactNumber()),
                patient.getCreatedAt()
        );
    }
}
