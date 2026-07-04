import { useEffect, useState } from 'react'
import { apiClient, extractErrorMessage } from '../../api/client'
import { getDashboard, type DashboardResponse, type Granularity } from '../../api/analytics'

export function AnalyticsDashboardPage() {
  const [granularity, setGranularity] = useState<Granularity>('MONTHLY')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [data, setData] = useState<DashboardResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [exporting, setExporting] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const result = await getDashboard(startDate || undefined, endDate || undefined, granularity)
      setData(result)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function handleExport(format: 'csv' | 'pdf') {
    setExporting(true)
    try {
      const response = await apiClient.get(`/analytics/export`, {
        params: { format, granularity },
        responseType: 'blob',
      })
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.download = `analytics-dashboard.${format}`
      link.click()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setExporting(false)
    }
  }

  if (loading) return <p className="text-slate-400">Loading...</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (!data) return null

  const { kpis, diseaseDistribution } = data
  const maxDiseaseCount = Math.max(1, ...kpis.topGiDiseases.map((d) => d.count))

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-semibold text-slate-800">Analytics Dashboard</h1>
        <div className="flex items-center gap-2">
          <input type="date" className="input" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
          <input type="date" className="input" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
          <select className="input" value={granularity} onChange={(e) => setGranularity(e.target.value as Granularity)}>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
            <option value="QUARTERLY">Quarterly</option>
          </select>
          <button onClick={load} className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50">
            Apply
          </button>
          <button
            onClick={() => handleExport('csv')}
            disabled={exporting}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
          >
            Export CSV
          </button>
          <button
            onClick={() => handleExport('pdf')}
            disabled={exporting}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
          >
            Export PDF
          </button>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        <KpiCard
          label="New registrations"
          value={kpis.patientVolume.series.reduce((sum, b) => sum + b.newRegistrations, 0)}
        />
        <KpiCard label="IPD admissions" value={kpis.opdIpdRatio.ipdAdmissions} />
        <KpiCard label="OPD encounters" value={kpis.opdIpdRatio.opdEncounters} />
        <KpiCard label="OPD:IPD ratio" value={kpis.opdIpdRatio.ratio.toFixed(1)} />
        <KpiCard label="30-day readmission" value={`${kpis.readmissionRate.rate30DayPercent.toFixed(1)}%`} />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card title="Top GI Diseases">
          <div className="space-y-2">
            {kpis.topGiDiseases.slice(0, 10).map((d) => (
              <BarRow key={d.icd10Code} label={`${d.icd10Code} — ${d.description}`} value={d.count} max={maxDiseaseCount} />
            ))}
            {kpis.topGiDiseases.length === 0 && <p className="text-sm text-slate-400">No diagnoses recorded yet.</p>}
          </div>
        </Card>

        <Card title="Average Length of Stay by Diagnosis">
          <table className="w-full text-left text-sm">
            <thead className="text-slate-400">
              <tr>
                <th className="pb-1 font-medium">Diagnosis</th>
                <th className="pb-1 font-medium">ALOS (days)</th>
                <th className="pb-1 font-medium">Discharges</th>
              </tr>
            </thead>
            <tbody>
              {kpis.alosByDiagnosis.map((a) => (
                <tr key={a.icd10Code} className="border-t border-slate-100">
                  <td className="py-1">{a.icd10Code} — {a.description}</td>
                  <td className="py-1">{a.averageLengthOfStayDays.toFixed(1)}</td>
                  <td className="py-1">{a.dischargeCount}</td>
                </tr>
              ))}
              {kpis.alosByDiagnosis.length === 0 && (
                <tr><td colSpan={3} className="py-2 text-slate-400">No discharges recorded yet.</td></tr>
              )}
            </tbody>
          </table>
        </Card>

        <Card title="Procedure Success / Complication Rates">
          <table className="w-full text-left text-sm">
            <thead className="text-slate-400">
              <tr>
                <th className="pb-1 font-medium">Procedure</th>
                <th className="pb-1 font-medium">Total</th>
                <th className="pb-1 font-medium">Complications</th>
                <th className="pb-1 font-medium">Success %</th>
              </tr>
            </thead>
            <tbody>
              {kpis.procedureStats.map((p) => (
                <tr key={p.procedureType} className="border-t border-slate-100">
                  <td className="py-1">{p.label}</td>
                  <td className="py-1">{p.totalCount}</td>
                  <td className="py-1">{p.complicationCount}</td>
                  <td className="py-1">{p.successRatePercent.toFixed(1)}%</td>
                </tr>
              ))}
              {kpis.procedureStats.length === 0 && (
                <tr><td colSpan={4} className="py-2 text-slate-400">No procedures recorded yet.</td></tr>
              )}
            </tbody>
          </table>
        </Card>

        <Card title="Mortality by Diagnosis">
          <table className="w-full text-left text-sm">
            <thead className="text-slate-400">
              <tr>
                <th className="pb-1 font-medium">Diagnosis</th>
                <th className="pb-1 font-medium">Discharges</th>
                <th className="pb-1 font-medium">Expired</th>
                <th className="pb-1 font-medium">Mortality %</th>
              </tr>
            </thead>
            <tbody>
              {kpis.mortalityByDiagnosis.map((m) => (
                <tr key={m.icd10Code} className="border-t border-slate-100">
                  <td className="py-1">{m.icd10Code} — {m.description}</td>
                  <td className="py-1">{m.totalDischarges}</td>
                  <td className="py-1">{m.expiredCount}</td>
                  <td className="py-1">{m.mortalityRatePercent.toFixed(1)}%</td>
                </tr>
              ))}
              {kpis.mortalityByDiagnosis.length === 0 && (
                <tr><td colSpan={4} className="py-2 text-slate-400">No discharges recorded yet.</td></tr>
              )}
            </tbody>
          </table>
        </Card>

        <Card title="Referral Pattern — Top Referring Doctors">
          <div className="space-y-1 text-sm text-slate-600">
            {kpis.referralPattern.topReferringDoctors.length === 0 && (
              <p className="text-slate-400">No referrals recorded yet.</p>
            )}
            {kpis.referralPattern.topReferringDoctors.map((d) => (
              <div key={d.label} className="flex justify-between">
                <span>{d.label}</span>
                <span className="font-medium">{d.count}</span>
              </div>
            ))}
          </div>
        </Card>

        <Card title="Patient Satisfaction">
          <p className="text-sm text-slate-500">
            {kpis.patientSatisfaction.available ? 'Available' : '⚠ Not available'} — {kpis.patientSatisfaction.message}
          </p>
        </Card>
      </div>

      <Card title="Disease Distribution — Age Group / Gender / Treatment Outcome">
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
          <DistributionList title="By age group" items={diseaseDistribution.byAgeGroup} />
          <DistributionList title="By gender" items={diseaseDistribution.byGender} />
          <DistributionList title="By treatment outcome" items={diseaseDistribution.byTreatmentOutcome} />
        </div>
      </Card>
    </div>
  )
}

function KpiCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-lg bg-white p-4 shadow-sm">
      <p className="text-xs font-medium text-slate-400">{label}</p>
      <p className="mt-1 text-2xl font-semibold text-slate-800">{value}</p>
    </div>
  )
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-lg bg-white p-5 shadow-sm">
      <h2 className="mb-3 text-sm font-semibold text-slate-600">{title}</h2>
      {children}
    </div>
  )
}

function BarRow({ label, value, max }: { label: string; value: number; max: number }) {
  return (
    <div>
      <div className="flex justify-between text-xs text-slate-500">
        <span>{label}</span>
        <span>{value}</span>
      </div>
      <div className="mt-0.5 h-2 rounded-full bg-slate-100">
        <div className="h-2 rounded-full bg-indigo-500" style={{ width: `${(value / max) * 100}%` }} />
      </div>
    </div>
  )
}

function DistributionList({ title, items }: { title: string; items: { label: string; count: number }[] }) {
  return (
    <div>
      <p className="mb-2 text-xs font-semibold text-slate-500">{title}</p>
      <div className="space-y-1 text-sm text-slate-600">
        {items.length === 0 && <p className="text-slate-400">No data.</p>}
        {items.map((item) => (
          <div key={item.label} className="flex justify-between">
            <span>{item.label}</span>
            <span className="font-medium">{item.count}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
