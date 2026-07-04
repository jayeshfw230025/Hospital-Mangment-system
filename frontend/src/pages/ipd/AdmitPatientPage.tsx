import { useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  allocateBed,
  createAdmission,
  getAvailableBeds,
  type AdmissionSource,
  type AdmissionType,
  type BedResponse,
  type IpdAdmissionResponse,
  type WardType,
} from '../../api/ipdAdmission'
import { searchIcd10, type Icd10CodeResponse } from '../../api/opd'
import { extractErrorMessage } from '../../api/client'

export function AdmitPatientPage() {
  const { upid } = useParams<{ upid: string }>()
  const navigate = useNavigate()

  const [admissionType, setAdmissionType] = useState<AdmissionType>('EMERGENCY')
  const [admissionSource, setAdmissionSource] = useState<AdmissionSource>('ER')
  const [primaryQuery, setPrimaryQuery] = useState('')
  const [primaryResults, setPrimaryResults] = useState<Icd10CodeResponse[]>([])
  const [primaryDiagnosis, setPrimaryDiagnosis] = useState<Icd10CodeResponse | null>(null)
  const [clinicalSummary, setClinicalSummary] = useState('')
  const [consentSignature, setConsentSignature] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [admission, setAdmission] = useState<IpdAdmissionResponse | null>(null)
  const [wardType, setWardType] = useState<WardType>('GENERAL')
  const [beds, setBeds] = useState<BedResponse[]>([])
  const [allocating, setAllocating] = useState(false)

  async function handleIcd10Search(value: string) {
    setPrimaryQuery(value)
    if (value.trim().length < 2) {
      setPrimaryResults([])
      return
    }
    try {
      setPrimaryResults(await searchIcd10(value))
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!upid || !primaryDiagnosis) return
    setError(null)
    setSubmitting(true)
    try {
      const created = await createAdmission({
        patientId: upid,
        admissionType,
        admissionSource,
        primaryDiagnosisIcd10: primaryDiagnosis.code,
        clinicalSummary: clinicalSummary || null,
        consentSignature,
      })
      setAdmission(created)
      await loadBeds('GENERAL')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function loadBeds(ward: WardType) {
    setWardType(ward)
    setBeds(await getAvailableBeds(ward))
  }

  async function handleAllocate(bedId: number) {
    if (!admission) return
    setAllocating(true)
    setError(null)
    try {
      await allocateBed(admission.id, bedId)
      navigate(`/ipd/${admission.id}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setAllocating(false)
    }
  }

  if (!upid) return null

  if (admission) {
    return (
      <div className="mx-auto max-w-2xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">Allocate a Bed</h1>
          <p className="text-sm text-slate-500">Admission #{admission.id} created for {upid}</p>
        </div>

        <div className="flex gap-2">
          {(['GENERAL', 'ICU', 'PRIVATE', 'SEMI_PRIVATE'] as WardType[]).map((w) => (
            <button
              key={w}
              onClick={() => loadBeds(w)}
              className={`rounded-md px-3 py-1.5 text-sm font-medium ${
                wardType === w ? 'bg-indigo-600 text-white' : 'border border-slate-300 text-slate-600 hover:bg-slate-50'
              }`}
            >
              {w.replace('_', ' ')}
            </button>
          ))}
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {beds.length === 0 && <p className="col-span-full text-sm text-slate-400">No available beds in this ward.</p>}
          {beds.map((bed) => (
            <button
              key={bed.id}
              disabled={allocating}
              onClick={() => handleAllocate(bed.id)}
              className="rounded-lg border border-slate-200 bg-white p-4 text-left shadow-sm hover:border-indigo-400 hover:shadow-md disabled:opacity-50"
            >
              <p className="font-medium text-slate-800">Room {bed.roomNumber} · Bed {bed.bedNumber}</p>
              <p className="text-xs text-slate-400">{bed.wardType.replace('_', ' ')}</p>
            </button>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">Admit Patient to IPD</h1>
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-6 shadow-sm">
        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Admission type</span>
            <select className="input" value={admissionType} onChange={(e) => setAdmissionType(e.target.value as AdmissionType)}>
              <option value="EMERGENCY">Emergency</option>
              <option value="ELECTIVE">Elective</option>
              <option value="TRANSFER">Transfer</option>
            </select>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Admission source</span>
            <select
              className="input"
              value={admissionSource}
              onChange={(e) => setAdmissionSource(e.target.value as AdmissionSource)}
            >
              <option value="OPD">OPD</option>
              <option value="ER">Emergency Room</option>
              <option value="REFERRAL">Referral</option>
              <option value="TRANSFER">Transfer</option>
            </select>
          </label>
        </div>

        {primaryDiagnosis ? (
          <div className="flex items-center justify-between rounded-md bg-indigo-50 px-3 py-2 text-sm">
            <span>
              <span className="font-medium">{primaryDiagnosis.code}</span> — {primaryDiagnosis.description}
            </span>
            <button type="button" className="text-xs text-indigo-600 hover:underline" onClick={() => setPrimaryDiagnosis(null)}>
              Change
            </button>
          </div>
        ) : (
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Primary diagnosis (ICD-10)</span>
            <input className="input" value={primaryQuery} onChange={(e) => handleIcd10Search(e.target.value)} />
            {primaryResults.length > 0 && (
              <ul className="mt-1 max-h-40 overflow-y-auto rounded-md border border-slate-200 bg-white shadow-sm">
                {primaryResults.map((r) => (
                  <li key={r.code}>
                    <button
                      type="button"
                      className="block w-full px-3 py-2 text-left text-sm hover:bg-slate-50"
                      onClick={() => {
                        setPrimaryDiagnosis(r)
                        setPrimaryResults([])
                      }}
                    >
                      {r.code} — {r.description}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </label>
        )}

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Clinical summary</span>
          <textarea className="input" rows={3} value={clinicalSummary} onChange={(e) => setClinicalSummary(e.target.value)} />
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Consent signature</span>
          <input className="input" value={consentSignature} onChange={(e) => setConsentSignature(e.target.value)} required />
        </label>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting || !primaryDiagnosis}
          className="rounded-md bg-indigo-600 px-5 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Admitting...' : 'Create admission'}
        </button>
      </form>
    </div>
  )
}
