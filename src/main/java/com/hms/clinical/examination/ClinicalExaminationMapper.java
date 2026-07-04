package com.hms.clinical.examination;

import com.hms.clinical.examination.dto.AbdominalExaminationDto;
import com.hms.clinical.examination.dto.AscitesAssessmentDto;
import com.hms.clinical.examination.dto.ClinicalExaminationRequest;
import com.hms.clinical.examination.dto.ClinicalExaminationResponse;
import com.hms.clinical.examination.dto.DigitalRectalExaminationDto;
import com.hms.clinical.examination.dto.GiMassExaminationDto;
import com.hms.clinical.examination.dto.HerniaExaminationDto;
import com.hms.clinical.examination.dto.JaundiceAssessmentDto;
import com.hms.clinical.examination.dto.LymphNodeExaminationDto;
import com.hms.clinical.examination.dto.SystemicExaminationDto;
import org.springframework.stereotype.Component;

@Component
public class ClinicalExaminationMapper {

    public void applyRequest(ClinicalExamination exam, ClinicalExaminationRequest request) {
        exam.setPatientUpid(request.patientId());

        exam.setAbdominalExamination(request.abdominalExamination() == null ? null : toEntity(request.abdominalExamination()));
        exam.setDigitalRectalExamination(request.digitalRectalExamination() == null ? null : toEntity(request.digitalRectalExamination()));
        exam.setJaundiceAssessment(request.jaundiceAssessment() == null ? null : toEntity(request.jaundiceAssessment()));
        exam.setHerniaExamination(request.herniaExamination() == null ? null : toEntity(request.herniaExamination()));
        exam.setLymphNodeExamination(request.lymphNodeExamination() == null ? null : toEntity(request.lymphNodeExamination()));
        exam.setGiMassExamination(request.giMassExamination() == null ? null : toEntity(request.giMassExamination()));
        exam.setAscitesAssessment(request.ascitesAssessment() == null ? null : toEntity(request.ascitesAssessment()));
        exam.setSystemicExamination(request.systemicExamination() == null ? null : toEntity(request.systemicExamination()));
        exam.setAbdominalGirthCm(request.abdominalGirthCm());
    }

    public ClinicalExaminationResponse toResponse(ClinicalExamination exam) {
        return new ClinicalExaminationResponse(
                exam.getId(),
                exam.getExaminationContext(),
                exam.getVisitId(),
                exam.getAdmissionId(),
                exam.getPatientUpid(),
                exam.getAbdominalExamination() == null ? null : toDto(exam.getAbdominalExamination()),
                exam.getDigitalRectalExamination() == null ? null : toDto(exam.getDigitalRectalExamination()),
                exam.getJaundiceAssessment() == null ? null : toDto(exam.getJaundiceAssessment()),
                exam.getHerniaExamination() == null ? null : toDto(exam.getHerniaExamination()),
                exam.getLymphNodeExamination() == null ? null : toDto(exam.getLymphNodeExamination()),
                exam.getGiMassExamination() == null ? null : toDto(exam.getGiMassExamination()),
                exam.getAscitesAssessment() == null ? null : toDto(exam.getAscitesAssessment()),
                exam.getSystemicExamination() == null ? null : toDto(exam.getSystemicExamination()),
                exam.getAbdominalGirthCm(),
                exam.getCreatedAt()
        );
    }

    private AbdominalExamination toEntity(AbdominalExaminationDto d) {
        return new AbdominalExamination(d.scarsPresent(), d.distensionPresent(), d.visiblePeristalsis(),
                d.tenderness(), d.tendernessSite(), d.guarding(), d.rigidity(), d.organomegaly(),
                d.percussionDullness(), d.tympanic(), d.bowelSounds(), d.notes());
    }

    private AbdominalExaminationDto toDto(AbdominalExamination e) {
        return new AbdominalExaminationDto(e.getScarsPresent(), e.getDistensionPresent(), e.getVisiblePeristalsis(),
                e.getTenderness(), e.getTendernessSite(), e.getGuarding(), e.getRigidity(), e.getOrganomegaly(),
                e.getPercussionDullness(), e.getTympanic(), e.getBowelSounds(), e.getNotes());
    }

    private DigitalRectalExamination toEntity(DigitalRectalExaminationDto d) {
        return new DigitalRectalExamination(d.fissures(), d.fistula(), d.externalPiles(), d.sphincterTone(),
                d.massPresent(), d.massDescription(), d.bloodOnFinger(), d.proctoscopyPerformed(), d.proctoscopyFindings());
    }

    private DigitalRectalExaminationDto toDto(DigitalRectalExamination e) {
        return new DigitalRectalExaminationDto(e.getFissures(), e.getFistula(), e.getExternalPiles(), e.getSphincterTone(),
                e.getMassPresent(), e.getMassDescription(), e.getBloodOnFinger(), e.getProctoscopyPerformed(), e.getProctoscopyFindings());
    }

    private JaundiceAssessment toEntity(JaundiceAssessmentDto d) {
        return new JaundiceAssessment(d.icterusSclera(), d.icterusSkin(), d.icterusPalmar(), d.scratchMarksPresent());
    }

    private JaundiceAssessmentDto toDto(JaundiceAssessment e) {
        return new JaundiceAssessmentDto(e.getIcterusSclera(), e.getIcterusSkin(), e.getIcterusPalmar(), e.getScratchMarksPresent());
    }

    private HerniaExamination toEntity(HerniaExaminationDto d) {
        return new HerniaExamination(d.herniaPresent(), d.site(), d.reducible(), d.coughImpulse());
    }

    private HerniaExaminationDto toDto(HerniaExamination e) {
        return new HerniaExaminationDto(e.getHerniaPresent(), e.getSite(), e.getReducible(), e.getCoughImpulse());
    }

    private LymphNodeExamination toEntity(LymphNodeExaminationDto d) {
        return new LymphNodeExamination(d.cervicalNodesPalpable(), d.supraclavicularNodesPalpable(),
                d.inguinalNodesPalpable(), d.notes());
    }

    private LymphNodeExaminationDto toDto(LymphNodeExamination e) {
        return new LymphNodeExaminationDto(e.getCervicalNodesPalpable(), e.getSupraclavicularNodesPalpable(),
                e.getInguinalNodesPalpable(), e.getNotes());
    }

    private GiMassExamination toEntity(GiMassExaminationDto d) {
        return new GiMassExamination(d.massPresent(), d.location(), d.sizeCm(), d.mobility(), d.consistency());
    }

    private GiMassExaminationDto toDto(GiMassExamination e) {
        return new GiMassExaminationDto(e.getMassPresent(), e.getLocation(), e.getSizeCm(), e.getMobility(), e.getConsistency());
    }

    private AscitesAssessment toEntity(AscitesAssessmentDto d) {
        return new AscitesAssessment(d.shiftingDullnessPresent(), d.fluidThrillPresent(), d.notes());
    }

    private AscitesAssessmentDto toDto(AscitesAssessment e) {
        return new AscitesAssessmentDto(e.getShiftingDullnessPresent(), e.getFluidThrillPresent(), e.getNotes());
    }

    private SystemicExamination toEntity(SystemicExaminationDto d) {
        return new SystemicExamination(d.chestExpansion(), d.breathSounds(), d.heartSounds(), d.murmursPresent(),
                d.murmurDescription(), d.jvp(), d.gcsScore(), d.pupillaryReflex(), d.motorFindings(), d.sensoryFindings());
    }

    private SystemicExaminationDto toDto(SystemicExamination e) {
        return new SystemicExaminationDto(e.getChestExpansion(), e.getBreathSounds(), e.getHeartSounds(), e.getMurmursPresent(),
                e.getMurmurDescription(), e.getJvp(), e.getGcsScore(), e.getPupillaryReflex(), e.getMotorFindings(), e.getSensoryFindings());
    }
}
