import { apiClient, type ApiResponse } from './client'

export type DischargeType = 'RECOVERED' | 'IMPROVED' | 'STABLE' | 'TRANSFERRED' | 'LAMA' | 'ABSCONDED' | 'EXPIRED'
export type DischargeCondition = 'STABLE' | 'IMPROVING' | 'PALLIATIVE'

export interface DischargeSummaryRequest {
  admissionId: number
  dischargeType: DischargeType
  primaryDiagnosisIcd10: string
  secondaryDiagnosisIcd10?: string | null
  dischargeDiagnosisText?: string | null
  summaryOfHospitalStay?: string | null
  followUpDateTime?: string | null
  followUpInstructions?: string | null
  dischargeCondition?: DischargeCondition | null
  dischargedByDoctorName: string
  dischargedByDoctorSignature: string
  medicalRecordsChecked?: boolean | null
  dischargeInstructions?: string | null
  dischargeDietPlanOverride?: string | null
}

export interface DischargeMedicationItemDto {
  drugName: string
  dosage: string | null
  frequency: string | null
  durationDays: number | null
}

export interface DischargeSummaryResponse {
  id: number
  admissionId: number
  patientId: string
  patientName: string
  admissionDateTime: string
  dischargeDateTime: string
  lengthOfStayDays: number | null
  dischargeType: DischargeType
  primaryDiagnosisIcd10: string
  primaryDiagnosisDescription: string | null
  dischargeDiagnosisText: string | null
  summaryOfHospitalStay: string | null
  significantProcedures: string[]
  complicationsDuringStay: string[]
  dischargeMedications: DischargeMedicationItemDto[]
  dischargeDietPlan: string | null
  followUpDateTime: string | null
  followUpInstructions: string | null
  dischargeCondition: DischargeCondition | null
  dischargedByDoctorName: string
  medicalRecordsChecked: boolean | null
  dischargeInstructions: string | null
  createdAt: string
}

export async function createDischargeSummary(request: DischargeSummaryRequest): Promise<DischargeSummaryResponse> {
  const response = await apiClient.post<ApiResponse<DischargeSummaryResponse>>('/discharge', request)
  return response.data.data
}

export async function getDischargeSummaryByAdmission(admissionId: number): Promise<DischargeSummaryResponse | null> {
  try {
    const response = await apiClient.get<ApiResponse<DischargeSummaryResponse>>(`/discharge/${admissionId}`)
    return response.data.data
  } catch {
    return null
  }
}
