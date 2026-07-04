import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { extractErrorMessage } from '../../api/client'

export function LoginPage() {
  const { initiateLogin, completeLogin } = useAuth()
  const navigate = useNavigate()

  const [step, setStep] = useState<'credentials' | 'otp'>('credentials')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [txnId, setTxnId] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleCredentialsSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const result = await initiateLogin(username, password)
      setTxnId(result.txnId)
      setStep('otp')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleOtpSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await completeLogin(txnId, otp)
      navigate('/dashboard')
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-slate-100">
      <div className="w-full max-w-sm rounded-xl bg-white p-8 shadow-sm">
        <h1 className="mb-1 text-xl font-semibold text-slate-800">Hospital Management System</h1>
        <p className="mb-6 text-sm text-slate-500">Gastroenterology Care Platform</p>

        {step === 'credentials' && (
          <form onSubmit={handleCredentialsSubmit} className="space-y-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Username</label>
              <input
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="doctor.demo"
                required
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Password</label>
              <input
                type="password"
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            {error && <p className="text-sm text-red-600">{error}</p>}
            <button
              type="submit"
              disabled={submitting}
              className="w-full rounded-md bg-indigo-600 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              {submitting ? 'Please wait...' : 'Continue'}
            </button>
            <p className="text-xs text-slate-400">
              Demo accounts: doctor.demo / nurse.demo / admin.demo / pharmacist.demo / dietitian.demo, password
              Passw0rd!23
            </p>
          </form>
        )}

        {step === 'otp' && (
          <form onSubmit={handleOtpSubmit} className="space-y-4">
            <p className="text-sm text-slate-600">
              An OTP has been generated. No SMS/email gateway is configured yet, so check the backend server
              console log for the 6-digit code.
            </p>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">OTP</label>
              <input
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm tracking-widest focus:border-indigo-500 focus:outline-none"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                maxLength={6}
                required
              />
            </div>
            {error && <p className="text-sm text-red-600">{error}</p>}
            <button
              type="submit"
              disabled={submitting}
              className="w-full rounded-md bg-indigo-600 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              {submitting ? 'Verifying...' : 'Verify & Sign in'}
            </button>
            <button
              type="button"
              onClick={() => setStep('credentials')}
              className="w-full text-sm text-slate-500 hover:text-slate-700"
            >
              Back
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
