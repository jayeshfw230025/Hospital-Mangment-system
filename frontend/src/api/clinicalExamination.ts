import { apiClient, type ApiResponse } from './client'

export type ExaminationContext = 'OPD' | 'IPD'
export type BowelSounds = 'NORMAL' | 'INCREASED' | 'DECREASED' | 'ABSENT'
export type MassMobility = 'MOBILE' | 'FIXED'
export type MassConsistency = 'SOFT' | 'FIRM' | 'HARD'
export type PupillaryReflex = 'NORMAL' | 'SLUGGISH' | 'FIXED'

export interface AbdominalExaminationDto {
  scarsPresent?: boolean | null
  distensionPresent?: boolean | null
  visiblePeristalsis?: boolean | null
  tenderness?: boolean | null
  tendernessSite?: string | null
  guarding?: boolean | null
  rigidity?: boolean | null
  organomegaly?: string | null
  percussionDullness?: boolean | null
  tympanic?: boolean | null
  bowelSounds?: BowelSounds | null
  notes?: string | null
}

export interface DigitalRectalExaminationDto {
  fissures?: boolean | null
  fistula?: boolean | null
  externalPiles?: boolean | null
  sphincterTone?: string | null
  massPresent?: boolean | null
  massDescription?: string | null
  bloodOnFinger?: boolean | null
  proctoscopyPerformed?: boolean | null
  proctoscopyFindings?: string | null
}

export interface JaundiceAssessmentDto {
  icterusSclera?: boolean | null
  icterusSkin?: boolean | null
  icterusPalmar?: boolean | null
  scratchMarksPresent?: boolean | null
}

export interface HerniaExaminationDto {
  herniaPresent?: boolean | null
  site?: string | null
  reducible?: boolean | null
  coughImpulse?: boolean | null
}

export interface LymphNodeExaminationDto {
  cervicalNodesPalpable?: boolean | null
  supraclavicularNodesPalpable?: boolean | null
  inguinalNodesPalpable?: boolean | null
  notes?: string | null
}

export interface GiMassExaminationDto {
  massPresent?: boolean | null
  location?: string | null
  sizeCm?: number | null
  mobility?: MassMobility | null
  consistency?: MassConsistency | null
}

export interface AscitesAssessmentDto {
  shiftingDullnessPresent?: boolean | null
  fluidThrillPresent?: boolean | null
  notes?: string | null
}

export interface SystemicExaminationDto {
  chestExpansion?: string | null
  breathSounds?: string | null
  heartSounds?: string | null
  murmursPresent?: boolean | null
  murmurDescription?: string | null
  jvp?: string | null
  gcsScore?: number | null
  pupillaryReflex?: PupillaryReflex | null
  motorFindings?: string | null
  sensoryFindings?: string | null
}

export interface ClinicalExaminationRequest {
  patientId: string
  visitId?: number | null
  admissionId?: number | null
  abdominalExamination?: AbdominalExaminationDto | null
  digitalRectalExamination?: DigitalRectalExaminationDto | null
  jaundiceAssessment?: JaundiceAssessmentDto | null
  herniaExamination?: HerniaExaminationDto | null
  lymphNodeExamination?: LymphNodeExaminationDto | null
  giMassExamination?: GiMassExaminationDto | null
  ascitesAssessment?: AscitesAssessmentDto | null
  systemicExamination?: SystemicExaminationDto | null
  abdominalGirthCm?: number | null
}

export interface ClinicalExaminationResponse extends ClinicalExaminationRequest {
  id: number
  examinationContext: ExaminationContext
  createdAt: string
}

export async function createOpdClinicalExamination(
  request: ClinicalExaminationRequest,
): Promise<ClinicalExaminationResponse> {
  const response = await apiClient.post<ApiResponse<ClinicalExaminationResponse>>('/clinical/opd', request)
  return response.data.data
}

export async function getOpdClinicalExaminations(visitId: number): Promise<ClinicalExaminationResponse[]> {
  const response = await apiClient.get<ApiResponse<ClinicalExaminationResponse[]>>(`/clinical/opd/${visitId}`)
  return response.data.data
}

export async function createIpdClinicalExamination(
  request: ClinicalExaminationRequest,
): Promise<ClinicalExaminationResponse> {
  const response = await apiClient.post<ApiResponse<ClinicalExaminationResponse>>('/clinical/ipd', request)
  return response.data.data
}

export async function getIpdClinicalExaminations(admissionId: number): Promise<ClinicalExaminationResponse[]> {
  const response = await apiClient.get<ApiResponse<ClinicalExaminationResponse[]>>(`/clinical/ipd/${admissionId}`)
  return response.data.data
}

export async function updateClinicalExamination(
  id: number,
  request: ClinicalExaminationRequest,
): Promise<ClinicalExaminationResponse> {
  const response = await apiClient.put<ApiResponse<ClinicalExaminationResponse>>(`/clinical/${id}`, request)
  return response.data.data
}
