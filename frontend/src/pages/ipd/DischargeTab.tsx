import { useEffect, useState, type FormEvent } from 'react'
import {
  createDischargeSummary,
  getDischargeSummaryByAdmission,
  type DischargeCondition,
  type DischargeSummaryResponse,
  type DischargeType,
} from '../../api/discharge'
import { searchIcd10, type Icd10CodeResponse } from '../../api/opd'
import { extractErrorMessage } from '../../api/client'

export function DischargeTab({ admissionId }: { admissionId: number }) {
  const [summary, setSummary] = useState<DischargeSummaryResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [dischargeType, setDischargeType] = useState<DischargeType>('RECOVERED')
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Icd10CodeResponse[]>([])
  const [selectedCode, setSelectedCode] = useState<Icd10CodeResponse | null>(null)
  const [summaryText, setSummaryText] = useState('')
  const [followUpInstructions, setFollowUpInstructions] = useState('')
  const [dischargeCondition, setDischargeCondition] = useState<DischargeCondition>('STABLE')
  const [doctorName, setDoctorName] = useState('')
  const [signature, setSignature] = useState('')

  useEffect(() => {
    setLoading(true)
    getDischargeSummaryByAdmission(admissionId)
      .then(setSummary)
      .finally(() => setLoading(false))
  }, [admissionId])

  async function handleSearch(value: string) {
    setQuery(value)
    if (value.trim().length < 2) {
      setResults([])
      return
    }
    try {
      setResults(await searchIcd10(value))
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!selectedCode) return
    setError(null)
    setSubmitting(true)
    try {
      const created = await createDischargeSummary({
        admissionId,
        dischargeType,
        primaryDiagnosisIcd10: selectedCode.code,
        summaryOfHospitalStay: summaryText || null,
        followUpInstructions: followUpInstructions || null,
        dischargeCondition,
        dischargedByDoctorName: doctorName,
        dischargedByDoctorSignature: signature,
        medicalRecordsChecked: true,
      })
      setSummary(created)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="text-slate-400">Loading...</p>

  if (summary) {
    return (
      <div className="mx-auto max-w-2xl space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-800">Discharge Summary #{summary.id}</h2>
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
            {summary.dischargeType}
          </span>
        </div>
        <dl className="space-y-2 text-sm">
          <Row label="Patient" value={`${summary.patientName} (${summary.patientId})`} />
          <Row label="Length of stay" value={summary.lengthOfStayDays != null ? `${summary.lengthOfStayDays} days` : '-'} />
          <Row label="Primary diagnosis" value={`${summary.primaryDiagnosisIcd10} — ${summary.primaryDiagnosisDescription ?? ''}`} />
          <Row label="Condition at discharge" value={summary.dischargeCondition ?? '-'} />
        </dl>

        {summary.significantProcedures.length > 0 && (
          <Block title="Significant procedures" items={summary.significantProcedures} />
        )}
        {summary.complicationsDuringStay.length > 0 && (
          <Block title="Complications during stay" items={summary.complicationsDuringStay} />
        )}
        {summary.dischargeMedications.length > 0 && (
          <div>
            <p className="text-xs font-semibold text-slate-500">Discharge medications (auto-populated)</p>
            <ul className="mt-1 space-y-1 text-sm text-slate-600">
              {summary.dischargeMedications.map((m, i) => (
                <li key={i}>
                  {m.drugName} — {m.dosage} {m.frequency}
                </li>
              ))}
            </ul>
          </div>
        )}
        {summary.dischargeDietPlan && (
          <div>
            <p className="text-xs font-semibold text-slate-500">Diet plan (auto-generated)</p>
            <p className="mt-1 text-sm text-slate-600">{summary.dischargeDietPlan}</p>
          </div>
        )}
        {summary.followUpInstructions && (
          <div>
            <p className="text-xs font-semibold text-slate-500">Follow-up instructions</p>
            <p className="mt-1 text-sm text-slate-600">{summary.followUpInstructions}</p>
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Create Discharge Summary</h2>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Discharge type</span>
          <select className="input" value={dischargeType} onChange={(e) => setDischargeType(e.target.value as DischargeType)}>
            <option value="RECOVERED">Recovered</option>
            <option value="IMPROVED">Improved</option>
            <option value="STABLE">Stable</option>
            <option value="TRANSFERRED">Transferred</option>
            <option value="LAMA">LAMA</option>
            <option value="ABSCONDED">Absconded</option>
            <option value="EXPIRED">Expired</option>
          </select>
        </label>

        {selectedCode ? (
          <div className="flex items-center justify-between rounded-md bg-indigo-50 px-3 py-2 text-sm">
            <span>
              <span className="font-medium">{selectedCode.code}</span> — {selectedCode.description}
            </span>
            <button type="button" className="text-xs text-indigo-600 hover:underline" onClick={() => setSelectedCode(null)}>
              Change
            </button>
          </div>
        ) : (
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Primary diagnosis (ICD-10)</span>
            <input className="input" value={query} onChange={(e) => handleSearch(e.target.value)} />
            {results.length > 0 && (
              <ul className="mt-1 max-h-40 overflow-y-auto rounded-md border border-slate-200 bg-white shadow-sm">
                {results.map((r) => (
                  <li key={r.code}>
                    <button
                      type="button"
                      className="block w-full px-3 py-2 text-left text-sm hover:bg-slate-50"
                      onClick={() => {
                        setSelectedCode(r)
                        setResults([])
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
          <span className="mb-1 block font-medium text-slate-700">Summary of hospital stay</span>
          <textarea className="input" rows={3} value={summaryText} onChange={(e) => setSummaryText(e.target.value)} />
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Follow-up instructions</span>
          <textarea className="input" rows={2} value={followUpInstructions} onChange={(e) => setFollowUpInstructions(e.target.value)} />
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Condition at discharge</span>
          <select
            className="input"
            value={dischargeCondition}
            onChange={(e) => setDischargeCondition(e.target.value as DischargeCondition)}
          >
            <option value="STABLE">Stable</option>
            <option value="IMPROVING">Improving</option>
            <option value="PALLIATIVE">Palliative</option>
          </select>
        </label>

        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Discharging doctor</span>
            <input className="input" value={doctorName} onChange={(e) => setDoctorName(e.target.value)} required />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Signature</span>
            <input className="input" value={signature} onChange={(e) => setSignature(e.target.value)} required />
          </label>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting || !selectedCode}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Creating...' : 'Create discharge summary'}
        </button>
      </form>
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-slate-400">{label}</dt>
      <dd className="text-right text-slate-700">{value}</dd>
    </div>
  )
}

function Block({ title, items }: { title: string; items: string[] }) {
  return (
    <div>
      <p className="text-xs font-semibold text-slate-500">{title}</p>
      <ul className="mt-1 space-y-1 text-sm text-slate-600">
        {items.map((item, i) => (
          <li key={i}>{item}</li>
        ))}
      </ul>
    </div>
  )
}
