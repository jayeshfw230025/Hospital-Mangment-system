import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getPatientByUpid, getPatientQrCode, BLOOD_GROUP_LABELS, type PatientResponse } from '../../api/patients'
import { extractErrorMessage } from '../../api/client'

export function PatientProfilePage() {
  const { upid } = useParams<{ upid: string }>()
  const navigate = useNavigate()
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [qrCode, setQrCode] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!upid) return
    setLoading(true)
    getPatientByUpid(upid)
      .then(setPatient)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [upid])

  async function handleShowQrCode() {
    if (!upid) return
    try {
      const code = await getPatientQrCode(upid)
      setQrCode(code)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  if (loading) return <p className="text-slate-400">Loading...</p>
  if (error) return <p className="text-sm text-red-600">{error}</p>
  if (!patient) return null

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-800">{patient.fullName}</h1>
          <p className="text-sm text-slate-500">{patient.upid}</p>
        </div>
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate(`/opd/${upid}/${Date.now()}`)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
          >
            Start OPD Visit
          </button>
          <button
            onClick={() => navigate(`/patients/${upid}/admit`)}
            className="rounded-md border border-indigo-300 px-4 py-2 text-sm font-medium text-indigo-700 hover:bg-indigo-50"
          >
            Admit to IPD
          </button>
          <Link to="/patients" className="text-sm text-indigo-600 hover:underline">
            Back to patients
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <div className="rounded-lg bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold text-slate-600">Demographics</h2>
          <dl className="space-y-2 text-sm">
            <Row label="Date of birth" value={patient.dateOfBirth} />
            <Row label="Gender" value={patient.gender} />
            <Row label="Marital status" value={patient.maritalStatus ?? '-'} />
            <Row label="Blood group" value={patient.bloodGroup ? BLOOD_GROUP_LABELS[patient.bloodGroup] : '-'} />
            <Row label="Nationality" value={patient.nationality ?? '-'} />
          </dl>
        </div>

        <div className="rounded-lg bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold text-slate-600">Contact</h2>
          <dl className="space-y-2 text-sm">
            <Row label="Primary contact" value={patient.primaryContactNumber} />
            <Row label="Secondary contact" value={patient.secondaryContactNumber ?? '-'} />
            <Row label="Email" value={patient.email ?? '-'} />
            <Row label="ABHA number" value={patient.abhaNumber ?? 'Not linked'} />
          </dl>
        </div>

        {patient.address && (
          <div className="rounded-lg bg-white p-5 shadow-sm">
            <h2 className="mb-3 text-sm font-semibold text-slate-600">Address</h2>
            <p className="text-sm text-slate-600">
              {[
                patient.address.addressLine1,
                patient.address.city,
                patient.address.state,
                patient.address.pinCode,
              ]
                .filter(Boolean)
                .join(', ') || '-'}
            </p>
          </div>
        )}

        {patient.emergencyContact && (
          <div className="rounded-lg bg-white p-5 shadow-sm">
            <h2 className="mb-3 text-sm font-semibold text-slate-600">Emergency Contact</h2>
            <dl className="space-y-2 text-sm">
              <Row label="Name" value={patient.emergencyContact.name ?? '-'} />
              <Row label="Contact number" value={patient.emergencyContact.contactNumber ?? '-'} />
              <Row label="Relation" value={patient.emergencyContact.relation ?? '-'} />
            </dl>
          </div>
        )}

        <div className="rounded-lg bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold text-slate-600">QR Code</h2>
          {qrCode ? (
            <img src={`data:image/png;base64,${qrCode}`} alt="Patient QR code" className="h-40 w-40" />
          ) : (
            <button
              onClick={handleShowQrCode}
              className="rounded-md border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
            >
              Generate QR code
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-slate-400">{label}</dt>
      <dd className="text-right text-slate-700">{value}</dd>
    </div>
  )
}
