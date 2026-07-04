import { apiClient, type ApiResponse } from './client'

export type Granularity = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY'

export interface PatientVolumeBucket {
  periodLabel: string
  newRegistrations: number
  ipdAdmissions: number
}

export interface PatientVolumeResponse {
  granularity: Granularity
  series: PatientVolumeBucket[]
}

export interface DiseaseDistributionEntry {
  icd10Code: string
  description: string
  count: number
}

export interface NamedCount {
  label: string
  count: number
}

export interface OpdIpdRatioResponse {
  opdEncounters: number
  ipdAdmissions: number
  ratio: number
}

export interface AlosEntry {
  icd10Code: string
  description: string
  averageLengthOfStayDays: number
  dischargeCount: number
}

export interface ReadmissionRateResponse {
  rate7DayPercent: number
  rate14DayPercent: number
  rate30DayPercent: number
  totalDischargesConsidered: number
}

export interface ProcedureStatEntry {
  procedureType: string
  label: string
  totalCount: number
  complicationCount: number
  complicationRatePercent: number
  successRatePercent: number
}

export interface MortalityEntry {
  icd10Code: string
  description: string
  totalDischarges: number
  expiredCount: number
  mortalityRatePercent: number
}

export interface ReferralPatternResponse {
  topReferringDoctors: NamedCount[]
  topReferringHospitals: NamedCount[]
  admissionSourceDistribution: NamedCount[]
}

export interface PatientSatisfactionResponse {
  available: boolean
  message: string
}

export interface KpisResponse {
  patientVolume: PatientVolumeResponse
  topGiDiseases: DiseaseDistributionEntry[]
  opdIpdRatio: OpdIpdRatioResponse
  alosByDiagnosis: AlosEntry[]
  readmissionRate: ReadmissionRateResponse
  procedureStats: ProcedureStatEntry[]
  mortalityByDiagnosis: MortalityEntry[]
  patientSatisfaction: PatientSatisfactionResponse
  referralPattern: ReferralPatternResponse
}

export interface DiseaseDistributionResponse {
  byAgeGroup: NamedCount[]
  byGender: NamedCount[]
  byLocation: NamedCount[]
  byTime: NamedCount[]
  byClinicalSeverity: NamedCount[]
  byIcd10Code: DiseaseDistributionEntry[]
  byTreatmentOutcome: NamedCount[]
}

export interface DashboardResponse {
  kpis: KpisResponse
  diseaseDistribution: DiseaseDistributionResponse
}

export async function getDashboard(
  startDate?: string,
  endDate?: string,
  granularity: Granularity = 'MONTHLY',
): Promise<DashboardResponse> {
  const response = await apiClient.get<ApiResponse<DashboardResponse>>('/analytics/dashboard', {
    params: { startDate: startDate || undefined, endDate: endDate || undefined, granularity },
  })
  return response.data.data
}

export function exportUrl(format: 'csv' | 'pdf', granularity: Granularity = 'MONTHLY'): string {
  return `/api/v1/analytics/export?format=${format}&granularity=${granularity}`
}
