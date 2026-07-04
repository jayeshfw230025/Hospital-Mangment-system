import { useEffect, useState, type FormEvent } from 'react'
import {
  createPrescription,
  getPrescriptionsByPatient,
  searchDrugs,
  type DrugResponse,
  type FoodInstruction,
  type PrescriptionItemRequest,
  type PrescriptionResponse,
} from '../../api/prescriptions'
import { extractErrorMessage } from '../../api/client'

interface PendingItem extends PrescriptionItemRequest {
  drugLabel: string
}

export function PrescriptionTab({ patientId, visitId }: { patientId: string; visitId: number }) {
  const [prescriptions, setPrescriptions] = useState<PrescriptionResponse[]>([])
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<DrugResponse[]>([])
  const [selectedDrug, setSelectedDrug] = useState<DrugResponse | null>(null)
  const [dosage, setDosage] = useState('')
  const [frequency, setFrequency] = useState('')
  const [route, setRoute] = useState('')
  const [durationDays, setDurationDays] = useState('')
  const [foodInstruction, setFoodInstruction] = useState<FoodInstruction | ''>('')
  const [items, setItems] = useState<PendingItem[]>([])
  const [doctorName, setDoctorName] = useState('')
  const [digitalSignature, setDigitalSignature] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [lastResult, setLastResult] = useState<PrescriptionResponse | null>(null)

  async function loadPrescriptions() {
    const result = await getPrescriptionsByPatient(patientId)
    setPrescriptions(result)
  }

  useEffect(() => {
    loadPrescriptions().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [patientId])

  async function handleDrugSearch(value: string) {
    setQuery(value)
    if (value.trim().length < 2) {
      setResults([])
      return
    }
    try {
      setResults(await searchDrugs(value))
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  function addItem() {
    if (!selectedDrug || !dosage || !frequency) return
    setItems((prev) => [
      ...prev,
      {
        drugId: selectedDrug.id,
        drugLabel: `${selectedDrug.genericName}${selectedDrug.brandName ? ` (${selectedDrug.brandName})` : ''}`,
        dosage,
        frequency,
        route: route || null,
        durationDays: durationDays ? Number(durationDays) : null,
        foodInstruction: foodInstruction || null,
      },
    ])
    setSelectedDrug(null)
    setQuery('')
    setResults([])
    setDosage('')
    setFrequency('')
    setRoute('')
    setDurationDays('')
    setFoodInstruction('')
  }

  function removeItem(index: number) {
    setItems((prev) => prev.filter((_, i) => i !== index))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (items.length === 0) return
    setError(null)
    setSubmitting(true)
    try {
      const created = await createPrescription({
        patientId,
        visitId,
        doctorName,
        digitalSignature,
        items: items.map(({ drugLabel: _drugLabel, ...rest }) => rest),
      })
      setLastResult(created)
      setItems([])
      await loadPrescriptions()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">New Prescription</h2>

        <div className="grid grid-cols-2 gap-3">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Doctor name</span>
            <input className="input" value={doctorName} onChange={(e) => setDoctorName(e.target.value)} required />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Digital signature</span>
            <input
              className="input"
              value={digitalSignature}
              onChange={(e) => setDigitalSignature(e.target.value)}
              required
            />
          </label>
        </div>

        <div className="space-y-3 rounded-md bg-slate-50 p-3">
          <p className="text-xs font-medium text-slate-500">Add drug</p>
          {selectedDrug ? (
            <div className="flex items-center justify-between rounded-md bg-indigo-50 px-3 py-2 text-sm">
              <span>
                {selectedDrug.genericName} {selectedDrug.brandName && `(${selectedDrug.brandName})`}
              </span>
              <button type="button" className="text-xs text-indigo-600 hover:underline" onClick={() => setSelectedDrug(null)}>
                Change
              </button>
            </div>
          ) : (
            <label className="block text-sm">
              <input
                className="input"
                placeholder="Search drug by generic/brand name"
                value={query}
                onChange={(e) => handleDrugSearch(e.target.value)}
              />
              {results.length > 0 && (
                <ul className="mt-1 max-h-40 overflow-y-auto rounded-md border border-slate-200 bg-white shadow-sm">
                  {results.map((d) => (
                    <li key={d.id}>
                      <button
                        type="button"
                        className="block w-full px-3 py-2 text-left text-sm hover:bg-slate-50"
                        onClick={() => {
                          setSelectedDrug(d)
                          setResults([])
                        }}
                      >
                        {d.genericName} {d.brandName && `(${d.brandName})`}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </label>
          )}

          <div className="grid grid-cols-2 gap-2">
            <input className="input" placeholder="Dosage e.g. 20mg" value={dosage} onChange={(e) => setDosage(e.target.value)} />
            <input
              className="input"
              placeholder="Frequency e.g. Twice daily"
              value={frequency}
              onChange={(e) => setFrequency(e.target.value)}
            />
            <input className="input" placeholder="Route e.g. Oral" value={route} onChange={(e) => setRoute(e.target.value)} />
            <input
              className="input"
              type="number"
              placeholder="Duration (days)"
              value={durationDays}
              onChange={(e) => setDurationDays(e.target.value)}
            />
            <select
              className="input col-span-2"
              value={foodInstruction}
              onChange={(e) => setFoodInstruction(e.target.value as FoodInstruction)}
            >
              <option value="">Food instruction</option>
              <option value="BEFORE_FOOD">Before food</option>
              <option value="AFTER_FOOD">After food</option>
              <option value="WITH_FOOD">With food</option>
              <option value="EMPTY_STOMACH">Empty stomach</option>
              <option value="ANYTIME">Anytime</option>
            </select>
          </div>
          <button
            type="button"
            onClick={addItem}
            disabled={!selectedDrug || !dosage || !frequency}
            className="rounded-md border border-indigo-300 px-3 py-1.5 text-sm text-indigo-700 hover:bg-indigo-50 disabled:opacity-40"
          >
            Add to prescription
          </button>
        </div>

        {items.length > 0 && (
          <ul className="space-y-1">
            {items.map((item, i) => (
              <li key={i} className="flex items-center justify-between rounded-md bg-white px-3 py-2 text-sm shadow-sm">
                <span>
                  {item.drugLabel} — {item.dosage}, {item.frequency}
                </span>
                <button type="button" className="text-xs text-red-500 hover:underline" onClick={() => removeItem(i)}>
                  Remove
                </button>
              </li>
            ))}
          </ul>
        )}

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting || items.length === 0 || !doctorName || !digitalSignature}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Creating...' : 'Create prescription'}
        </button>

        {lastResult && (
          <div className="space-y-2 rounded-md border border-slate-200 p-3">
            <p className="text-xs font-semibold text-slate-500">Prescription #{lastResult.id} created</p>
            {lastResult.interactionWarnings.length > 0 && (
              <div className="space-y-1">
                {lastResult.interactionWarnings.map((w, i) => (
                  <p key={i} className="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700">
                    ⚠ {w.drugA} + {w.drugB}: {w.description}
                  </p>
                ))}
              </div>
            )}
            {lastResult.nutritionAlerts.length > 0 && (
              <div className="space-y-1">
                {lastResult.nutritionAlerts.map((a, i) => (
                  <p key={i} className="rounded-md bg-amber-50 px-2 py-1 text-xs font-medium text-amber-700">
                    ⚠ {a.drugName}: {a.alert}
                  </p>
                ))}
              </div>
            )}
          </div>
        )}
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Prescription history ({prescriptions.length})</h2>
        {prescriptions.length === 0 && <p className="text-sm text-slate-400">No prescriptions for this patient yet.</p>}
        {prescriptions.map((p) => (
          <div key={p.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">Dr. {p.doctorName}</p>
              <span className="text-xs text-slate-400">{p.prescribedDate}</span>
            </div>
            <ul className="mt-2 space-y-1">
              {p.items.map((item, i) => (
                <li key={i} className="text-sm text-slate-600">
                  {item.genericName} — {item.generatedInstructions}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  )
}
