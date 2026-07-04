import { useEffect, useState, type FormEvent } from 'react'
import {
  createDiagnosis,
  getDiagnosesByPatient,
  searchIcd10,
  type DiagnosisResponse,
  type DiagnosisType,
  type Icd10CodeResponse,
} from '../../api/opd'
import { extractErrorMessage } from '../../api/client'

export function DiagnosisTab({ patientId }: { patientId: string }) {
  const [diagnoses, setDiagnoses] = useState<DiagnosisResponse[]>([])
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Icd10CodeResponse[]>([])
  const [selectedCode, setSelectedCode] = useState<Icd10CodeResponse | null>(null)
  const [diagnosisType, setDiagnosisType] = useState<DiagnosisType>('PRIMARY')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function loadDiagnoses() {
    const result = await getDiagnosesByPatient(patientId)
    setDiagnoses(result)
  }

  useEffect(() => {
    loadDiagnoses().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [patientId])

  async function handleSearch(value: string) {
    setQuery(value)
    if (value.trim().length < 2) {
      setResults([])
      return
    }
    try {
      const codes = await searchIcd10(value)
      setResults(codes)
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
      await createDiagnosis({
        patientId,
        icd10Code: selectedCode.code,
        diagnosisType,
        notes: notes || null,
      })
      setSelectedCode(null)
      setQuery('')
      setResults([])
      setNotes('')
      await loadDiagnoses()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Add Diagnosis</h2>

        {selectedCode ? (
          <div className="flex items-center justify-between rounded-md bg-indigo-50 px-3 py-2 text-sm">
            <span>
              <span className="font-medium">{selectedCode.code}</span> — {selectedCode.description}
            </span>
            <button
              type="button"
              className="text-xs text-indigo-600 hover:underline"
              onClick={() => setSelectedCode(null)}
            >
              Change
            </button>
          </div>
        ) : (
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Search ICD-10 (code or description)</span>
            <input className="input" value={query} onChange={(e) => handleSearch(e.target.value)} placeholder="e.g. K25 or ulcer" />
            {results.length > 0 && (
              <ul className="mt-1 max-h-48 overflow-y-auto rounded-md border border-slate-200 bg-white shadow-sm">
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
                      <span className="font-medium">{r.code}</span> — {r.description}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </label>
        )}

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Type</span>
          <select className="input" value={diagnosisType} onChange={(e) => setDiagnosisType(e.target.value as DiagnosisType)}>
            <option value="PRIMARY">Primary</option>
            <option value="SECONDARY">Secondary</option>
          </select>
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Notes</span>
          <textarea className="input" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
        </label>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting || !selectedCode}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Add diagnosis'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Diagnoses ({diagnoses.length})</h2>
        {diagnoses.length === 0 && <p className="text-sm text-slate-400">No diagnoses recorded yet.</p>}
        {diagnoses.map((d) => (
          <div key={d.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">
                {d.icd10Code} — {d.icd10Description}
              </p>
              <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                {d.diagnosisType}
              </span>
            </div>
            <p className="mt-1 text-xs text-slate-400">
              {d.status} {d.diagnosedDate && `· diagnosed ${d.diagnosedDate}`}
            </p>
            {d.notes && <p className="mt-1 text-sm text-slate-600">{d.notes}</p>}
          </div>
        ))}
      </div>
    </div>
  )
}
