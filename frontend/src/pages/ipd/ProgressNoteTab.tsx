import { useEffect, useState, type FormEvent } from 'react'
import {
  createProgressNote,
  getProgressNotesByAdmission,
  type ActivityLevel,
  type AppetiteLevel,
  type ProgressNoteResponse,
  type SeverityLevel,
} from '../../api/ipdCare'
import { extractErrorMessage } from '../../api/client'

const emptyForm = {
  chiefComplaintToday: '',
  painScore: '',
  nauseaVomiting: false,
  appetite: '' as AppetiteLevel | '',
  bowelMovementFrequency: '',
  bowelMovementCharacter: '',
  sleepPattern: '',
  generalWellBeing: '',
  generalAppearance: '',
  abdominalExaminationFindings: '',
  newFindings: '',
  clinicalImpression: '',
  currentDiagnosis: '',
  icd10Code: '',
  severityAssessment: '' as SeverityLevel | '',
  complicationFlags: '',
  investigationsOrdered: '',
  consultationsRequired: '',
  dietPlan: '',
  activityLevel: '' as ActivityLevel | '',
  dischargePlanningNotes: '',
}

export function ProgressNoteTab({ admissionId }: { admissionId: number }) {
  const [notes, setNotes] = useState<ProgressNoteResponse[]>([])
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof typeof emptyForm>(key: K, value: (typeof emptyForm)[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function loadNotes() {
    setNotes(await getProgressNotesByAdmission(admissionId))
  }

  useEffect(() => {
    loadNotes().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [admissionId])

  function toList(value: string): string[] {
    return value
      .split(',')
      .map((v) => v.trim())
      .filter(Boolean)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await createProgressNote({
        admissionId,
        chiefComplaintToday: form.chiefComplaintToday || null,
        painScore: form.painScore ? Number(form.painScore) : null,
        nauseaVomiting: form.nauseaVomiting,
        appetite: form.appetite || null,
        bowelMovementFrequency: form.bowelMovementFrequency || null,
        bowelMovementCharacter: form.bowelMovementCharacter || null,
        sleepPattern: form.sleepPattern || null,
        generalWellBeing: form.generalWellBeing || null,
        generalAppearance: form.generalAppearance || null,
        abdominalExaminationFindings: form.abdominalExaminationFindings || null,
        newFindings: form.newFindings || null,
        clinicalImpression: form.clinicalImpression || null,
        currentDiagnosis: form.currentDiagnosis || null,
        icd10Code: form.icd10Code || null,
        severityAssessment: form.severityAssessment || null,
        complicationFlags: toList(form.complicationFlags),
        investigationsOrdered: toList(form.investigationsOrdered),
        consultationsRequired: toList(form.consultationsRequired),
        dietPlan: form.dietPlan || null,
        activityLevel: form.activityLevel || null,
        dischargePlanningNotes: form.dischargePlanningNotes || null,
      })
      setForm(emptyForm)
      await loadNotes()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <form onSubmit={handleSubmit} className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Daily Progress Note (SOAP)</h2>

        <Section title="S — Subjective">
          <TextField label="Chief complaint today" value={form.chiefComplaintToday} onChange={(v) => update('chiefComplaintToday', v)} />
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={form.nauseaVomiting}
              onChange={(e) => update('nauseaVomiting', e.target.checked)}
            />
            Nausea / vomiting
          </label>
          <div className="grid grid-cols-2 gap-2">
            <TextField label="Pain score (0-10)" value={form.painScore} onChange={(v) => update('painScore', v)} type="number" />
            <label className="block text-sm">
              <span className="mb-1 block font-medium text-slate-700">Appetite</span>
              <select className="input" value={form.appetite} onChange={(e) => update('appetite', e.target.value as AppetiteLevel)}>
                <option value="">-</option>
                <option value="POOR">Poor</option>
                <option value="MODERATE">Moderate</option>
                <option value="GOOD">Good</option>
              </select>
            </label>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <TextField label="Bowel movement frequency" value={form.bowelMovementFrequency} onChange={(v) => update('bowelMovementFrequency', v)} />
            <TextField label="Bowel movement character" value={form.bowelMovementCharacter} onChange={(v) => update('bowelMovementCharacter', v)} />
          </div>
          <TextField label="Sleep pattern" value={form.sleepPattern} onChange={(v) => update('sleepPattern', v)} />
          <TextField label="General well-being" value={form.generalWellBeing} onChange={(v) => update('generalWellBeing', v)} />
        </Section>

        <Section title="O — Objective">
          <TextField label="General appearance" value={form.generalAppearance} onChange={(v) => update('generalAppearance', v)} />
          <TextField label="Abdominal examination findings" value={form.abdominalExaminationFindings} onChange={(v) => update('abdominalExaminationFindings', v)} />
          <TextField label="New findings" value={form.newFindings} onChange={(v) => update('newFindings', v)} />
        </Section>

        <Section title="A — Assessment">
          <TextField label="Clinical impression" value={form.clinicalImpression} onChange={(v) => update('clinicalImpression', v)} />
          <div className="grid grid-cols-2 gap-2">
            <TextField label="Current diagnosis" value={form.currentDiagnosis} onChange={(v) => update('currentDiagnosis', v)} />
            <TextField label="ICD-10 code" value={form.icd10Code} onChange={(v) => update('icd10Code', v)} />
          </div>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Severity assessment</span>
            <select className="input" value={form.severityAssessment} onChange={(e) => update('severityAssessment', e.target.value as SeverityLevel)}>
              <option value="">-</option>
              <option value="MILD">Mild</option>
              <option value="MODERATE">Moderate</option>
              <option value="SEVERE">Severe</option>
            </select>
          </label>
          <TextField label="Complication flags (comma separated)" value={form.complicationFlags} onChange={(v) => update('complicationFlags', v)} />
        </Section>

        <Section title="P — Plan">
          <TextField label="Investigations ordered (comma separated)" value={form.investigationsOrdered} onChange={(v) => update('investigationsOrdered', v)} />
          <TextField label="Consultations required (comma separated)" value={form.consultationsRequired} onChange={(v) => update('consultationsRequired', v)} />
          <TextField label="Diet plan" value={form.dietPlan} onChange={(v) => update('dietPlan', v)} />
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Activity level</span>
            <select className="input" value={form.activityLevel} onChange={(e) => update('activityLevel', e.target.value as ActivityLevel)}>
              <option value="">-</option>
              <option value="BED_REST">Bed rest</option>
              <option value="ASSISTED_AMBULATION">Assisted ambulation</option>
              <option value="INDEPENDENT_AMBULATION">Independent ambulation</option>
              <option value="NORMAL_ACTIVITY">Normal activity</option>
            </select>
          </label>
          <TextField label="Discharge planning notes" value={form.dischargePlanningNotes} onChange={(v) => update('dischargePlanningNotes', v)} />
        </Section>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Save progress note'}
        </button>
      </form>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Notes ({notes.length})</h2>
        {notes.length === 0 && <p className="text-sm text-slate-400">No progress notes for this admission yet.</p>}
        {notes.map((n) => (
          <div key={n.id} className="rounded-lg bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between">
              <p className="font-medium text-slate-800">{n.noteDate}</p>
              {n.severityAssessment && (
                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                  {n.severityAssessment}
                </span>
              )}
            </div>
            {n.chiefComplaintToday && <p className="mt-1 text-sm text-slate-600">{n.chiefComplaintToday}</p>}
            {n.clinicalImpression && <p className="mt-1 text-sm text-slate-500">Impression: {n.clinicalImpression}</p>}
          </div>
        ))}
      </div>
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2 rounded-md bg-slate-50 p-3">
      <p className="text-xs font-semibold text-slate-500">{title}</p>
      {children}
    </div>
  )
}

function TextField({
  label,
  value,
  onChange,
  type = 'text',
}: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <input type={type} className="input" value={value} onChange={(e) => onChange(e.target.value)} />
    </label>
  )
}
