import { apiClient, type ApiResponse } from './client'

export interface ProcedureTypeResponse {
  name: string
  label: string
  requiredDetailKeys: string[]
}

export async function getProcedureTypes(): Promise<ProcedureTypeResponse[]> {
  const response = await apiClient.get<ApiResponse<ProcedureTypeResponse[]>>('/ipd/procedure/types')
  return response.data.data
}

export interface ProcedureRequest {
  admissionId: number
  procedureType: string
  procedureDate?: string | null
  performedByName?: string | null
  notes?: string | null
  details: Record<string, string>
}

export interface ProcedureComplicationResponse {
  id: number
  procedureId: number
  complicationDescription: string
  severity: string | null
  reportedDate: string | null
  reportedByName: string | null
}

export interface ProcedureResponse {
  id: number
  admissionId: number
  procedureType: string
  procedureTypeLabel: string
  procedureDate: string | null
  performedByName: string | null
  notes: string | null
  details: Record<string, unknown>
  complications: ProcedureComplicationResponse[]
  createdAt: string
}

export async function createProcedure(request: ProcedureRequest): Promise<ProcedureResponse> {
  const response = await apiClient.post<ApiResponse<ProcedureResponse>>('/ipd/procedure', request)
  return response.data.data
}

export async function getProceduresByAdmission(admissionId: number): Promise<ProcedureResponse[]> {
  const response = await apiClient.get<ApiResponse<ProcedureResponse[]>>(`/ipd/procedure/${admissionId}`)
  return response.data.data
}

export type SeverityLevel = 'MILD' | 'MODERATE' | 'SEVERE'

export interface ProcedureComplicationRequest {
  procedureId: number
  complicationDescription: string
  severity?: SeverityLevel | null
  reportedDate?: string | null
  reportedByName?: string | null
}

export async function reportComplication(request: ProcedureComplicationRequest): Promise<ProcedureComplicationResponse> {
  const response = await apiClient.post<ApiResponse<ProcedureComplicationResponse>>('/ipd/procedure/complication', request)
  return response.data.data
}
