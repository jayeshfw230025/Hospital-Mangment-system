import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getAdmissionById, type IpdAdmissionResponse } from '../../api/ipdAdmission'
import { extractErrorMessage } from '../../api/client'
import { ProgressNoteTab } from './ProgressNoteTab'
import { MarTab } from './MarTab'
import { IpdVitalsTab } from './IpdVitalsTab'
import { ProcedureTab } from './ProcedureTab'
import { DischargeTab } from './DischargeTab'
import { ClinicalExaminationTab } from '../clinical/ClinicalExaminationTab'

type Tab = 'progress-note' | 'mar' | 'vitals' | 'examination' | 'procedures' | 'discharge'

const TABS: { key: Tab; label: string }[] = [
  { key: 'progress-note', label: 'Progress Notes' },
  { key: 'mar', label: 'MAR' },
  { key: 'vitals', label: 'Vitals' },
  { key: 'examination', label: 'Clinical Examination' },
  { key: 'procedures', label: 'Procedures' },
  { key: 'discharge', label: 'Discharge Summary' },
]

export function IpdEncounterPage() {
  const { admissionId } = useParams<{ admissionId: string }>()
  const [tab, setTab] = useState<Tab>('progress-note')
  const [admission, setAdmission] = useState<IpdAdmissionResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!admissionId) return
    setLoading(true)
    getAdmissionById(Number(admissionId))
      .then(setAdmission)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [admissionId])

  if (!admissionId) return null
  if (loading) return <p className="text-slate-400">Loading...</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (!admission) return null

  const admissionIdNumber = Number(admissionId)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">IPD Admission #{admission.id}</h1>
          <p className="text-sm text-slate-500">
            Patient <span className="font-medium">{admission.patientId}</span> ·{' '}
            {admission.bed ? `Room ${admission.bed.roomNumber} / Bed ${admission.bed.bedNumber}` : 'No bed allocated'} ·{' '}
            {admission.primaryDiagnosisDescription ?? admission.primaryDiagnosisIcd10}
          </p>
          {admission.hardStopAllergies.length > 0 && (
            <p className="mt-1 text-sm font-medium text-red-600">
              ⚠ Hard-stop allergies: {admission.hardStopAllergies.join(', ')}
            </p>
          )}
        </div>
        <Link to={`/patients/${admission.patientId}`} className="text-sm text-indigo-600 hover:underline">
          Back to patient profile
        </Link>
      </div>

      <div className="flex flex-wrap gap-1 border-b border-slate-200">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`rounded-t-md px-4 py-2 text-sm font-medium ${
              tab === t.key ? 'border-b-2 border-indigo-600 text-indigo-700' : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'progress-note' && <ProgressNoteTab admissionId={admissionIdNumber} />}
      {tab === 'mar' && <MarTab admissionId={admissionIdNumber} />}
      {tab === 'vitals' && <IpdVitalsTab patientId={admission.patientId} admissionId={admissionIdNumber} />}
      {tab === 'examination' && (
        <ClinicalExaminationTab context="IPD" patientId={admission.patientId} admissionId={admissionIdNumber} />
      )}
      {tab === 'procedures' && <ProcedureTab admissionId={admissionIdNumber} />}
      {tab === 'discharge' && <DischargeTab admissionId={admissionIdNumber} />}
    </div>
  )
}
