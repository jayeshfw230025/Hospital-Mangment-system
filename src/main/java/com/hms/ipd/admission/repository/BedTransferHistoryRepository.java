package com.hms.ipd.admission.repository;

import com.hms.ipd.admission.domain.BedTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedTransferHistoryRepository extends JpaRepository<BedTransferHistory, Long> {
}
