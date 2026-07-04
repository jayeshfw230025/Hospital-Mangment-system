package com.hms.prescription.domain;

import java.util.Set;

/**
 * The 11 gastro prescription templates. Each carries the DrugCategory set a
 * doctor typically prescribes from for that condition - used to suggest matching
 * drugs from the master list rather than to prescribe exact drugs automatically.
 */
public enum GastroTemplateType {

    PUD("Peptic Ulcer Disease",
            Set.of(DrugCategory.PPI, DrugCategory.H2_BLOCKER, DrugCategory.ANTACID, DrugCategory.H_PYLORI_ERADICATION)),

    GERD("GERD",
            Set.of(DrugCategory.PPI, DrugCategory.PROKINETIC, DrugCategory.ANTACID)),

    IBD("Inflammatory Bowel Disease",
            Set.of(DrugCategory.FIVE_ASA, DrugCategory.IMMUNOMODULATOR, DrugCategory.BIOLOGIC, DrugCategory.CORTICOSTEROID)),

    ACUTE_PANCREATITIS("Acute Pancreatitis",
            Set.of(DrugCategory.IV_FLUID, DrugCategory.ANALGESIC, DrugCategory.ANTIBIOTIC, DrugCategory.ANTIEMETIC)),

    CHRONIC_HEPATITIS("Chronic Hepatitis",
            Set.of(DrugCategory.ANTIVIRAL, DrugCategory.HEPATOPROTECTIVE, DrugCategory.VITAMIN_SUPPLEMENT)),

    LIVER_CIRRHOSIS("Liver Cirrhosis",
            Set.of(DrugCategory.DIURETIC, DrugCategory.ALBUMIN, DrugCategory.VITAMIN_SUPPLEMENT, DrugCategory.LAXATIVE_OSMOTIC)),

    GI_BLEEDING("GI Bleeding",
            Set.of(DrugCategory.ANTACID, DrugCategory.PPI, DrugCategory.BLOOD_PRODUCT, DrugCategory.VITAMIN_SUPPLEMENT)),

    DIARRHEA("Diarrhea",
            Set.of(DrugCategory.ORS, DrugCategory.PROBIOTIC, DrugCategory.ANTIBIOTIC, DrugCategory.ANTIDIARRHEAL, DrugCategory.ZINC_SUPPLEMENT)),

    CONSTIPATION("Constipation",
            Set.of(DrugCategory.LAXATIVE_BULK_FORMING, DrugCategory.LAXATIVE_STIMULANT, DrugCategory.LAXATIVE_OSMOTIC)),

    IBS("Irritable Bowel Syndrome",
            Set.of(DrugCategory.ANTISPASMODIC, DrugCategory.PROBIOTIC, DrugCategory.ANTIDEPRESSANT, DrugCategory.LAXATIVE_BULK_FORMING)),

    GI_MALIGNANCY("Malignancy/GI Cancer",
            Set.of(DrugCategory.PALLIATIVE_CARE, DrugCategory.ANALGESIC, DrugCategory.ANTIEMETIC,
                    DrugCategory.NUTRITIONAL_SUPPORT, DrugCategory.CHEMOTHERAPY));

    private final String label;
    private final Set<DrugCategory> categories;

    GastroTemplateType(String label, Set<DrugCategory> categories) {
        this.label = label;
        this.categories = categories;
    }

    public String getLabel() {
        return label;
    }

    public Set<DrugCategory> getCategories() {
        return categories;
    }
}
