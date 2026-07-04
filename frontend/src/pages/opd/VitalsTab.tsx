import { useEffect, useState, type FormEvent } from 'react'
import { getOpdVitalsByVisit, recordOpdVitals, type OpdVitalsResponse } from '../../api/opd'
import { extractErrorMessage } from '../../api/client'

const emptyForm = {
  systolicBp: '',
  diastolicBp: '',
  heartRate: '',
  respiratoryRate: '',
  temperature: '',
  heightCm: '',
  weightKg: '',
  spo2: '',
  painScore: '',
  randomBloodSugar: '',
}

export function VitalsTab({ patientId, visitId }: { patientId: string; visitId: number }) {
  const [readings, setReadings] = useState<OpdVitalsResponse[]>([])
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function loadReadings() {
    const result = await getOpdVitalsByVisit(visitId)
    setReadings(result)
  }

  useEffect(() => {
    loadReadings().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visitId])

  function update(key: keyof typeof emptyForm, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  function toNumber(value: string): number | null {
    return value === '' ? null : Number(value)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await recordOpdVitals({
        visitId,
        patientId,
        systolicBp: toNumber(form.systolicBp),
        diastolicBp: toNumber(form.diastolicBp),
        heartRate: toNumber(form.heartRate),
        respiratoryRate: toNumber(form.respiratoryRate),
        temperature: toNumber(form.temperature),
        temperatureUnit: form.temperature ? 'CELSIUS' : null,
        heightCm: toNumber(form.heightCm),
        weightKg: toNumber(form.weightKg),
        spo2: toNumber(form.spo2),
        painScore: toNumber(form.painScore),
        randomBloodSugar: toNumber(form.randomBloodSugar),
      })
      setForm(emptyForm)
      await loadReadings()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Record Vitals</h2>
        <div className="grid grid-cols-2 gap-3">
          <NumberField label="Systolic BP (mmHg)" value={form.systolicBp} onChange={(v) => update('systolicBp', v)} />
          <NumberField label="Diastolic BP (mmHg)" value={form.diastolicBp} onChange={(v) => update('diastolicBp', v)} />
          <NumberField label="Heart rate (bpm)" value={form.heartRate} onChange={(v) => update('heartRate', v)} />
          <NumberField
            label="Respiratory rate"
            value={form.respiratoryRate}
            onChange={(v) => update('respiratoryRate', v)}
          />
          <NumberField label="Temperature (°C)" value={form.temperature} onChange={(v) => update('temperature', v)} />
          <NumberField label="SpO2 (%)" value={form.spo2} onChange={(v) => update('spo2', v)} />
          <NumberField label="Height (cm)" value={form.heightCm} onChange={(v) => update('heightCm', v)} />
          <NumberField label="Weight (kg)" value={form.weightKg} onChange={(v) => update('weightKg', v)} />
          <NumberField label="Pain score (0-10)" value={form.painScore} onChange={(v) => update('painScore', v)} />
          <NumberField
            label="Random blood sugar"
            value={form.randomBloodSugar}
            onChange={(v) => update('randomBloodSugar', v)}
          />
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Record vitals'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Readings ({readings.length})</h2>
        {readings.length === 0 && <p className="text-sm text-slate-400">No vitals recorded for this visit yet.</p>}
        {readings.map((r) => (
          <div key={r.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-sm text-slate-600 sm:grid-cols-3">
              {r.systolicBp && <span>BP: {r.systolicBp}/{r.diastolicBp}</span>}
              {r.heartRate && <span>HR: {r.heartRate} bpm</span>}
              {r.temperatureCelsius && <span>Temp: {r.temperatureCelsius}°C</span>}
              {r.spo2 && <span>SpO2: {r.spo2}%</span>}
              {r.bmi && <span>BMI: {r.bmi.toFixed(1)}</span>}
              {r.painScore != null && <span>Pain: {r.painScore}/10</span>}
            </div>
            {r.triggeredAlerts.length > 0 && (
              <div className="mt-2 space-y-1">
                {r.triggeredAlerts.map((alert) => (
                  <p key={alert.id} className="rounded-md bg-red-50 px-2 py-1 text-xs font-medium text-red-700">
                    ⚠ {alert.message}
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

function NumberField({
  label,
  value,
  onChange,
}: {
  label: string
  value: string
  onChange: (value: string) => void
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <input type="number" className="input" value={value} onChange={(e) => onChange(e.target.value)} />
    </label>
  )
}
