package com.hms.prescription.repository;

import com.hms.prescription.domain.Drug;
import com.hms.prescription.domain.DrugCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface DrugRepository extends JpaRepository<Drug, Long> {

    List<Drug> findByGenericNameContainingIgnoreCaseOrBrandNameContainingIgnoreCaseOrderByGenericNameAsc(
            String genericQuery, String brandQuery);

    List<Drug> findByCategoryInAndActiveTrue(Set<DrugCategory> categories);
}
