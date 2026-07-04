import { apiClient, type ApiResponse } from './client'

export type AdmissionType = 'EMERGENCY' | 'ELECTIVE' | 'TRANSFER'
export type AdmissionSource = 'OPD' | 'ER' | 'REFERRAL' | 'TRANSFER'
export type WardType = 'GENERAL' | 'ICU' | 'PRIVATE' | 'SEMI_PRIVATE'
export type BedStatus = 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'MAINTENANCE'

export interface IpdAdmissionRequest {
  patientId: string
  admissionType: AdmissionType
  admissionSource: AdmissionSource
  referralDoctorName?: string | null
  referralDoctorContact?: string | null
  referringHospitalName?: string | null
  referringHospitalContact?: string | null
  primaryDiagnosisIcd10: string
  secondaryDiagnosisIcd10?: string | null
  clinicalSummary?: string | null
  consentSignature: string
}

export interface BedResponse {
  id: number
  wardType: WardType
  roomNumber: string
  bedNumber: string
  status: BedStatus
  currentAdmissionId: number | null
}

export interface CurrentMedicationDto {
  drugName: string
  dosage: string
  frequency: string
}

export interface TpaPreAuthResponse {
  id: number
  admissionId: number
  approvalStatus: string
}

export interface IpdAdmissionResponse {
  id: number
  patientId: string
  admissionDateTime: string
  admissionType: AdmissionType
  admissionSource: AdmissionSource
  referralDoctorName: string | null
  referralDoctorContact: string | null
  referringHospitalName: string | null
  referringHospitalContact: string | null
  primaryDiagnosisIcd10: string
  primaryDiagnosisDescription: string | null
  secondaryDiagnosisIcd10: string | null
  secondaryDiagnosisDescription: string | null
  clinicalSummary: string | null
  hardStopAllergies: string[]
  currentMedications: CurrentMedicationDto[]
  hasConsentDocument: boolean
  bed: BedResponse | null
  latestTpaPreAuth: TpaPreAuthResponse | null
  createdAt: string
}

export async function createAdmission(request: IpdAdmissionRequest): Promise<IpdAdmissionResponse> {
  const formData = new FormData()
  formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }))
  const response = await apiClient.post<ApiResponse<IpdAdmissionResponse>>('/ipd/admission', formData)
  return response.data.data
}

export async function getAdmissionById(id: number): Promise<IpdAdmissionResponse> {
  const response = await apiClient.get<ApiResponse<IpdAdmissionResponse>>(`/ipd/admission/${id}`)
  return response.data.data
}

export async function getAvailableBeds(wardType?: WardType): Promise<BedResponse[]> {
  const response = await apiClient.get<ApiResponse<BedResponse[]>>('/ipd/bed/availability', {
    params: wardType ? { wardType } : undefined,
  })
  return response.data.data
}

export async function allocateBed(admissionId: number, bedId: number): Promise<BedResponse> {
  const response = await apiClient.post<ApiResponse<BedResponse>>('/ipd/bed/allocate', { admissionId, bedId })
  return response.data.data
}
