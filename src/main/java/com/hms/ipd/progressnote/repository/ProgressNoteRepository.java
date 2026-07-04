package com.hms.ipd.progressnote.repository;

import com.hms.ipd.progressnote.domain.ProgressNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressNoteRepository extends JpaRepository<ProgressNote, Long> {

    List<ProgressNote> findByAdmissionIdOrderByCreatedAtAsc(Long admissionId);
}
