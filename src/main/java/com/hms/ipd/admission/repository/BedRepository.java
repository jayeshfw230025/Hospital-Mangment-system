package com.hms.ipd.admission.repository;

import com.hms.ipd.admission.domain.Bed;
import com.hms.ipd.admission.domain.BedStatus;
import com.hms.ipd.admission.domain.WardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByStatus(BedStatus status);

    List<Bed> findByStatusAndWardType(BedStatus status, WardType wardType);
}
