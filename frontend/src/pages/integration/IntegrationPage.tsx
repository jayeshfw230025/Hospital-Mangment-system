import { useEffect, useState } from 'react'
import {
  createAbdmConsent,
  getAbdmHealthRecord,
  getLisStatus,
  initiateAbhaLink,
  type AbdmConsentResponse,
  type AbdmHealthRecordResponse,
  type AbhaLinkInitiationResponse,
  type LisStatusResponse,
} from '../../api/integration'
import { extractErrorMessage } from '../../api/client'

export function IntegrationPage() {
  const [lisStatus, setLisStatus] = useState<LisStatusResponse | null>(null)

  const [linkPatientId, setLinkPatientId] = useState('')
  const [linkAbhaNumber, setLinkAbhaNumber] = useState('')
  const [linkResult, setLinkResult] = useState<AbhaLinkInitiationResponse | null>(null)
  const [linkError, setLinkError] = useState<string | null>(null)

  const [consentPatientId, setConsentPatientId] = useState('')
  const [consentPurpose, setConsentPurpose] = useState('Treatment')
  const [consentResult, setConsentResult] = useState<AbdmConsentResponse | null>(null)
  const [consentError, setConsentError] = useState<string | null>(null)

  const [healthRecordResult, setHealthRecordResult] = useState<AbdmHealthRecordResponse | null>(null)
  const [healthRecordError, setHealthRecordError] = useState<string | null>(null)

  useEffect(() => {
    getLisStatus()
      .then(setLisStatus)
      .catch(() => setLisStatus(null))
  }, [])

  async function handleLink() {
    setLinkError(null)
    try {
      setLinkResult(await initiateAbhaLink(linkPatientId, linkAbhaNumber))
    } catch (err) {
      setLinkError(extractErrorMessage(err))
    }
  }

  async function handleConsent() {
    setConsentError(null)
    try {
      const result = await createAbdmConsent(consentPatientId, consentPurpose, ['DiagnosticReport', 'Prescription'], 30)
      setConsentResult(result)
    } catch (err) {
      setConsentError(extractErrorMessage(err))
    }
  }

  async function handleHealthRecord() {
    if (!consentResult) return
    setHealthRecordError(null)
    try {
      setHealthRecordResult(await getAbdmHealthRecord(consentResult.patientId, consentResult.consentId))
    } catch (err) {
      setHealthRecordError(extractErrorMessage(err))
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">Integration Module</h1>

      <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
        The actual ABDM gateway and any live LIS/RIS connection are external systems not stood up at this stage —
        ABHA linking, consent, and health-record assembly below are real (persisted, validated) but transmission to
        ABDM's Health Information Exchange is logged server-side, not actually sent anywhere.
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="space-y-3 rounded-lg bg-white p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-600">ABHA Link Status</h2>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Patient UPID</span>
            <input className="input" value={linkPatientId} onChange={(e) => setLinkPatientId(e.target.value)} />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">ABHA number</span>
            <input className="input" value={linkAbhaNumber} onChange={(e) => setLinkAbhaNumber(e.target.value)} />
          </label>
          {linkError && <p className="text-sm text-red-600">{linkError}</p>}
          <button
            onClick={handleLink}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Initiate link
          </button>
          {linkResult && (
            <p className="rounded-md bg-indigo-50 px-3 py-2 text-sm text-indigo-800">{linkResult.message}</p>
          )}
        </div>

        <div className="space-y-3 rounded-lg bg-white p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-600">LIS/RIS Import Status</h2>
          {lisStatus ? (
            <p
              className={`rounded-md px-3 py-2 text-sm ${
                lisStatus.connected ? 'bg-green-50 text-green-700' : 'bg-amber-50 text-amber-700'
              }`}
            >
              {lisStatus.connected ? 'Connected' : 'Not connected'} — {lisStatus.message}
            </p>
          ) : (
            <p className="text-sm text-slate-400">Checking...</p>
          )}
          <p className="text-xs text-slate-400">
            Lab/radiology result import happens via the same investigation-order pipeline used in the OPD encounter's
            Investigations tab.
          </p>
        </div>

        <div className="space-y-3 rounded-lg bg-white p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-600">ABDM Consent Management</h2>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Patient UPID</span>
            <input className="input" value={consentPatientId} onChange={(e) => setConsentPatientId(e.target.value)} />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-slate-700">Purpose</span>
            <input className="input" value={consentPurpose} onChange={(e) => setConsentPurpose(e.target.value)} />
          </label>
          {consentError && <p className="text-sm text-red-600">{consentError}</p>}
          <button
            onClick={handleConsent}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Grant consent
          </button>
          {consentResult && (
            <div className="rounded-md bg-indigo-50 px-3 py-2 text-sm text-indigo-800">
              <p>Consent {consentResult.consentId}</p>
              <p>Status: {consentResult.status}</p>
              <p>Expires: {new Date(consentResult.expiresAt).toLocaleDateString()}</p>
            </div>
          )}
        </div>

        <div className="space-y-3 rounded-lg bg-white p-5 shadow-sm">
          <h2 className="text-sm font-semibold text-slate-600">Health Record Bundle</h2>
          <p className="text-xs text-slate-400">Requires an active consent above.</p>
          {healthRecordError && <p className="text-sm text-red-600">{healthRecordError}</p>}
          <button
            onClick={handleHealthRecord}
            disabled={!consentResult}
            className="rounded-md border border-indigo-300 px-4 py-2 text-sm font-medium text-indigo-700 hover:bg-indigo-50 disabled:opacity-40"
          >
            Assemble &amp; share
          </button>
          {healthRecordResult && (
            <div className="rounded-md bg-indigo-50 px-3 py-2 text-sm text-indigo-800">
              <p>{healthRecordResult.message}</p>
              <p>Bundle contains {healthRecordResult.bundle.total} FHIR resources</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
