import { useEffect, useState } from 'react'
import {
  createIpdClinicalExamination,
  createOpdClinicalExamination,
  getIpdClinicalExaminations,
  getOpdClinicalExaminations,
  type AbdominalExaminationDto,
  type AscitesAssessmentDto,
  type BowelSounds,
  type ClinicalExaminationResponse,
  type DigitalRectalExaminationDto,
  type GiMassExaminationDto,
  type HerniaExaminationDto,
  type JaundiceAssessmentDto,
  type LymphNodeExaminationDto,
  type MassConsistency,
  type MassMobility,
  type PupillaryReflex,
  type SystemicExaminationDto,
} from '../../api/clinicalExamination'
import { extractErrorMessage } from '../../api/client'

const BOWEL_SOUNDS: BowelSounds[] = ['NORMAL', 'INCREASED', 'DECREASED', 'ABSENT']
const MASS_MOBILITY: MassMobility[] = ['MOBILE', 'FIXED']
const MASS_CONSISTENCY: MassConsistency[] = ['SOFT', 'FIRM', 'HARD']
const PUPILLARY_REFLEX: PupillaryReflex[] = ['NORMAL', 'SLUGGISH', 'FIXED']

function BoolField({
  label,
  value,
  onChange,
}: {
  label: string
  value: boolean | null | undefined
  onChange: (v: boolean | null) => void
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <select
        className="input"
        value={value === true ? 'true' : value === false ? 'false' : ''}
        onChange={(e) => onChange(e.target.value === '' ? null : e.target.value === 'true')}
      >
        <option value="">—</option>
        <option value="true">Yes</option>
        <option value="false">No</option>
      </select>
    </label>
  )
}

function TextField({
  label,
  value,
  onChange,
}: {
  label: string
  value: string | null | undefined
  onChange: (v: string | null) => void
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <input className="input" value={value ?? ''} onChange={(e) => onChange(e.target.value || null)} />
    </label>
  )
}

function NumberField({
  label,
  value,
  onChange,
}: {
  label: string
  value: number | null | undefined
  onChange: (v: number | null) => void
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <input
        type="number"
        step="any"
        className="input"
        value={value ?? ''}
        onChange={(e) => onChange(e.target.value === '' ? null : Number(e.target.value))}
      />
    </label>
  )
}

function SelectField<T extends string>({
  label,
  value,
  options,
  onChange,
}: {
  label: string
  value: T | null | undefined
  options: T[]
  onChange: (v: T | null) => void
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <select className="input" value={value ?? ''} onChange={(e) => onChange((e.target.value || null) as T | null)}>
        <option value="">—</option>
        {options.map((o) => (
          <option key={o} value={o}>
            {o}
          </option>
        ))}
      </select>
    </label>
  )
}

function splitCamel(key: string): string {
  const withSpaces = key.replace(/([A-Z])/g, ' $1')
  return withSpaces.charAt(0).toUpperCase() + withSpaces.slice(1)
}

function summarize(dto: Record<string, unknown> | null | undefined): string[] {
  if (!dto) return []
  return Object.entries(dto)
    .filter(([, v]) => v !== null && v !== undefined && v !== '')
    .map(([k, v]) => `${splitCamel(k)}: ${typeof v === 'boolean' ? (v ? 'Yes' : 'No') : String(v)}`)
}

function ExaminationSummaryCard({ exam }: { exam: ClinicalExaminationResponse }) {
  const sections = [
    { label: 'Abdominal', items: summarize(exam.abdominalExamination) },
    { label: 'Digital Rectal', items: summarize(exam.digitalRectalExamination) },
    { label: 'Jaundice', items: summarize(exam.jaundiceAssessment) },
    { label: 'Hernia', items: summarize(exam.herniaExamination) },
    { label: 'Lymph Node', items: summarize(exam.lymphNodeExamination) },
    { label: 'GI Mass', items: summarize(exam.giMassExamination) },
    { label: 'Ascites', items: summarize(exam.ascitesAssessment) },
    { label: 'Systemic', items: summarize(exam.systemicExamination) },
  ].filter((s) => s.items.length > 0)

  return (
    <div className="rounded-lg bg-white p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
          {exam.examinationContext}
        </span>
        <span className="text-xs text-slate-400">{new Date(exam.createdAt).toLocaleString()}</span>
      </div>
      {exam.abdominalGirthCm != null && (
        <p className="mt-2 text-sm text-slate-700">Abdominal girth: {exam.abdominalGirthCm} cm</p>
      )}
      {sections.length === 0 ? (
        <p className="mt-2 text-xs text-slate-400">No positive findings recorded.</p>
      ) : (
        <div className="mt-2 grid grid-cols-1 gap-3 sm:grid-cols-2">
          {sections.map((s) => (
            <div key={s.label}>
              <p className="text-xs font-semibold text-slate-500">{s.label}</p>
              <ul className="text-xs text-slate-600">
                {s.items.map((it) => (
                  <li key={it}>{it}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

interface Props {
  context: 'OPD' | 'IPD'
  patientId: string
  visitId?: number
  admissionId?: number
}

export function ClinicalExaminationTab({ context, patientId, visitId, admissionId }: Props) {
  const [exams, setExams] = useState<ClinicalExaminationResponse[]>([])
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const [abdominal, setAbdominal] = useState<AbdominalExaminationDto>({})
  const [dre, setDre] = useState<DigitalRectalExaminationDto>({})
  const [jaundice, setJaundice] = useState<JaundiceAssessmentDto>({})
  const [hernia, setHernia] = useState<HerniaExaminationDto>({})
  const [lymphNode, setLymphNode] = useState<LymphNodeExaminationDto>({})
  const [giMass, setGiMass] = useState<GiMassExaminationDto>({})
  const [ascites, setAscites] = useState<AscitesAssessmentDto>({})
  const [systemic, setSystemic] = useState<SystemicExaminationDto>({})
  const [abdominalGirthCm, setAbdominalGirthCm] = useState<number | null>(null)

  async function load() {
    try {
      const result =
        context === 'OPD' ? await getOpdClinicalExaminations(visitId!) : await getIpdClinicalExaminations(admissionId!)
      setExams(result)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [context, visitId, admissionId])

  function resetForm() {
    setAbdominal({})
    setDre({})
    setJaundice({})
    setHernia({})
    setLymphNode({})
    setGiMass({})
    setAscites({})
    setSystemic({})
    setAbdominalGirthCm(null)
  }

  async function handleSubmit() {
    setError(null)
    setSubmitting(true)
    try {
      const request = {
        patientId,
        visitId: context === 'OPD' ? visitId : null,
        admissionId: context === 'IPD' ? admissionId : null,
        abdominalExamination: abdominal,
        digitalRectalExamination: dre,
        jaundiceAssessment: jaundice,
        herniaExamination: hernia,
        lymphNodeExamination: lymphNode,
        giMassExamination: giMass,
        ascitesAssessment: ascites,
        systemicExamination: context === 'IPD' ? systemic : null,
        abdominalGirthCm: context === 'IPD' ? abdominalGirthCm : null,
      }
      if (context === 'OPD') {
        await createOpdClinicalExamination(request)
      } else {
        await createIpdClinicalExamination(request)
      }
      resetForm()
      await load()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="space-y-6 rounded-lg bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Record Clinical Examination</h2>

        <section className="space-y-3">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Abdominal Examination</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Scars present" value={abdominal.scarsPresent} onChange={(v) => setAbdominal((s) => ({ ...s, scarsPresent: v }))} />
            <BoolField label="Distension present" value={abdominal.distensionPresent} onChange={(v) => setAbdominal((s) => ({ ...s, distensionPresent: v }))} />
            <BoolField label="Visible peristalsis" value={abdominal.visiblePeristalsis} onChange={(v) => setAbdominal((s) => ({ ...s, visiblePeristalsis: v }))} />
            <BoolField label="Tenderness" value={abdominal.tenderness} onChange={(v) => setAbdominal((s) => ({ ...s, tenderness: v }))} />
            <TextField label="Tenderness site" value={abdominal.tendernessSite} onChange={(v) => setAbdominal((s) => ({ ...s, tendernessSite: v }))} />
            <BoolField label="Guarding" value={abdominal.guarding} onChange={(v) => setAbdominal((s) => ({ ...s, guarding: v }))} />
            <BoolField label="Rigidity" value={abdominal.rigidity} onChange={(v) => setAbdominal((s) => ({ ...s, rigidity: v }))} />
            <TextField label="Organomegaly" value={abdominal.organomegaly} onChange={(v) => setAbdominal((s) => ({ ...s, organomegaly: v }))} />
            <BoolField label="Percussion dullness" value={abdominal.percussionDullness} onChange={(v) => setAbdominal((s) => ({ ...s, percussionDullness: v }))} />
            <BoolField label="Tympanic" value={abdominal.tympanic} onChange={(v) => setAbdominal((s) => ({ ...s, tympanic: v }))} />
            <SelectField label="Bowel sounds" value={abdominal.bowelSounds} options={BOWEL_SOUNDS} onChange={(v) => setAbdominal((s) => ({ ...s, bowelSounds: v }))} />
          </div>
          <TextField label="Notes" value={abdominal.notes} onChange={(v) => setAbdominal((s) => ({ ...s, notes: v }))} />
        </section>

        <section className="space-y-3 border-t border-slate-100 pt-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Digital Rectal Examination</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Fissures" value={dre.fissures} onChange={(v) => setDre((s) => ({ ...s, fissures: v }))} />
            <BoolField label="Fistula" value={dre.fistula} onChange={(v) => setDre((s) => ({ ...s, fistula: v }))} />
            <BoolField label="External piles" value={dre.externalPiles} onChange={(v) => setDre((s) => ({ ...s, externalPiles: v }))} />
            <TextField label="Sphincter tone" value={dre.sphincterTone} onChange={(v) => setDre((s) => ({ ...s, sphincterTone: v }))} />
            <BoolField label="Mass present" value={dre.massPresent} onChange={(v) => setDre((s) => ({ ...s, massPresent: v }))} />
            <TextField label="Mass description" value={dre.massDescription} onChange={(v) => setDre((s) => ({ ...s, massDescription: v }))} />
            <BoolField label="Blood on finger" value={dre.bloodOnFinger} onChange={(v) => setDre((s) => ({ ...s, bloodOnFinger: v }))} />
            <BoolField label="Proctoscopy performed" value={dre.proctoscopyPerformed} onChange={(v) => setDre((s) => ({ ...s, proctoscopyPerformed: v }))} />
            <TextField label="Proctoscopy findings" value={dre.proctoscopyFindings} onChange={(v) => setDre((s) => ({ ...s, proctoscopyFindings: v }))} />
          </div>
        </section>

        <section className="space-y-3 border-t border-slate-100 pt-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Jaundice Assessment</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Icterus (sclera)" value={jaundice.icterusSclera} onChange={(v) => setJaundice((s) => ({ ...s, icterusSclera: v }))} />
            <BoolField label="Icterus (skin)" value={jaundice.icterusSkin} onChange={(v) => setJaundice((s) => ({ ...s, icterusSkin: v }))} />
            <BoolField label="Icterus (palmar)" value={jaundice.icterusPalmar} onChange={(v) => setJaundice((s) => ({ ...s, icterusPalmar: v }))} />
            <BoolField label="Scratch marks present" value={jaundice.scratchMarksPresent} onChange={(v) => setJaundice((s) => ({ ...s, scratchMarksPresent: v }))} />
          </div>
        </section>

        <section className="space-y-3 border-t border-slate-100 pt-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Hernia Examination</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Hernia present" value={hernia.herniaPresent} onChange={(v) => setHernia((s) => ({ ...s, herniaPresent: v }))} />
            <TextField label="Site" value={hernia.site} onChange={(v) => setHernia((s) => ({ ...s, site: v }))} />
            <BoolField label="Reducible" value={hernia.reducible} onChange={(v) => setHernia((s) => ({ ...s, reducible: v }))} />
            <BoolField label="Cough impulse" value={hernia.coughImpulse} onChange={(v) => setHernia((s) => ({ ...s, coughImpulse: v }))} />
          </div>
        </section>

        <section className="space-y-3 border-t border-slate-100 pt-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Lymph Node Examination</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Cervical nodes palpable" value={lymphNode.cervicalNodesPalpable} onChange={(v) => setLymphNode((s) => ({ ...s, cervicalNodesPalpable: v }))} />
            <BoolField label="Supraclavicular nodes palpable" value={lymphNode.supraclavicularNodesPalpable} onChange={(v) => setLymphNode((s) => ({ ...s, supraclavicularNodesPalpable: v }))} />
            <BoolField label="Inguinal nodes palpable" value={lymphNode.inguinalNodesPalpable} onChange={(v) => setLymphNode((s) => ({ ...s, inguinalNodesPalpable: v }))} />
          </div>
          <TextField label="Notes" value={lymphNode.notes} onChange={(v) => setLymphNode((s) => ({ ...s, notes: v }))} />
        </section>

        <section className="space-y-3 border-t border-slate-100 pt-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">GI Mass Examination</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Mass present" value={giMass.massPresent} onChange={(v) => setGiMass((s) => ({ ...s, massPresent: v }))} />
            <TextField label="Location" value={giMass.location} onChange={(v) => setGiMass((s) => ({ ...s, location: v }))} />
            <NumberField label="Size (cm)" value={giMass.sizeCm} onChange={(v) => setGiMass((s) => ({ ...s, sizeCm: v }))} />
            <SelectField label="Mobility" value={giMass.mobility} options={MASS_MOBILITY} onChange={(v) => setGiMass((s) => ({ ...s, mobility: v }))} />
            <SelectField label="Consistency" value={giMass.consistency} options={MASS_CONSISTENCY} onChange={(v) => setGiMass((s) => ({ ...s, consistency: v }))} />
          </div>
        </section>

        <section className="space-y-3 border-t border-slate-100 pt-4">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Ascites Assessment</h3>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
            <BoolField label="Shifting dullness present" value={ascites.shiftingDullnessPresent} onChange={(v) => setAscites((s) => ({ ...s, shiftingDullnessPresent: v }))} />
            <BoolField label="Fluid thrill present" value={ascites.fluidThrillPresent} onChange={(v) => setAscites((s) => ({ ...s, fluidThrillPresent: v }))} />
          </div>
          <TextField label="Notes" value={ascites.notes} onChange={(v) => setAscites((s) => ({ ...s, notes: v }))} />
        </section>

        {context === 'IPD' && (
          <section className="space-y-3 border-t border-slate-100 pt-4">
            <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-400">Systemic Examination (IPD)</h3>
            <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
              <TextField label="Chest expansion" value={systemic.chestExpansion} onChange={(v) => setSystemic((s) => ({ ...s, chestExpansion: v }))} />
              <TextField label="Breath sounds" value={systemic.breathSounds} onChange={(v) => setSystemic((s) => ({ ...s, breathSounds: v }))} />
              <TextField label="Heart sounds" value={systemic.heartSounds} onChange={(v) => setSystemic((s) => ({ ...s, heartSounds: v }))} />
              <BoolField label="Murmurs present" value={systemic.murmursPresent} onChange={(v) => setSystemic((s) => ({ ...s, murmursPresent: v }))} />
              <TextField label="Murmur description" value={systemic.murmurDescription} onChange={(v) => setSystemic((s) => ({ ...s, murmurDescription: v }))} />
              <TextField label="JVP" value={systemic.jvp} onChange={(v) => setSystemic((s) => ({ ...s, jvp: v }))} />
              <NumberField label="GCS score" value={systemic.gcsScore} onChange={(v) => setSystemic((s) => ({ ...s, gcsScore: v }))} />
              <SelectField label="Pupillary reflex" value={systemic.pupillaryReflex} options={PUPILLARY_REFLEX} onChange={(v) => setSystemic((s) => ({ ...s, pupillaryReflex: v }))} />
              <TextField label="Motor findings" value={systemic.motorFindings} onChange={(v) => setSystemic((s) => ({ ...s, motorFindings: v }))} />
              <TextField label="Sensory findings" value={systemic.sensoryFindings} onChange={(v) => setSystemic((s) => ({ ...s, sensoryFindings: v }))} />
            </div>
            <div className="max-w-xs">
              <NumberField label="Abdominal girth (cm)" value={abdominalGirthCm} onChange={setAbdominalGirthCm} />
            </div>
          </section>
        )}

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          onClick={handleSubmit}
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Saving...' : 'Record examination'}
        </button>
      </div>

      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-600">Recorded Examinations ({exams.length})</h2>
        {exams.length === 0 && <p className="text-sm text-slate-400">No examinations recorded yet.</p>}
        {exams.map((exam) => (
          <ExaminationSummaryCard key={exam.id} exam={exam} />
        ))}
      </div>
    </div>
  )
}
