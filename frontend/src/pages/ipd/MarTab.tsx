import { useEffect, useState, type FormEvent } from 'react'
import { getMarByAdmission, recordMar, type AdministrationStatus, type MarResponse } from '../../api/ipdCare'
import { extractErrorMessage } from '../../api/client'

export function MarTab({ admissionId }: { admissionId: number }) {
  const [records, setRecords] = useState<MarResponse[]>([])
  const [drugName, setDrugName] = useState('')
  const [dosage, setDosage] = useState('')
  const [route, setRoute] = useState('')
  const [scheduledTime, setScheduledTime] = useState('')
  const [administeredByName, setAdministeredByName] = useState('')
  const [status, setStatus] = useState<AdministrationStatus>('ADMINISTERED')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function loadRecords() {
    setRecords(await getMarByAdmission(admissionId))
  }

  useEffect(() => {
    loadRecords().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [admissionId])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await recordMar({
        admissionId,
        drugName,
        dosage: dosage || null,
        route: route || null,
        scheduledTime: scheduledTime ? new Date(scheduledTime).toISOString() : new Date().toISOString(),
        administeredTime: status === 'ADMINISTERED' ? new Date().toISOString() : null,
        administeredByName: administeredByName || null,
        status,
      })
      setDrugName('')
      setDosage('')
      setRoute('')
      setScheduledTime('')
      await loadRecords()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Medication Administration Record</h2>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Drug name</span>
          <input className="input" value={drugName} onChange={(e) => setDrugName(e.target.value)} required />
        </label>

        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Dosage</span>
            <input className="input" value={dosage} onChange={(e) => setDosage(e.target.value)} />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Route</span>
            <input className="input" value={route} onChange={(e) => setRoute(e.target.value)} />
          </label>
        </div>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Scheduled time</span>
          <input
            type="datetime-local"
            className="input"
            value={scheduledTime}
            onChange={(e) => setScheduledTime(e.target.value)}
          />
        </label>

        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Administered by</span>
            <input className="input" value={administeredByName} onChange={(e) => setAdministeredByName(e.target.value)} />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Status</span>
            <select className="input" value={status} onChange={(e) => setStatus(e.target.value as AdministrationStatus)}>
              <option value="ADMINISTERED">Administered</option>
              <option value="MISSED">Missed</option>
              <option value="REFUSED">Refused</option>
              <option value="PENDING">Pending</option>
            </select>
          </label>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Record administration'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Records ({records.length})</h2>
        {records.length === 0 && <p className="text-sm text-slate-400">No MAR entries for this admission yet.</p>}
        {records.map((r) => (
          <div key={r.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">{r.drugName}</p>
              <span
                className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                  r.status === 'ADMINISTERED'
                    ? 'bg-green-100 text-green-700'
                    : r.status === 'MISSED' || r.status === 'REFUSED'
                      ? 'bg-red-100 text-red-700'
                      : 'bg-amber-100 text-amber-700'
                }`}
              >
                {r.status}
              </span>
            </div>
            <p className="mt-1 text-sm text-slate-500">
              {r.dosage} {r.route}
            </p>
            <p className="text-xs text-slate-400">Scheduled: {new Date(r.scheduledTime).toLocaleString()}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
