import { apiClient, type ApiResponse } from './client'

export type FoodInstruction = 'BEFORE_FOOD' | 'AFTER_FOOD' | 'WITH_FOOD' | 'EMPTY_STOMACH' | 'ANYTIME'

export interface DrugResponse {
  id: number
  genericName: string
  brandName: string | null
  category: string
  unit: string
  strength: string | null
  routeOfAdministration: string | null
  schedule: string
  contraindications: string | null
  drugInteractions: string | null
  pediatricDoseMgPerKg: number | null
  adultDose: string | null
  active: boolean
}

export async function searchDrugs(query: string): Promise<DrugResponse[]> {
  const response = await apiClient.get<ApiResponse<DrugResponse[]>>('/drugs/search', { params: { q: query } })
  return response.data.data
}

export interface PrescriptionItemRequest {
  drugId: number
  dosage: string
  frequency: string
  route?: string | null
  durationDays?: number | null
  foodInstruction?: FoodInstruction | null
  refillsAllowed?: number | null
  patientWeightKg?: number | null
}

export interface PrescriptionRequest {
  patientId: string
  visitId?: number | null
  admissionId?: number | null
  doctorName: string
  digitalSignature: string
  templateUsed?: string | null
  items: PrescriptionItemRequest[]
}

export interface PrescriptionItemResponse {
  drugId: number
  genericName: string
  brandName: string | null
  dosage: string
  frequency: string
  route: string | null
  durationDays: number | null
  foodInstruction: FoodInstruction | null
  generatedInstructions: string
  refillsAllowed: number | null
  refillsUsed: number
  calculatedPediatricDoseMg: number | null
}

export interface DrugInteractionWarning {
  drugA: string
  drugB: string
  description: string
}

export interface NutritionAlert {
  drugName: string
  alert: string
}

export interface PrescriptionResponse {
  id: number
  patientId: string
  visitId: number | null
  admissionId: number | null
  prescribedDate: string
  doctorName: string
  templateUsed: string | null
  items: PrescriptionItemResponse[]
  interactionWarnings: DrugInteractionWarning[]
  nutritionAlerts: NutritionAlert[]
  createdAt: string
}

export async function createPrescription(request: PrescriptionRequest): Promise<PrescriptionResponse> {
  const response = await apiClient.post<ApiResponse<PrescriptionResponse>>('/prescriptions', request)
  return response.data.data
}

export async function getPrescriptionsByPatient(patientId: string): Promise<PrescriptionResponse[]> {
  const response = await apiClient.get<ApiResponse<PrescriptionResponse[]>>(`/prescriptions/${patientId}`)
  return response.data.data
}

export interface TemplateResponse {
  name: string
  label: string
  categories: string[]
  suggestedDrugs: DrugResponse[]
}

export async function getTemplates(): Promise<TemplateResponse[]> {
  const response = await apiClient.get<ApiResponse<TemplateResponse[]>>('/prescriptions/templates')
  return response.data.data
}
