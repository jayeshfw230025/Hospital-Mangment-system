import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAvailableBeds, type BedResponse, type WardType } from '../../api/ipdAdmission'
import { extractErrorMessage } from '../../api/client'

const WARDS: WardType[] = ['GENERAL', 'ICU', 'PRIVATE', 'SEMI_PRIVATE']

/**
 * There is no "list all IPD admissions" endpoint in the backend (only
 * GET /api/v1/ipd/admission/{id}) - so this page is scoped to what the API
 * actually supports: bed availability by ward, plus a quick jump to a known
 * admission ID. A true IPD patient list would need a new backend endpoint.
 */
export function IpdBedCensusPage() {
  const navigate = useNavigate()
  const [wardType, setWardType] = useState<WardType>('GENERAL')
  const [beds, setBeds] = useState<BedResponse[]>([])
  const [error, setError] = useState<string | null>(null)
  const [admissionIdInput, setAdmissionIdInput] = useState('')

  async function loadBeds(ward: WardType) {
    setWardType(ward)
    try {
      setBeds(await getAvailableBeds(ward))
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    loadBeds('GENERAL')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleJump(event: FormEvent) {
    event.preventDefault()
    if (admissionIdInput.trim()) {
      navigate(`/ipd/${admissionIdInput.trim()}`)
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">IPD Bed Census</h1>

      <form onSubmit={handleJump} className="flex items-end gap-3 rounded-lg bg-white p-4 shadow-sm">
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Go to admission by ID</span>
          <input
            className="input"
            value={admissionIdInput}
            onChange={(e) => setAdmissionIdInput(e.target.value)}
            placeholder="e.g. 1"
          />
        </label>
        <button type="submit" className="rounded-md border border-slate-300 px-4 py-2 text-sm hover:bg-slate-50">
          Open
        </button>
      </form>

      <div className="flex gap-2">
        {WARDS.map((w) => (
          <button
            key={w}
            onClick={() => loadBeds(w)}
            className={`rounded-md px-3 py-1.5 text-sm font-medium ${
              wardType === w ? 'bg-indigo-600 text-white' : 'border border-slate-300 text-slate-600 hover:bg-slate-50'
            }`}
          >
            {w.replace('_', ' ')}
          </button>
        ))}
      </div>

      {error && <p className="text-sm text-red-600">{error}</p>}

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {beds.length === 0 && <p className="col-span-full text-sm text-slate-400">No available beds in this ward.</p>}
        {beds.map((bed) => (
          <div key={bed.id} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <p className="font-medium text-slate-800">Room {bed.roomNumber} · Bed {bed.bedNumber}</p>
            <p className="text-xs text-green-600">Available</p>
          </div>
        ))}
      </div>
    </div>
  )
}
