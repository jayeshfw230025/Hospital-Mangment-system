import { apiClient, type ApiResponse } from './client'

export type Gender = 'MALE' | 'FEMALE' | 'OTHER'
export type MaritalStatus = 'MARRIED' | 'UNMARRIED' | 'DIVORCED' | 'WIDOWED'
export type BloodGroup =
  | 'A_POSITIVE'
  | 'A_NEGATIVE'
  | 'B_POSITIVE'
  | 'B_NEGATIVE'
  | 'AB_POSITIVE'
  | 'AB_NEGATIVE'
  | 'O_POSITIVE'
  | 'O_NEGATIVE'
  | 'UNKNOWN'

export const BLOOD_GROUP_LABELS: Record<BloodGroup, string> = {
  A_POSITIVE: 'A+',
  A_NEGATIVE: 'A-',
  B_POSITIVE: 'B+',
  B_NEGATIVE: 'B-',
  AB_POSITIVE: 'AB+',
  AB_NEGATIVE: 'AB-',
  O_POSITIVE: 'O+',
  O_NEGATIVE: 'O-',
  UNKNOWN: 'Unknown',
}

export interface AddressDto {
  addressLine1?: string
  addressLine2?: string
  city?: string
  state?: string
  district?: string
  pinCode?: string
  country?: string
}

export interface EmergencyContactDto {
  name?: string
  contactNumber?: string
  relation?: string
}

export interface PatientRegistrationRequest {
  abhaNumber?: string | null
  fullName: string
  dateOfBirth: string
  gender: Gender
  maritalStatus?: MaritalStatus | null
  bloodGroup?: BloodGroup | null
  nationality?: string | null
  religion?: string | null
  occupation?: string | null
  education?: string | null
  primaryContactNumber: string
  secondaryContactNumber?: string | null
  email?: string | null
  address?: AddressDto | null
  geoLocation?: null
  aadhaarNumber?: string | null
  govtIdType?: string | null
  govtIdNumber?: string | null
  emergencyContact?: EmergencyContactDto | null
  referralDetails?: null
}

export interface PatientResponse {
  id: number
  upid: string
  abhaNumber: string | null
  fullName: string
  dateOfBirth: string
  gender: Gender
  maritalStatus: MaritalStatus | null
  bloodGroup: BloodGroup | null
  nationality: string | null
  religion: string | null
  occupation: string | null
  education: string | null
  primaryContactNumber: string
  secondaryContactNumber: string | null
  email: string | null
  address: AddressDto | null
  geoLocation: unknown
  govtIdType: string | null
  emergencyContact: EmergencyContactDto | null
  referralDetails: unknown
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface PatientSearchParams {
  fullName?: string
  contactNumber?: string
  dateOfBirth?: string
  upid?: string
  page?: number
  size?: number
}

export async function registerPatient(request: PatientRegistrationRequest): Promise<PatientResponse> {
  const response = await apiClient.post<ApiResponse<PatientResponse>>('/patients/register', request)
  return response.data.data
}

export async function getPatientByUpid(upid: string): Promise<PatientResponse> {
  const response = await apiClient.get<ApiResponse<PatientResponse>>(`/patients/${upid}`)
  return response.data.data
}

export async function searchPatients(params: PatientSearchParams): Promise<PageResponse<PatientResponse>> {
  const response = await apiClient.get<ApiResponse<PageResponse<PatientResponse>>>('/patients/search', {
    params: {
      fullName: params.fullName || undefined,
      contactNumber: params.contactNumber || undefined,
      dateOfBirth: params.dateOfBirth || undefined,
      upid: params.upid || undefined,
      page: params.page ?? 0,
      size: params.size ?? 10,
    },
  })
  return response.data.data
}

export async function getPatientQrCode(upid: string): Promise<string> {
  const response = await apiClient.get<ApiResponse<string>>(`/patients/${upid}/qr-code`)
  return response.data.data
}
