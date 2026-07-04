import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ComplaintsTab } from './ComplaintsTab'
import { VitalsTab } from './VitalsTab'
import { DiagnosisTab } from './DiagnosisTab'
import { InvestigationsTab } from './InvestigationsTab'
import { PrescriptionTab } from './PrescriptionTab'
import { CdsTab } from './CdsTab'
import { ClinicalExaminationTab } from '../clinical/ClinicalExaminationTab'

type Tab = 'complaints' | 'vitals' | 'examination' | 'diagnosis' | 'investigations' | 'prescription' | 'cds'

const TABS: { key: Tab; label: string }[] = [
  { key: 'complaints', label: 'Chief Complaints' },
  { key: 'vitals', label: 'Vitals' },
  { key: 'examination', label: 'Clinical Examination' },
  { key: 'diagnosis', label: 'Diagnosis' },
  { key: 'investigations', label: 'Investigations' },
  { key: 'prescription', label: 'Prescription' },
  { key: 'cds', label: 'Clinical Decision Support' },
]

export function OpdEncounterPage() {
  const { upid, visitId } = useParams<{ upid: string; visitId: string }>()
  const [tab, setTab] = useState<Tab>('complaints')

  if (!upid || !visitId) return null
  const visitIdNumber = Number(visitId)

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">OPD Encounter</h1>
          <p className="text-sm text-slate-500">
            Patient <span className="font-medium">{upid}</span> · Visit #{visitId}
          </p>
        </div>
        <Link to={`/patients/${upid}`} className="text-sm text-indigo-600 hover:underline">
          Back to patient profile
        </Link>
      </div>

      <div className="flex flex-wrap gap-1 border-b border-slate-200">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`rounded-t-md px-4 py-2 text-sm font-medium ${
              tab === t.key
                ? 'border-b-2 border-indigo-600 text-indigo-700'
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'complaints' && <ComplaintsTab visitId={visitIdNumber} />}
      {tab === 'vitals' && <VitalsTab patientId={upid} visitId={visitIdNumber} />}
      {tab === 'examination' && <ClinicalExaminationTab context="OPD" patientId={upid} visitId={visitIdNumber} />}
      {tab === 'diagnosis' && <DiagnosisTab patientId={upid} />}
      {tab === 'investigations' && <InvestigationsTab patientId={upid} visitId={visitIdNumber} />}
      {tab === 'prescription' && <PrescriptionTab patientId={upid} visitId={visitIdNumber} />}
      {tab === 'cds' && <CdsTab patientId={upid} visitId={visitIdNumber} />}
    </div>
  )
}
