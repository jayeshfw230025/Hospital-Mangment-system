import { apiClient, type ApiResponse } from './client'

export interface AbhaLinkInitiationResponse {
  txnId: string
  message: string
}

export async function initiateAbhaLink(patientId: string, abhaNumber: string): Promise<AbhaLinkInitiationResponse> {
  const response = await apiClient.post<ApiResponse<AbhaLinkInitiationResponse>>('/integration/abdm/link', {
    patientId,
    abhaNumber,
  })
  return response.data.data
}

export interface AbdmConsentResponse {
  consentId: string
  patientId: string
  purpose: string
  hiTypes: string[]
  status: string
  grantedAt: string
  expiresAt: string
}

export async function createAbdmConsent(
  patientId: string,
  purpose: string,
  hiTypes: string[],
  validityDays: number,
): Promise<AbdmConsentResponse> {
  const response = await apiClient.post<ApiResponse<AbdmConsentResponse>>('/integration/abdm/consent', {
    patientId,
    purpose,
    hiTypes,
    validityDays,
  })
  return response.data.data
}

export interface AbdmHealthRecordResponse {
  patientId: string
  consentId: string
  message: string
  bundle: { resourceType: string; type: string; total: number }
}

export async function getAbdmHealthRecord(patientId: string, consentId: string): Promise<AbdmHealthRecordResponse> {
  const response = await apiClient.post<ApiResponse<AbdmHealthRecordResponse>>('/integration/abdm/health-record', {
    patientId,
    consentId,
  })
  return response.data.data
}

export interface LisStatusResponse {
  connected: boolean
  message: string
}

export async function getLisStatus(): Promise<LisStatusResponse> {
  const response = await apiClient.get<ApiResponse<LisStatusResponse>>('/integration/lis/status')
  return response.data.data
}
