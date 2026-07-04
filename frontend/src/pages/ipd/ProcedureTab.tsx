import { useEffect, useState, type FormEvent } from 'react'
import {
  createProcedure,
  getProceduresByAdmission,
  getProcedureTypes,
  reportComplication,
  type ProcedureResponse,
  type ProcedureTypeResponse,
  type SeverityLevel,
} from '../../api/procedures'
import { extractErrorMessage } from '../../api/client'

export function ProcedureTab({ admissionId }: { admissionId: number }) {
  const [types, setTypes] = useState<ProcedureTypeResponse[]>([])
  const [procedures, setProcedures] = useState<ProcedureResponse[]>([])
  const [procedureType, setProcedureType] = useState('')
  const [performedByName, setPerformedByName] = useState('')
  const [notes, setNotes] = useState('')
  const [details, setDetails] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [complicationTarget, setComplicationTarget] = useState<ProcedureResponse | null>(null)
  const [complicationDescription, setComplicationDescription] = useState('')
  const [complicationSeverity, setComplicationSeverity] = useState<SeverityLevel | ''>('')

  const selectedType = types.find((t) => t.name === procedureType) ?? null

  async function loadData() {
    const [typeList, procedureList] = await Promise.all([getProcedureTypes(), getProceduresByAdmission(admissionId)])
    setTypes(typeList)
    setProcedures(procedureList)
    if (typeList.length > 0) setProcedureType(typeList[0].name)
  }

  useEffect(() => {
    loadData().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [admissionId])

  function handleTypeChange(value: string) {
    setProcedureType(value)
    setDetails({})
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!procedureType) return
    setError(null)
    setSubmitting(true)
    try {
      await createProcedure({
        admissionId,
        procedureType,
        performedByName: performedByName || null,
        notes: notes || null,
        details,
      })
      setPerformedByName('')
      setNotes('')
      setDetails({})
      setProcedures(await getProceduresByAdmission(admissionId))
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleReportComplication(event: FormEvent) {
    event.preventDefault()
    if (!complicationTarget) return
    setError(null)
    try {
      await reportComplication({
        procedureId: complicationTarget.id,
        complicationDescription,
        severity: complicationSeverity || null,
      })
      setComplicationTarget(null)
      setComplicationDescription('')
      setComplicationSeverity('')
      setProcedures(await getProceduresByAdmission(admissionId))
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Record Procedure</h2>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Procedure type</span>
          <select className="input" value={procedureType} onChange={(e) => handleTypeChange(e.target.value)}>
            {types.map((t) => (
              <option key={t.name} value={t.name}>
                {t.label}
              </option>
            ))}
          </select>
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Performed by</span>
          <input className="input" value={performedByName} onChange={(e) => setPerformedByName(e.target.value)} />
        </label>

        {selectedType && selectedType.requiredDetailKeys.length > 0 && (
          <div className="space-y-3 rounded-md bg-slate-50 p-3">
            <p className="text-xs font-medium text-slate-500">{selectedType.label} details</p>
            {selectedType.requiredDetailKeys.map((key) => (
              <label key={key} className="block text-sm">
                <span className="mb-1 block font-medium capitalize text-slate-700">{key.replace(/([A-Z])/g, ' $1')}</span>
                <input
                  className="input"
                  value={details[key] ?? ''}
                  onChange={(e) => setDetails((prev) => ({ ...prev, [key]: e.target.value }))}
                />
              </label>
            ))}
          </div>
        )}

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Notes</span>
          <textarea className="input" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
        </label>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Record procedure'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Procedures ({procedures.length})</h2>
        {procedures.length === 0 && <p className="text-sm text-slate-400">No procedures recorded for this admission yet.</p>}
        {procedures.map((p) => (
          <div key={p.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">{p.procedureTypeLabel}</p>
              <button
                className="text-xs text-indigo-600 hover:underline"
                onClick={() => setComplicationTarget(complicationTarget?.id === p.id ? null : p)}
              >
                Report complication
              </button>
            </div>
            <p className="mt-1 text-xs text-slate-400">
              {p.procedureDate} {p.performedByName && `· ${p.performedByName}`}
            </p>
            {p.notes && <p className="mt-1 text-sm text-slate-600">{p.notes}</p>}
            {p.complications.length > 0 && (
              <div className="mt-2 space-y-1">
                {p.complications.map((c) => (
                  <p key={c.id} className="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700">
                    ⚠ {c.complicationDescription} {c.severity && `(${c.severity})`}
                  </p>
                ))}
              </div>
            )}
            {complicationTarget?.id === p.id && (
              <form onSubmit={handleReportComplication} className="mt-3 space-y-2 rounded-md bg-slate-50 p-3">
                <input
                  className="input"
                  placeholder="Complication description"
                  value={complicationDescription}
                  onChange={(e) => setComplicationDescription(e.target.value)}
                  required
                />
                <select
                  className="input"
                  value={complicationSeverity}
                  onChange={(e) => setComplicationSeverity(e.target.value as SeverityLevel)}
                >
                  <option value="">Severity</option>
                  <option value="MILD">Mild</option>
                  <option value="MODERATE">Moderate</option>
                  <option value="SEVERE">Severe</option>
                </select>
                <button
                  type="submit"
                  className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700"
                >
                  Submit complication
                </button>
              </form>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
