import { apiClient, type ApiResponse } from './client'
import type { PageResponse } from './patients'

export interface AuditLogResponse {
  id: number
  timestamp: string
  userId: number | null
  username: string | null
  userRole: string | null
  action: string
  moduleName: string
  recordId: string | null
  relatedPatientId: string | null
  oldValueJson: string | null
  newValueJson: string | null
  ipAddress: string | null
  sessionId: string | null
  deviceInfo: string | null
}

export async function getAuditTrail(page = 0, size = 50): Promise<PageResponse<AuditLogResponse>> {
  const response = await apiClient.get<ApiResponse<PageResponse<AuditLogResponse>>>('/audit-trail', {
    params: { page, size },
  })
  return response.data.data
}

export async function getAuditTrailForPatient(patientId: string, page = 0, size = 50): Promise<PageResponse<AuditLogResponse>> {
  const response = await apiClient.get<ApiResponse<PageResponse<AuditLogResponse>>>(`/audit-trail/patient/${patientId}`, {
    params: { page, size },
  })
  return response.data.data
}
