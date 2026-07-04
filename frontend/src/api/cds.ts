import { apiClient, type ApiResponse } from './client'

export interface AdditionalFindings {
  darkUrine: boolean
  dehydration: boolean
  obstructionSymptoms: boolean
  spiderNevi: boolean
  hepaticEncephalopathySigns: boolean
}

export const NO_ADDITIONAL_FINDINGS: AdditionalFindings = {
  darkUrine: false,
  dehydration: false,
  obstructionSymptoms: false,
  spiderNevi: false,
  hepaticEncephalopathySigns: false,
}

export interface CdsAssessRequest {
  patientId: string
  visitId?: number | null
  admissionId?: number | null
  additionalFindings: AdditionalFindings
}

export interface CdsAlertResponse {
  id: number
  patientId: string
  context: string
  ruleName: string
  finding: string
  suggestion: string
  createdAt: string
}

export async function assess(request: CdsAssessRequest): Promise<CdsAlertResponse[]> {
  const response = await apiClient.post<ApiResponse<CdsAlertResponse[]>>('/cds/assess', request)
  return response.data.data
}

export async function getAlertsByPatient(patientId: string): Promise<CdsAlertResponse[]> {
  const response = await apiClient.get<ApiResponse<CdsAlertResponse[]>>(`/cds/alerts/${patientId}`)
  return response.data.data
}

export type AscitesGrade = 'NONE' | 'MILD' | 'MODERATE_SEVERE'
export type EncephalopathyGrade = 'NONE' | 'GRADE_1_2' | 'GRADE_3_4'

export interface CtpScoreRequest {
  ascites: AscitesGrade
  encephalopathy: EncephalopathyGrade
  bilirubinMgDl: number
  albuminGDl: number
  inr: number
}
export interface CtpScoreResponse {
  totalScore: number
  ctpClass: string
  interpretation: string
}
export async function calculateCtp(request: CtpScoreRequest): Promise<CtpScoreResponse> {
  const response = await apiClient.post<ApiResponse<CtpScoreResponse>>('/cds/score/ctp', request)
  return response.data.data
}

export interface MeldScoreRequest {
  bilirubinMgDl: number
  inr: number
  creatinineMgDl: number
  sodiumMeqL: number
}
export interface MeldScoreResponse {
  meldScore: number
  meldNaScore: number
  interpretation: string
}
export async function calculateMeld(request: MeldScoreRequest): Promise<MeldScoreResponse> {
  const response = await apiClient.post<ApiResponse<MeldScoreResponse>>('/cds/score/meld', request)
  return response.data.data
}

export interface MayoScoreRequest {
  stoolFrequencySubscore: number
  rectalBleedingSubscore: number
  endoscopySubscore: number
  physicianGlobalAssessmentSubscore: number
}
export interface MayoScoreResponse {
  totalScore: number
  diseaseActivity: string
  interpretation: string
}
export async function calculateMayo(request: MayoScoreRequest): Promise<MayoScoreResponse> {
  const response = await apiClient.post<ApiResponse<MayoScoreResponse>>('/cds/score/mayo', request)
  return response.data.data
}

export interface BisapScoreRequest {
  bunOver25: boolean
  impairedMentalStatus: boolean
  sirsPresent: boolean
  ageOver60: boolean
  pleuralEffusion: boolean
}
export interface BisapScoreResponse {
  totalScore: number
  mortalityRiskCategory: string
  interpretation: string
}
export async function calculateBisap(request: BisapScoreRequest): Promise<BisapScoreResponse> {
  const response = await apiClient.post<ApiResponse<BisapScoreResponse>>('/cds/score/bisap', request)
  return response.data.data
}

export interface CdaiScoreRequest {
  stoolFrequencySum: number
  abdominalPainSum: number
  wellBeingSum: number
  extraintestinalManifestationsCount: number
}
export interface CdaiScoreResponse {
  totalScore: number
  diseaseActivity: string
  interpretation: string
}
export async function calculateCdai(request: CdaiScoreRequest): Promise<CdaiScoreResponse> {
  const response = await apiClient.post<ApiResponse<CdaiScoreResponse>>('/cds/score/cdai', request)
  return response.data.data
}
