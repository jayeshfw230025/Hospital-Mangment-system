import { useEffect, useState } from 'react'
import { getAuditTrail, type AuditLogResponse } from '../../api/audit'
import { extractErrorMessage } from '../../api/client'

export function AdministrationPage() {
  const [logs, setLogs] = useState<AuditLogResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  async function load(targetPage: number) {
    setLoading(true)
    setError(null)
    try {
      const result = await getAuditTrail(targetPage, 25)
      setLogs(result.content)
      setTotalPages(result.totalPages)
      setPage(result.number)
    } catch (err) {
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-800">Administration</h1>

      <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
        User management, drug master management, and ICD-10 master management are not wired up here — the backend
        only exposes read/search endpoints for drugs and ICD-10 codes and no user CRUD endpoints at all, so there is
        nothing real to build a management UI against yet. The audit trail below is the one fully real capability in
        this module.
      </div>

      <div className="rounded-lg bg-white p-5 shadow-sm">
        <h2 className="mb-3 text-sm font-semibold text-slate-600">Audit Trail</h2>
        {error && <p className="text-sm text-red-600">{error}</p>}
        {loading && <p className="text-sm text-slate-400">Loading...</p>}
        {!loading && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 text-slate-400">
                  <tr>
                    <th className="py-2 pr-3 font-medium">Timestamp</th>
                    <th className="py-2 pr-3 font-medium">User</th>
                    <th className="py-2 pr-3 font-medium">Action</th>
                    <th className="py-2 pr-3 font-medium">Module</th>
                    <th className="py-2 pr-3 font-medium">Record ID</th>
                    <th className="py-2 pr-3 font-medium">Patient</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.length === 0 && (
                    <tr>
                      <td colSpan={6} className="py-4 text-center text-slate-400">
                        No audit log entries yet.
                      </td>
                    </tr>
                  )}
                  {logs.map((log) => (
                    <tr key={log.id} className="border-b border-slate-100">
                      <td className="py-2 pr-3 text-xs text-slate-500">{new Date(log.timestamp).toLocaleString()}</td>
                      <td className="py-2 pr-3">
                        {log.username ?? '-'} {log.userRole && <span className="text-xs text-slate-400">({log.userRole})</span>}
                      </td>
                      <td className="py-2 pr-3">
                        <span
                          className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            log.action === 'CREATE'
                              ? 'bg-green-100 text-green-700'
                              : log.action === 'UPDATE'
                                ? 'bg-blue-100 text-blue-700'
                                : log.action === 'LOGIN' || log.action === 'LOGOUT'
                                  ? 'bg-slate-100 text-slate-600'
                                  : 'bg-slate-50 text-slate-500'
                          }`}
                        >
                          {log.action}
                        </span>
                      </td>
                      <td className="py-2 pr-3">{log.moduleName}</td>
                      <td className="py-2 pr-3 text-xs text-slate-500">{log.recordId ?? '-'}</td>
                      <td className="py-2 pr-3 text-xs text-slate-500">{log.relatedPatientId ?? '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {totalPages > 1 && (
              <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
                <button
                  disabled={page === 0}
                  onClick={() => load(page - 1)}
                  className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
                >
                  Previous
                </button>
                <span>
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  disabled={page + 1 >= totalPages}
                  onClick={() => load(page + 1)}
                  className="rounded-md border border-slate-300 px-3 py-1 disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
