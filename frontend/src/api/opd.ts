import { apiClient, type ApiResponse } from './client'

export type ComplaintType =
  | 'ABDOMINAL_PAIN'
  | 'NAUSEA_VOMITING'
  | 'HEARTBURN_REFLUX'
  | 'DYSPHAGIA'
  | 'EARLY_SATIETY'
  | 'BLOATING'
  | 'DIARRHEA'
  | 'CONSTIPATION'
  | 'BLOOD_IN_STOOL'
  | 'MELENA'
  | 'HEMATEMESIS'
  | 'JAUNDICE'
  | 'WEIGHT_LOSS'
  | 'APPETITE_CHANGES'
  | 'ABDOMINAL_DISTENSION'
  | 'RECTAL_BLEEDING'
  | 'ANOREXIA'
  | 'FEVER'
  | 'FATIGUE'
  | 'OTHERS'

export const COMPLAINT_TYPES: { value: ComplaintType; label: string; requiredKeys: string[] }[] = [
  { value: 'ABDOMINAL_PAIN', label: 'Abdominal Pain', requiredKeys: ['location', 'aggravatingFactors', 'relievingFactors'] },
  { value: 'NAUSEA_VOMITING', label: 'Nausea & Vomiting', requiredKeys: ['character', 'bloodPresent'] },
  { value: 'HEARTBURN_REFLUX', label: 'Heartburn/Acid Reflux', requiredKeys: [] },
  { value: 'DYSPHAGIA', label: 'Dysphagia', requiredKeys: ['solidOrLiquid', 'progressivePattern'] },
  { value: 'EARLY_SATIETY', label: 'Early Satiety', requiredKeys: ['associatedSymptoms'] },
  { value: 'BLOATING', label: 'Bloating', requiredKeys: [] },
  { value: 'DIARRHEA', label: 'Diarrhea', requiredKeys: ['character', 'bloodOrMucus'] },
  { value: 'CONSTIPATION', label: 'Constipation', requiredKeys: ['hardStools'] },
  { value: 'BLOOD_IN_STOOL', label: 'Blood in Stool', requiredKeys: ['color', 'amount', 'associatedSymptoms'] },
  { value: 'MELENA', label: 'Melena', requiredKeys: ['amount'] },
  { value: 'HEMATEMESIS', label: 'Hematemesis', requiredKeys: ['amount'] },
  { value: 'JAUNDICE', label: 'Jaundice', requiredKeys: ['pruritus'] },
  { value: 'WEIGHT_LOSS', label: 'Weight Loss', requiredKeys: ['amount'] },
  { value: 'APPETITE_CHANGES', label: 'Appetite Changes', requiredKeys: ['direction'] },
  { value: 'ABDOMINAL_DISTENSION', label: 'Abdominal Distension', requiredKeys: ['onset', 'progression'] },
  { value: 'RECTAL_BLEEDING', label: 'Rectal Bleeding', requiredKeys: ['amount'] },
  { value: 'ANOREXIA', label: 'Anorexia', requiredKeys: [] },
  { value: 'FEVER', label: 'Fever', requiredKeys: ['temperature', 'pattern'] },
  { value: 'FATIGUE', label: 'Fatigue', requiredKeys: ['dailyActivityImpact'] },
  { value: 'OTHERS', label: 'Others', requiredKeys: ['freeText'] },
]

export type SeverityLevel = 'MILD' | 'MODERATE' | 'SEVERE'
export type DurationUnit = 'HOURS' | 'DAYS' | 'WEEKS' | 'MONTHS' | 'YEARS'
export type FrequencyLevel = 'RARE' | 'OCCASIONAL' | 'FREQUENT' | 'CONSTANT'

export interface OpdComplaintRequest {
  visitId: number
  complaintType: ComplaintType
  severity?: SeverityLevel | null
  durationValue?: number | null
  durationUnit?: DurationUnit | null
  frequency?: FrequencyLevel | null
  onsetDate?: string | null
  notes?: string | null
  details: Record<string, string>
}

export interface OpdComplaintResponse {
  id: number
  visitId: number
  complaintType: ComplaintType
  complaintLabel: string
  severity: SeverityLevel | null
  durationValue: number | null
  durationUnit: DurationUnit | null
  frequency: FrequencyLevel | null
  onsetDate: string | null
  notes: string | null
  details: Record<string, unknown>
  createdAt: string
}

export async function createOpdComplaint(request: OpdComplaintRequest): Promise<OpdComplaintResponse> {
  const response = await apiClient.post<ApiResponse<OpdComplaintResponse>>('/opd/complaints', request)
  return response.data.data
}

export async function getOpdComplaintsByVisit(visitId: number): Promise<OpdComplaintResponse[]> {
  const response = await apiClient.get<ApiResponse<OpdComplaintResponse[]>>(`/opd/complaints/${visitId}`)
  return response.data.data
}

export interface OpdVitalsRequest {
  visitId: number
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
}

export interface VitalAlertResponse {
  id: number
  patientId: string
  parameter: string
  measuredValue: string
  message: string
  acknowledged: boolean
}

export interface OpdVitalsResponse {
  id: number
  visitId: number
  patientId: string
  systolicBp: number | null
  diastolicBp: number | null
  heartRate: number | null
  respiratoryRate: number | null
  temperatureCelsius: number | null
  temperatureFahrenheit: number | null
  heightCm: number | null
  weightKg: number | null
  bmi: number | null
  spo2: number | null
  painScore: number | null
  randomBloodSugar: number | null
  triggeredAlerts: VitalAlertResponse[]
  recordedAt: string
}

export async function recordOpdVitals(request: OpdVitalsRequest): Promise<OpdVitalsResponse> {
  const response = await apiClient.post<ApiResponse<OpdVitalsResponse>>('/vitals/opd', request)
  return response.data.data
}

export async function getOpdVitalsByVisit(visitId: number): Promise<OpdVitalsResponse[]> {
  const response = await apiClient.get<ApiResponse<OpdVitalsResponse[]>>(`/vitals/visit/${visitId}`)
  return response.data.data
}

export type DiagnosisType = 'PRIMARY' | 'SECONDARY'
export type DiagnosisStatus = 'ACTIVE' | 'INACTIVE'

export interface Icd10CodeResponse {
  code: string
  description: string
  category: string
  active: boolean
}

export interface DiagnosisRequest {
  patientId: string
  icd10Code: string
  diagnosisType: DiagnosisType
  status?: DiagnosisStatus | null
  diagnosedDate?: string | null
  notes?: string | null
}

export interface DiagnosisResponse {
  id: number
  patientId: string
  icd10Code: string
  icd10Description: string
  icd10Category: string
  diagnosisType: DiagnosisType
  status: DiagnosisStatus
  diagnosedDate: string | null
  notes: string | null
  createdAt: string
}

export async function searchIcd10(query: string): Promise<Icd10CodeResponse[]> {
  const response = await apiClient.get<ApiResponse<Icd10CodeResponse[]>>('/icd10/search', { params: { q: query } })
  return response.data.data
}

export async function createDiagnosis(request: DiagnosisRequest): Promise<DiagnosisResponse> {
  const response = await apiClient.post<ApiResponse<DiagnosisResponse>>('/diagnosis', request)
  return response.data.data
}

export async function getDiagnosesByPatient(patientId: string): Promise<DiagnosisResponse[]> {
  const response = await apiClient.get<ApiResponse<DiagnosisResponse[]>>(`/diagnosis/${patientId}`)
  return response.data.data
}
