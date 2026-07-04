import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  registerPatient,
  BLOOD_GROUP_LABELS,
  type BloodGroup,
  type Gender,
  type MaritalStatus,
} from '../../api/patients'
import { extractErrorMessage } from '../../api/client'

const emptyForm = {
  fullName: '',
  dateOfBirth: '',
  gender: '' as Gender | '',
  maritalStatus: '' as MaritalStatus | '',
  bloodGroup: '' as BloodGroup | '',
  primaryContactNumber: '',
  secondaryContactNumber: '',
  email: '',
  nationality: '',
  addressLine1: '',
  city: '',
  state: '',
  pinCode: '',
  emergencyContactName: '',
  emergencyContactNumber: '',
  emergencyContactRelation: '',
}

export function PatientRegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function update<K extends keyof typeof emptyForm>(key: K, value: (typeof emptyForm)[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const hasAddress = form.addressLine1 || form.city || form.state || form.pinCode
      const hasEmergencyContact = form.emergencyContactName || form.emergencyContactNumber

      const patient = await registerPatient({
        fullName: form.fullName,
        dateOfBirth: form.dateOfBirth,
        gender: form.gender as Gender,
        maritalStatus: form.maritalStatus || null,
        bloodGroup: form.bloodGroup || null,
        nationality: form.nationality || null,
        primaryContactNumber: form.primaryContactNumber,
        secondaryContactNumber: form.secondaryContactNumber || null,
        email: form.email || null,
        address: hasAddress
          ? { addressLine1: form.addressLine1, city: form.city, state: form.state, pinCode: form.pinCode }
          : null,
        emergencyContact: hasEmergencyContact
          ? {
              name: form.emergencyContactName,
              contactNumber: form.emergencyContactNumber,
              relation: form.emergencyContactRelation,
            }
          : null,
      })
      navigate(`/patients/${patient.upid}`)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">Register Patient</h1>

      <form onSubmit={handleSubmit} className="space-y-6 rounded-lg bg-white p-6 shadow-sm">
        <section className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Full name" required>
            <input
              className="input"
              value={form.fullName}
              onChange={(e) => update('fullName', e.target.value)}
              required
            />
          </Field>
          <Field label="Date of birth" required>
            <input
              type="date"
              className="input"
              value={form.dateOfBirth}
              onChange={(e) => update('dateOfBirth', e.target.value)}
              required
            />
          </Field>
          <Field label="Gender" required>
            <select
              className="input"
              value={form.gender}
              onChange={(e) => update('gender', e.target.value as Gender)}
              required
            >
              <option value="">Select</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </Field>
          <Field label="Marital status">
            <select
              className="input"
              value={form.maritalStatus}
              onChange={(e) => update('maritalStatus', e.target.value as MaritalStatus)}
            >
              <option value="">Select</option>
              <option value="UNMARRIED">Unmarried</option>
              <option value="MARRIED">Married</option>
              <option value="DIVORCED">Divorced</option>
              <option value="WIDOWED">Widowed</option>
            </select>
          </Field>
          <Field label="Blood group">
            <select
              className="input"
              value={form.bloodGroup}
              onChange={(e) => update('bloodGroup', e.target.value as BloodGroup)}
            >
              <option value="">Select</option>
              {Object.entries(BLOOD_GROUP_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Nationality">
            <input className="input" value={form.nationality} onChange={(e) => update('nationality', e.target.value)} />
          </Field>
        </section>

        <section className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Primary contact number" required>
            <input
              className="input"
              value={form.primaryContactNumber}
              onChange={(e) => update('primaryContactNumber', e.target.value)}
              placeholder="10-digit number"
              required
            />
          </Field>
          <Field label="Secondary contact number">
            <input
              className="input"
              value={form.secondaryContactNumber}
              onChange={(e) => update('secondaryContactNumber', e.target.value)}
            />
          </Field>
          <Field label="Email">
            <input
              type="email"
              className="input"
              value={form.email}
              onChange={(e) => update('email', e.target.value)}
            />
          </Field>
        </section>

        <section>
          <h2 className="mb-3 text-sm font-semibold text-slate-600">Address (optional)</h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Field label="Address line 1">
              <input
                className="input"
                value={form.addressLine1}
                onChange={(e) => update('addressLine1', e.target.value)}
              />
            </Field>
            <Field label="City">
              <input className="input" value={form.city} onChange={(e) => update('city', e.target.value)} />
            </Field>
            <Field label="State">
              <input className="input" value={form.state} onChange={(e) => update('state', e.target.value)} />
            </Field>
            <Field label="PIN code">
              <input className="input" value={form.pinCode} onChange={(e) => update('pinCode', e.target.value)} />
            </Field>
          </div>
        </section>

        <section>
          <h2 className="mb-3 text-sm font-semibold text-slate-600">Emergency contact (optional)</h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Field label="Name">
              <input
                className="input"
                value={form.emergencyContactName}
                onChange={(e) => update('emergencyContactName', e.target.value)}
              />
            </Field>
            <Field label="Contact number">
              <input
                className="input"
                value={form.emergencyContactNumber}
                onChange={(e) => update('emergencyContactNumber', e.target.value)}
              />
            </Field>
            <Field label="Relation">
              <input
                className="input"
                value={form.emergencyContactRelation}
                onChange={(e) => update('emergencyContactRelation', e.target.value)}
              />
            </Field>
          </div>
        </section>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-5 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {submitting ? 'Registering...' : 'Register patient'}
        </button>
      </form>
    </div>
  )
}

function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">
        {label}
        {required && <span className="text-red-500"> *</span>}
      </span>
      {children}
    </label>
  )
}
