package com.hms.ipd.repository;

import com.hms.ipd.domain.IpdComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IpdComplaintRepository extends JpaRepository<IpdComplaint, Long> {

    List<IpdComplaint> findByAdmissionIdOrderByCreatedAtAsc(Long admissionId);
}
