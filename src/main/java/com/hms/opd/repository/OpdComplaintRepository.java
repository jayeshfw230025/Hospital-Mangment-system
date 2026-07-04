package com.hms.opd.repository;

import com.hms.opd.domain.OpdComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpdComplaintRepository extends JpaRepository<OpdComplaint, Long> {

    List<OpdComplaint> findByVisitIdOrderByCreatedAtAsc(Long visitId);
}
