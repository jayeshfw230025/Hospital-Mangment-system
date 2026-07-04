import { apiClient, type ApiResponse } from './client'

export type AppetiteLevel = 'POOR' | 'MODERATE' | 'GOOD'
export type ActivityLevel = 'BED_REST' | 'ASSISTED_AMBULATION' | 'INDEPENDENT_AMBULATION' | 'NORMAL_ACTIVITY'
export type SeverityLevel = 'MILD' | 'MODERATE' | 'SEVERE'

export interface ProgressNoteRequest {
  admissionId: number
  chiefComplaintToday?: string | null
  painScore?: number | null
  nauseaVomiting?: boolean | null
  appetite?: AppetiteLevel | null
  bowelMovementFrequency?: string | null
  bowelMovementCharacter?: string | null
  sleepPattern?: string | null
  generalWellBeing?: string | null
  generalAppearance?: string | null
  abdominalExaminationFindings?: string | null
  newFindings?: string | null
  clinicalImpression?: string | null
  currentDiagnosis?: string | null
  icd10Code?: string | null
  severityAssessment?: SeverityLevel | null
  complicationFlags?: string[]
  medicationPlanItems?: never[]
  investigationsOrdered?: string[]
  consultationsRequired?: string[]
  dietPlan?: string | null
  activityLevel?: ActivityLevel | null
  dischargePlanningNotes?: string | null
}

export interface ProgressNoteResponse {
  id: number
  admissionId: number
  noteDate: string
  chiefComplaintToday: string | null
  painScore: number | null
  generalWellBeing: string | null
  clinicalImpression: string | null
  currentDiagnosis: string | null
  severityAssessment: SeverityLevel | null
  dietPlan: string | null
  activityLevel: ActivityLevel | null
  dischargePlanningNotes: string | null
  createdAt: string
}

export async function createProgressNote(request: ProgressNoteRequest): Promise<ProgressNoteResponse> {
  const response = await apiClient.post<ApiResponse<ProgressNoteResponse>>('/ipd/progress-note', request)
  return response.data.data
}

export async function getProgressNotesByAdmission(admissionId: number): Promise<ProgressNoteResponse[]> {
  const response = await apiClient.get<ApiResponse<ProgressNoteResponse[]>>(`/ipd/progress-note/${admissionId}`)
  return response.data.data
}

export type AdministrationStatus = 'ADMINISTERED' | 'MISSED' | 'REFUSED' | 'PENDING'

export interface MarRequest {
  admissionId: number
  drugId?: number | null
  drugName: string
  dosage?: string | null
  route?: string | null
  scheduledTime: string
  administeredTime?: string | null
  administeredByName?: string | null
  status?: AdministrationStatus | null
  notes?: string | null
}

export interface MarResponse {
  id: number
  admissionId: number
  drugId: number | null
  drugName: string
  dosage: string | null
  route: string | null
  scheduledTime: string
  administeredTime: string | null
  administeredByName: string | null
  status: AdministrationStatus
  notes: string | null
  createdAt: string
}

export async function recordMar(request: MarRequest): Promise<MarResponse> {
  const response = await apiClient.post<ApiResponse<MarResponse>>('/ipd/mar', request)
  return response.data.data
}

export async function getMarByAdmission(admissionId: number): Promise<MarResponse[]> {
  const response = await apiClient.get<ApiResponse<MarResponse[]>>(`/ipd/mar/${admissionId}`)
  return response.data.data
}

export interface IpdVitalsRequest {
  admissionId: number
  patientId: string
  systolicBp?: number | null
  diastolicBp?: number | null
  heartRate?: number | null
  respiratoryRate?: number | null
  temperature?: number | null
  temperatureUnit?: 'CELSIUS' | 'FAHRENHEIT' | null
  heightCm?: number | null
  weightKg?: number | null
  spo2?: number | null
  painScore?: number | null
  randomBloodSugar?: number | null
  qtcMs?: number | null
  inputOutputBalanceMl?: number | null
  gcsScore?: number | null
  cvpCmH2o?: number | null
  gagReflex?: 'PRESENT' | 'ABSENT' | null
}

export interface VitalAlertResponse {
  id: number
  message: string
  acknowledged: boolean
}

export interface IpdVitalsResponse {
  id: number
  admissionId: number
  patientId: string
  systolicBp: number | null
  diastolicBp: number | null
  heartRate: number | null
  temperatureCelsius: number | null
  spo2: number | null
  gcsScore: number | null
  bmi: number | null
  mapValue: number | null
  triggeredAlerts: VitalAlertResponse[]
  recordedAt: string
}

export async function recordIpdVitals(request: IpdVitalsRequest): Promise<IpdVitalsResponse> {
  const response = await apiClient.post<ApiResponse<IpdVitalsResponse>>('/vitals/ipd', request)
  return response.data.data
}

interface PatientVitalsHistoryResponse {
  patientId: string
  opdVitals: unknown[]
  ipdVitals: IpdVitalsResponse[]
}

// There is no GET .../vitals/admission/{admissionId} endpoint - only a
// per-patient combined history - so this filters client-side.
export async function getIpdVitalsByAdmission(patientId: string, admissionId: number): Promise<IpdVitalsResponse[]> {
  const response = await apiClient.get<ApiResponse<PatientVitalsHistoryResponse>>(`/vitals/patient/${patientId}`)
  return response.data.data.ipdVitals.filter((v) => v.admissionId === admissionId)
}
