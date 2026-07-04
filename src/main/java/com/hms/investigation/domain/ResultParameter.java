package com.hms.investigation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ResultParameter {

    @Column(name = "parameter_name")
    private String parameterName;

    @Column(name = "result_value")
    private String value;

    @Column(name = "unit")
    private String unit;

    @Column(name = "reference_range_low")
    private Double referenceRangeLow;

    @Column(name = "reference_range_high")
    private Double referenceRangeHigh;

    @Column(name = "abnormal")
    private boolean abnormal;
}
