import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { Layout } from './components/Layout'
import { LoginPage } from './pages/auth/LoginPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { PatientsPage } from './pages/patients/PatientsPage'
import { PatientRegisterPage } from './pages/patients/PatientRegisterPage'
import { PatientProfilePage } from './pages/patients/PatientProfilePage'
import { OpdEncounterPage } from './pages/opd/OpdEncounterPage'
import { AdmitPatientPage } from './pages/ipd/AdmitPatientPage'
import { IpdBedCensusPage } from './pages/ipd/IpdBedCensusPage'
import { IpdEncounterPage } from './pages/ipd/IpdEncounterPage'
import { AnalyticsDashboardPage } from './pages/analytics/AnalyticsDashboardPage'
import { AdministrationPage } from './pages/admin/AdministrationPage'
import { IntegrationPage } from './pages/integration/IntegrationPage'

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Layout>
                <DashboardPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/patients"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'NURSE', 'ADMIN']}>
              <Layout>
                <PatientsPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/register"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
              <Layout>
                <PatientRegisterPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/:upid"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'NURSE', 'ADMIN']}>
              <Layout>
                <PatientProfilePage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/opd/:upid/:visitId"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
              <Layout>
                <OpdEncounterPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/ipd"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'NURSE', 'ADMIN']}>
              <Layout>
                <IpdBedCensusPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/patients/:upid/admit"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
              <Layout>
                <AdmitPatientPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/ipd/:admissionId"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'NURSE', 'ADMIN']}>
              <Layout>
                <IpdEncounterPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/analytics"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
              <Layout>
                <AnalyticsDashboardPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/integration"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
              <Layout>
                <IntegrationPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <Layout>
                <AdministrationPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  )
}

export default App
