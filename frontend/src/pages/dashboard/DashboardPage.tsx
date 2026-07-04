import { Link } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'

export function DashboardPage() {
  const { user } = useAuth()

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-800">Welcome, {user?.fullName}</h1>
        <p className="text-sm text-slate-500">Signed in as {user?.role}</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Link
          to="/patients"
          className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md"
        >
          <p className="text-sm font-medium text-slate-500">Patients</p>
          <p className="mt-1 text-lg font-semibold text-slate-800">Search &amp; Register</p>
        </Link>
      </div>
    </div>
  )
}
