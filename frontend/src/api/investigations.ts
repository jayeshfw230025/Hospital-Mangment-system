import { apiClient, type ApiResponse } from './client'

export type InvestigationCategory = 'LAB' | 'IMAGING' | 'PROCEDURE'

export interface InvestigationTypeOption {
  code: string
  name: string
  category: InvestigationCategory
}

// The backend has no "list investigation types" endpoint - this mirrors the
// non-IPD-only rows seeded in V7__init_investigation_module.sql.
export const INVESTIGATION_TYPES: InvestigationTypeOption[] = [
  { code: 'CBC', name: 'Complete Blood Count (CBC)', category: 'LAB' },
  { code: 'ESR', name: 'ESR', category: 'LAB' },
  { code: 'CRP', name: 'C-Reactive Protein', category: 'LAB' },
  { code: 'LFT', name: 'Liver Function Test (LFT)', category: 'LAB' },
  { code: 'RFT', name: 'Renal Function Test (RFT)', category: 'LAB' },
  { code: 'SERUM_ELECTROLYTES', name: 'Serum Electrolytes', category: 'LAB' },
  { code: 'BLOOD_GLUCOSE', name: 'Blood Glucose (Fasting/Postprandial)', category: 'LAB' },
  { code: 'HBA1C', name: 'HbA1c', category: 'LAB' },
  { code: 'SERUM_AMYLASE', name: 'Serum Amylase', category: 'LAB' },
  { code: 'SERUM_LIPASE', name: 'Serum Lipase', category: 'LAB' },
  { code: 'H_PYLORI', name: 'H.pylori Antigen/Serology', category: 'LAB' },
  { code: 'STOOL_ANALYSIS', name: 'Stool Analysis (C/S)', category: 'LAB' },
  { code: 'URINE_ANALYSIS', name: 'Urine Analysis', category: 'LAB' },
  { code: 'ASCITIC_FLUID_ANALYSIS', name: 'Ascitic Fluid Analysis', category: 'LAB' },
  { code: 'PLEURAL_FLUID_ANALYSIS', name: 'Pleural Fluid Analysis', category: 'LAB' },
  { code: 'USG_ABDOMEN', name: 'USG Abdomen', category: 'IMAGING' },
  { code: 'CT_ABDOMEN', name: 'CT Abdomen', category: 'IMAGING' },
  { code: 'MRI_ABDOMEN', name: 'MRI Abdomen', category: 'IMAGING' },
  { code: 'MRCP', name: 'MRCP', category: 'IMAGING' },
  { code: 'ERCP', name: 'ERCP', category: 'PROCEDURE' },
  { code: 'OGD', name: 'OGD (Upper GI Endoscopy)', category: 'PROCEDURE' },
  { code: 'COLONOSCOPY', name: 'Colonoscopy', category: 'PROCEDURE' },
  { code: 'LIVER_FIBROSCAN', name: 'Liver Fibroscan', category: 'IMAGING' },
  { code: 'BARIUM_STUDIES', name: 'Barium Studies', category: 'IMAGING' },
  { code: 'PET_CT', name: 'PET-CT', category: 'IMAGING' },
]

export type OrderStatus = 'ORDERED' | 'COMPLETED'

export interface InvestigationOrderRequest {
  patientId: string
  visitId?: number | null
  admissionId?: number | null
  investigationTypeCode: string
  notes?: string | null
}

export interface ResultParameterResponse {
  parameterName: string
  value: string
  unit: string | null
  referenceRangeLow: number | null
  referenceRangeHigh: number | null
  abnormal: boolean
  previousValues: string[]
}

export interface InvestigationReportResponse {
  id: number
  orderId: number
  patientId: string
  investigationTypeCode: string
  investigationName: string | null
  reportDate: string
  resultParameters: ResultParameterResponse[]
  hasFile: boolean
  reportFileName: string | null
  notes: string | null
  createdAt: string
}

export interface InvestigationOrderResponse {
  id: number
  patientId: string
  visitId: number | null
  admissionId: number | null
  investigationTypeCode: string
  investigationName: string
  category: InvestigationCategory
  orderedDate: string
  status: OrderStatus
  notes: string | null
  latestReport: InvestigationReportResponse | null
  createdAt: string
}

export async function orderInvestigation(request: InvestigationOrderRequest): Promise<InvestigationOrderResponse> {
  const response = await apiClient.post<ApiResponse<InvestigationOrderResponse>>('/investigations/order', request)
  return response.data.data
}

export async function getInvestigationsByVisit(visitId: number): Promise<InvestigationOrderResponse[]> {
  const response = await apiClient.get<ApiResponse<InvestigationOrderResponse[]>>(`/investigations/visit/${visitId}`)
  return response.data.data
}
