import { useEffect, useState, type FormEvent } from 'react'
import {
  COMPLAINT_TYPES,
  createOpdComplaint,
  getOpdComplaintsByVisit,
  type ComplaintType,
  type DurationUnit,
  type FrequencyLevel,
  type OpdComplaintResponse,
  type SeverityLevel,
} from '../../api/opd'
import { extractErrorMessage } from '../../api/client'

export function ComplaintsTab({ visitId }: { visitId: number }) {
  const [complaints, setComplaints] = useState<OpdComplaintResponse[]>([])
  const [complaintType, setComplaintType] = useState<ComplaintType>('ABDOMINAL_PAIN')
  const [severity, setSeverity] = useState<SeverityLevel | ''>('')
  const [durationValue, setDurationValue] = useState('')
  const [durationUnit, setDurationUnit] = useState<DurationUnit | ''>('')
  const [frequency, setFrequency] = useState<FrequencyLevel | ''>('')
  const [onsetDate, setOnsetDate] = useState('')
  const [notes, setNotes] = useState('')
  const [details, setDetails] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const selectedType = COMPLAINT_TYPES.find((t) => t.value === complaintType)!

  async function loadComplaints() {
    const result = await getOpdComplaintsByVisit(visitId)
    setComplaints(result)
  }

  useEffect(() => {
    loadComplaints().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visitId])

  function handleTypeChange(value: ComplaintType) {
    setComplaintType(value)
    setDetails({})
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await createOpdComplaint({
        visitId,
        complaintType,
        severity: severity || null,
        durationValue: durationValue ? Number(durationValue) : null,
        durationUnit: durationUnit || null,
        frequency: frequency || null,
        onsetDate: onsetDate || null,
        notes: notes || null,
        details,
      })
      setNotes('')
      setDetails({})
      await loadComplaints()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Add Chief Complaint</h2>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Complaint type</span>
          <select
            className="input"
            value={complaintType}
            onChange={(e) => handleTypeChange(e.target.value as ComplaintType)}
          >
            {COMPLAINT_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </label>

        <div className="grid grid-cols-3 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Severity</span>
            <select className="input" value={severity} onChange={(e) => setSeverity(e.target.value as SeverityLevel)}>
              <option value="">-</option>
              <option value="MILD">Mild</option>
              <option value="MODERATE">Moderate</option>
              <option value="SEVERE">Severe</option>
            </select>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Duration</span>
            <input
              className="input"
              type="number"
              value={durationValue}
              onChange={(e) => setDurationValue(e.target.value)}
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Unit</span>
            <select
              className="input"
              value={durationUnit}
              onChange={(e) => setDurationUnit(e.target.value as DurationUnit)}
            >
              <option value="">-</option>
              <option value="HOURS">Hours</option>
              <option value="DAYS">Days</option>
              <option value="WEEKS">Weeks</option>
              <option value="MONTHS">Months</option>
              <option value="YEARS">Years</option>
            </select>
          </label>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Frequency</span>
            <select
              className="input"
              value={frequency}
              onChange={(e) => setFrequency(e.target.value as FrequencyLevel)}
            >
              <option value="">-</option>
              <option value="RARE">Rare</option>
              <option value="OCCASIONAL">Occasional</option>
              <option value="FREQUENT">Frequent</option>
              <option value="CONSTANT">Constant</option>
            </select>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Onset date</span>
            <input type="date" className="input" value={onsetDate} onChange={(e) => setOnsetDate(e.target.value)} />
          </label>
        </div>

        {selectedType.requiredKeys.length > 0 && (
          <div className="space-y-3 rounded-md bg-slate-50 p-3">
            <p className="text-xs font-medium text-slate-500">{selectedType.label} details</p>
            {selectedType.requiredKeys.map((key) => (
              <label key={key} className="block text-sm">
                <span className="mb-1 block font-medium capitalize text-slate-700">
                  {key.replace(/([A-Z])/g, ' $1')}
                </span>
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
          {submitting ? 'Saving...' : 'Add complaint'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Recorded complaints ({complaints.length})</h2>
        {complaints.length === 0 && <p className="text-sm text-slate-400">No complaints recorded for this visit yet.</p>}
        {complaints.map((c) => (
          <div key={c.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">{c.complaintLabel}</p>
              {c.severity && (
                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                  {c.severity}
                </span>
              )}
            </div>
            <p className="mt-1 text-sm text-slate-500">
              {c.durationValue != null && `${c.durationValue}${c.durationUnit ? ` ${c.durationUnit.toLowerCase()}` : ''}`}
              {c.frequency && ` · ${c.frequency.toLowerCase()}`}
              {c.onsetDate && ` · onset ${c.onsetDate}`}
            </p>
            {c.notes && <p className="mt-1 text-sm text-slate-600">{c.notes}</p>}
          </div>
        ))}
      </div>
    </div>
  )
}
