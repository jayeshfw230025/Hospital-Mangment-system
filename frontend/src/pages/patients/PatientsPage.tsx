import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { searchPatients, type PatientResponse } from '../../api/patients'
import { extractErrorMessage } from '../../api/client'

export function PatientsPage() {
  const [fullName, setFullName] = useState('')
  const [contactNumber, setContactNumber] = useState('')
  const [upid, setUpid] = useState('')
  const [page, setPage] = useState(0)
  const [results, setResults] = useState<PatientResponse[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function runSearch(targetPage = 0) {
    setLoading(true)
    setError(null)
    try {
      const result = await searchPatients({
        fullName,
        contactNumber,
        upid,
        page: targetPage,
        size: 10,
      })
      setResults(result.content)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
      setPage(result.number)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    runSearch(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    runSearch(0)
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-800">Patients</h1>
        <Link
          to="/patients/register"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Register new patient
        </Link>
      </div>

      <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-3 rounded-lg bg-white p-4 shadow-sm sm:grid-cols-4">
        <input
          placeholder="Name"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
        />
        <input
          placeholder="Contact number"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          value={contactNumber}
          onChange={(e) => setContactNumber(e.target.value)}
        />
        <input
          placeholder="UPID"
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
          value={upid}
          onChange={(e) => setUpid(e.target.value)}
        />
        <button type="submit" className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50">
          Search
        </button>
      </form>

      {error && <p className="text-sm text-red-600">{error}</p>}

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-slate-500">
            <tr>
              <th className="px-4 py-2 font-medium">UPID</th>
              <th className="px-4 py-2 font-medium">Name</th>
              <th className="px-4 py-2 font-medium">Gender</th>
              <th className="px-4 py-2 font-medium">DOB</th>
              <th className="px-4 py-2 font-medium">Contact</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                  Loading...
                </td>
              </tr>
            )}
            {!loading && results.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                  No patients found
                </td>
              </tr>
            )}
            {results.map((patient) => (
              <tr key={patient.upid} className="border-t border-slate-100 hover:bg-slate-50">
                <td className="px-4 py-2">
                  <Link to={`/patients/${patient.upid}`} className="font-medium text-indigo-600 hover:underline">
                    {patient.upid}
                  </Link>
                </td>
                <td className="px-4 py-2">{patient.fullName}</td>
                <td className="px-4 py-2">{patient.gender}</td>
                <td className="px-4 py-2">{patient.dateOfBirth}</td>
                <td className="px-4 py-2">{patient.primaryContactNumber}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between text-sm text-slate-500">
          <span>{totalElements} total patients</span>
          <div className="flex gap-2">
            <button
              disabled={page === 0}
              onClick={() => runSearch(page - 1)}
              className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
            >
              Previous
            </button>
            <span>
              Page {page + 1} of {totalPages}
            </span>
            <button
              disabled={page + 1 >= totalPages}
              onClick={() => runSearch(page + 1)}
              className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
