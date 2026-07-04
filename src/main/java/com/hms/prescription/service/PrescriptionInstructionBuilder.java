package com.hms.prescription.service;

import com.hms.prescription.domain.FoodInstruction;
import com.hms.prescription.dto.PrescriptionItemRequest;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionInstructionBuilder {

    public String build(PrescriptionItemRequest item, String genericName) {
        StringBuilder sb = new StringBuilder("Take ")
                .append(item.dosage())
                .append(" of ")
                .append(genericName);

        if (item.route() != null && !item.route().isBlank()) {
            sb.append(" via ").append(item.route()).append(" route");
        }

        sb.append(" ").append(item.frequency());

        if (item.foodInstruction() != null) {
            sb.append(" ").append(foodInstructionText(item.foodInstruction()));
        }

        if (item.durationDays() != null) {
            sb.append(" for ").append(item.durationDays()).append(" day")
                    .append(item.durationDays() == 1 ? "" : "s");
        }

        return sb.append(".").toString();
    }

    private String foodInstructionText(FoodInstruction foodInstruction) {
        return switch (foodInstruction) {
            case BEFORE_FOOD -> "before food";
            case AFTER_FOOD -> "after food";
            case WITH_FOOD -> "with food";
            case EMPTY_STOMACH -> "on an empty stomach";
            case ANYTIME -> "at any time";
        };
    }
}
