import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { fetchMe, login as apiLogin, logout as apiLogout, verifyOtp as apiVerifyOtp, type MeResponse } from '../api/auth'
import { tokenStorage } from '../api/client'

interface AuthContextValue {
  user: MeResponse | null
  loading: boolean
  initiateLogin: (username: string, password: string) => Promise<{ txnId: string; message: string }>
  completeLogin: (txnId: string, otp: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<MeResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const accessToken = tokenStorage.getAccessToken()
    if (!accessToken) {
      setLoading(false)
      return
    }
    fetchMe()
      .then(setUser)
      .catch(() => tokenStorage.clear())
      .finally(() => setLoading(false))
  }, [])

  async function initiateLogin(username: string, password: string) {
    const response = await apiLogin(username, password)
    return { txnId: response.txnId, message: response.message }
  }

  async function completeLogin(txnId: string, otp: string) {
    const tokens = await apiVerifyOtp(txnId, otp)
    tokenStorage.setTokens(tokens.accessToken, tokens.refreshToken)
    const me = await fetchMe()
    setUser(me)
  }

  async function logout() {
    const refreshToken = tokenStorage.getRefreshToken()
    try {
      if (refreshToken) await apiLogout(refreshToken)
    } finally {
      tokenStorage.clear()
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, initiateLogin, completeLogin, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}
