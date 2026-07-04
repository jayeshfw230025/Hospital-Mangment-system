import { useEffect, useState, type FormEvent } from 'react'
import {
  INVESTIGATION_TYPES,
  getInvestigationsByVisit,
  orderInvestigation,
  type InvestigationOrderResponse,
} from '../../api/investigations'
import { extractErrorMessage } from '../../api/client'

const CATEGORY_LABELS: Record<string, string> = { LAB: 'Laboratory', IMAGING: 'Imaging', PROCEDURE: 'Endoscopy/Procedure' }

export function InvestigationsTab({ patientId, visitId }: { patientId: string; visitId: number }) {
  const [orders, setOrders] = useState<InvestigationOrderResponse[]>([])
  const [investigationTypeCode, setInvestigationTypeCode] = useState(INVESTIGATION_TYPES[0].code)
  const [notes, setNotes] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function loadOrders() {
    const result = await getInvestigationsByVisit(visitId)
    setOrders(result)
  }

  useEffect(() => {
    loadOrders().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visitId])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await orderInvestigation({
        patientId,
        visitId,
        investigationTypeCode,
        notes: notes || null,
      })
      setNotes('')
      await loadOrders()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  const grouped = ['LAB', 'IMAGING', 'PROCEDURE'].map((category) => ({
    category,
    items: INVESTIGATION_TYPES.filter((t) => t.category === category),
  }))

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Order Investigation</h2>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Investigation type</span>
          <select
            className="input"
            value={investigationTypeCode}
            onChange={(e) => setInvestigationTypeCode(e.target.value)}
          >
            {grouped.map((g) => (
              <optgroup key={g.category} label={CATEGORY_LABELS[g.category]}>
                {g.items.map((t) => (
                  <option key={t.code} value={t.code}>
                    {t.name}
                  </option>
                ))}
              </optgroup>
            ))}
          </select>
        </label>

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
          {submitting ? 'Ordering...' : 'Order investigation'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Orders ({orders.length})</h2>
        {orders.length === 0 && <p className="text-sm text-slate-400">No investigations ordered for this visit yet.</p>}
        {orders.map((o) => (
          <div key={o.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">{o.investigationName}</p>
              <span
                className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                  o.status === 'COMPLETED' ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
                }`}
              >
                {o.status}
              </span>
            </div>
            <p className="mt-1 text-xs text-slate-400">
              {o.category} · ordered {o.orderedDate}
            </p>
            {o.notes && <p className="mt-1 text-sm text-slate-600">{o.notes}</p>}
            {o.latestReport && (
              <div className="mt-2 rounded-md bg-slate-50 p-2 text-sm">
                {o.latestReport.resultParameters.map((p) => (
                  <p key={p.parameterName} className={p.abnormal ? 'font-medium text-red-600' : 'text-slate-600'}>
                    {p.parameterName}: {p.value} {p.unit}
                    {p.abnormal && ' ⚠'}
                  </p>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
