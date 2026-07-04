import { useEffect, useState } from 'react'
import {
  NO_ADDITIONAL_FINDINGS,
  assess,
  calculateBisap,
  calculateCdai,
  calculateCtp,
  calculateMayo,
  calculateMeld,
  getAlertsByPatient,
  type AdditionalFindings,
  type AscitesGrade,
  type CdsAlertResponse,
  type EncephalopathyGrade,
} from '../../api/cds'
import { extractErrorMessage } from '../../api/client'

type CalculatorKey = 'CTP' | 'MELD' | 'MAYO' | 'BISAP' | 'CDAI'

export function CdsTab({ patientId, visitId }: { patientId: string; visitId: number }) {
  const [alerts, setAlerts] = useState<CdsAlertResponse[]>([])
  const [findings, setFindings] = useState<AdditionalFindings>(NO_ADDITIONAL_FINDINGS)
  const [assessing, setAssessing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [calculator, setCalculator] = useState<CalculatorKey>('CTP')

  async function loadAlerts() {
    setAlerts(await getAlertsByPatient(patientId))
  }

  useEffect(() => {
    loadAlerts().catch((err) => setError(extractErrorMessage(err)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [patientId])

  async function handleAssess() {
    setError(null)
    setAssessing(true)
    try {
      await assess({ patientId, visitId, additionalFindings: findings })
      await loadAlerts()
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setAssessing(false)
    }
  }

  const findingLabels: { key: keyof AdditionalFindings; label: string }[] = [
    { key: 'darkUrine', label: 'Dark urine' },
    { key: 'dehydration', label: 'Dehydration' },
    { key: 'obstructionSymptoms', label: 'Obstruction symptoms' },
    { key: 'spiderNevi', label: 'Spider nevi' },
    { key: 'hepaticEncephalopathySigns', label: 'Hepatic encephalopathy signs' },
  ]

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="space-y-4 rounded-lg bg-white p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-600">Run CDS Assessment</h2>
          <p className="text-xs text-slate-400">
            Aggregates vitals, history, diagnoses and investigations already on file; the findings below have no
            other data source and must be supplied by the assessing clinician.
          </p>
          <div className="grid grid-cols-2 gap-2">
            {findingLabels.map((f) => (
              <label key={f.key} className="flex items-center gap-2 text-sm text-slate-700">
                <input
                  type="checkbox"
                  checked={findings[f.key]}
                  onChange={(e) => setFindings((prev) => ({ ...prev, [f.key]: e.target.checked }))}
                />
                {f.label}
              </label>
            ))}
          </div>
          {error && <p className="text-sm text-red-600">{error}</p>}
          <button
            onClick={handleAssess}
            disabled={assessing}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
          >
            {assessing ? 'Assessing...' : 'Run assessment'}
          </button>
        </div>

        <div className="space-y-3">
          <h2 className="text-sm font-semibold text-slate-600">Alerts ({alerts.length})</h2>
          {alerts.length === 0 && <p className="text-sm text-slate-400">No CDS alerts for this patient.</p>}
          {alerts.map((a) => (
            <div key={a.id} className="rounded-lg border border-amber-200 bg-amber-50 p-4">
              <p className="text-sm font-medium text-amber-800">{a.ruleName}</p>
              <p className="mt-1 text-sm text-amber-700">{a.finding}</p>
              <p className="mt-1 text-xs text-amber-600">Suggestion: {a.suggestion}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="rounded-lg bg-white p-5 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-600">Score Calculators</h2>
          <select className="input w-56" value={calculator} onChange={(e) => setCalculator(e.target.value as CalculatorKey)}>
            <option value="CTP">Child-Turcotte-Pugh (CTP)</option>
            <option value="MELD">MELD / MELD-Na</option>
            <option value="MAYO">Mayo Score (Ulcerative Colitis)</option>
            <option value="BISAP">BISAP (Pancreatitis)</option>
            <option value="CDAI">CDAI (Crohn's Disease)</option>
          </select>
        </div>
        {calculator === 'CTP' && <CtpCalculator />}
        {calculator === 'MELD' && <MeldCalculator />}
        {calculator === 'MAYO' && <MayoCalculator />}
        {calculator === 'BISAP' && <BisapCalculator />}
        {calculator === 'CDAI' && <CdaiCalculator />}
      </div>
    </div>
  )
}

function ResultBanner({ children }: { children: React.ReactNode }) {
  return <div className="mt-3 rounded-md bg-indigo-50 px-3 py-2 text-sm text-indigo-800">{children}</div>
}

function CtpCalculator() {
  const [ascites, setAscites] = useState<AscitesGrade>('NONE')
  const [encephalopathy, setEncephalopathy] = useState<EncephalopathyGrade>('NONE')
  const [bilirubin, setBilirubin] = useState('')
  const [albumin, setAlbumin] = useState('')
  const [inr, setInr] = useState('')
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleCalculate() {
    setError(null)
    try {
      const r = await calculateCtp({
        ascites,
        encephalopathy,
        bilirubinMgDl: Number(bilirubin),
        albuminGDl: Number(albumin),
        inr: Number(inr),
      })
      setResult(`Score ${r.totalScore} — Class ${r.ctpClass}. ${r.interpretation}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      <select className="input" value={ascites} onChange={(e) => setAscites(e.target.value as AscitesGrade)}>
        <option value="NONE">Ascites: None</option>
        <option value="MILD">Ascites: Mild</option>
        <option value="MODERATE_SEVERE">Ascites: Moderate/Severe</option>
      </select>
      <select
        className="input"
        value={encephalopathy}
        onChange={(e) => setEncephalopathy(e.target.value as EncephalopathyGrade)}
      >
        <option value="NONE">Encephalopathy: None</option>
        <option value="GRADE_1_2">Encephalopathy: Grade 1-2</option>
        <option value="GRADE_3_4">Encephalopathy: Grade 3-4</option>
      </select>
      <input className="input" placeholder="Bilirubin (mg/dL)" value={bilirubin} onChange={(e) => setBilirubin(e.target.value)} />
      <input className="input" placeholder="Albumin (g/dL)" value={albumin} onChange={(e) => setAlbumin(e.target.value)} />
      <input className="input" placeholder="INR" value={inr} onChange={(e) => setInr(e.target.value)} />
      <button
        onClick={handleCalculate}
        className="rounded-md border border-indigo-300 px-3 py-1.5 text-sm text-indigo-700 hover:bg-indigo-50"
      >
        Calculate
      </button>
      {error && <p className="col-span-full text-sm text-red-600">{error}</p>}
      {result && <div className="col-span-full"><ResultBanner>{result}</ResultBanner></div>}
    </div>
  )
}

function MeldCalculator() {
  const [bilirubin, setBilirubin] = useState('')
  const [inr, setInr] = useState('')
  const [creatinine, setCreatinine] = useState('')
  const [sodium, setSodium] = useState('')
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleCalculate() {
    setError(null)
    try {
      const r = await calculateMeld({
        bilirubinMgDl: Number(bilirubin),
        inr: Number(inr),
        creatinineMgDl: Number(creatinine),
        sodiumMeqL: Number(sodium),
      })
      setResult(`MELD ${r.meldScore} · MELD-Na ${r.meldNaScore}. ${r.interpretation}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
      <input className="input" placeholder="Bilirubin (mg/dL)" value={bilirubin} onChange={(e) => setBilirubin(e.target.value)} />
      <input className="input" placeholder="INR" value={inr} onChange={(e) => setInr(e.target.value)} />
      <input className="input" placeholder="Creatinine (mg/dL)" value={creatinine} onChange={(e) => setCreatinine(e.target.value)} />
      <input className="input" placeholder="Sodium (mEq/L)" value={sodium} onChange={(e) => setSodium(e.target.value)} />
      <button
        onClick={handleCalculate}
        className="rounded-md border border-indigo-300 px-3 py-1.5 text-sm text-indigo-700 hover:bg-indigo-50"
      >
        Calculate
      </button>
      {error && <p className="col-span-full text-sm text-red-600">{error}</p>}
      {result && <div className="col-span-full"><ResultBanner>{result}</ResultBanner></div>}
    </div>
  )
}

function MayoCalculator() {
  const [stool, setStool] = useState('0')
  const [bleeding, setBleeding] = useState('0')
  const [endoscopy, setEndoscopy] = useState('0')
  const [physician, setPhysician] = useState('0')
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleCalculate() {
    setError(null)
    try {
      const r = await calculateMayo({
        stoolFrequencySubscore: Number(stool),
        rectalBleedingSubscore: Number(bleeding),
        endoscopySubscore: Number(endoscopy),
        physicianGlobalAssessmentSubscore: Number(physician),
      })
      setResult(`Score ${r.totalScore} — ${r.diseaseActivity}. ${r.interpretation}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  const subscoreSelect = (label: string, value: string, setValue: (v: string) => void) => (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label} (0-3)</span>
      <select className="input" value={value} onChange={(e) => setValue(e.target.value)}>
        <option value="0">0</option>
        <option value="1">1</option>
        <option value="2">2</option>
        <option value="3">3</option>
      </select>
    </label>
  )

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
      {subscoreSelect('Stool frequency', stool, setStool)}
      {subscoreSelect('Rectal bleeding', bleeding, setBleeding)}
      {subscoreSelect('Endoscopy', endoscopy, setEndoscopy)}
      {subscoreSelect('Physician global', physician, setPhysician)}
      <button
        onClick={handleCalculate}
        className="col-span-2 rounded-md border border-indigo-300 px-3 py-1.5 text-sm text-indigo-700 hover:bg-indigo-50 sm:col-span-1"
      >
        Calculate
      </button>
      {error && <p className="col-span-full text-sm text-red-600">{error}</p>}
      {result && <div className="col-span-full"><ResultBanner>{result}</ResultBanner></div>}
    </div>
  )
}

function BisapCalculator() {
  const [values, setValues] = useState({
    bunOver25: false,
    impairedMentalStatus: false,
    sirsPresent: false,
    ageOver60: false,
    pleuralEffusion: false,
  })
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleCalculate() {
    setError(null)
    try {
      const r = await calculateBisap(values)
      setResult(`Score ${r.totalScore} — ${r.mortalityRiskCategory}. ${r.interpretation}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  const labels: { key: keyof typeof values; label: string }[] = [
    { key: 'bunOver25', label: 'BUN > 25 mg/dL' },
    { key: 'impairedMentalStatus', label: 'Impaired mental status' },
    { key: 'sirsPresent', label: 'SIRS present' },
    { key: 'ageOver60', label: 'Age > 60' },
    { key: 'pleuralEffusion', label: 'Pleural effusion' },
  ]

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
        {labels.map((l) => (
          <label key={l.key} className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={values[l.key]}
              onChange={(e) => setValues((prev) => ({ ...prev, [l.key]: e.target.checked }))}
            />
            {l.label}
          </label>
        ))}
      </div>
      <button
        onClick={handleCalculate}
        className="rounded-md border border-indigo-300 px-3 py-1.5 text-sm text-indigo-700 hover:bg-indigo-50"
      >
        Calculate
      </button>
      {error && <p className="text-sm text-red-600">{error}</p>}
      {result && <ResultBanner>{result}</ResultBanner>}
    </div>
  )
}

function CdaiCalculator() {
  const [stool, setStool] = useState('')
  const [pain, setPain] = useState('')
  const [wellbeing, setWellbeing] = useState('')
  const [extra, setExtra] = useState('')
  const [result, setResult] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleCalculate() {
    setError(null)
    try {
      const r = await calculateCdai({
        stoolFrequencySum: Number(stool),
        abdominalPainSum: Number(pain),
        wellBeingSum: Number(wellbeing),
        extraintestinalManifestationsCount: Number(extra),
      })
      setResult(`Score ${r.totalScore} — ${r.diseaseActivity}. ${r.interpretation}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
      <input className="input" placeholder="Stool frequency sum" value={stool} onChange={(e) => setStool(e.target.value)} />
      <input className="input" placeholder="Abdominal pain sum" value={pain} onChange={(e) => setPain(e.target.value)} />
      <input className="input" placeholder="Well-being sum" value={wellbeing} onChange={(e) => setWellbeing(e.target.value)} />
      <input
        className="input"
        placeholder="Extraintestinal count"
        value={extra}
        onChange={(e) => setExtra(e.target.value)}
      />
      <button
        onClick={handleCalculate}
        className="rounded-md border border-indigo-300 px-3 py-1.5 text-sm text-indigo-700 hover:bg-indigo-50"
      >
        Calculate
      </button>
      {error && <p className="col-span-full text-sm text-red-600">{error}</p>}
      {result && <div className="col-span-full"><ResultBanner>{result}</ResultBanner></div>}
    </div>
  )
}
