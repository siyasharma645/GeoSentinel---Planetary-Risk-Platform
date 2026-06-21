import axios from 'axios'

const BASE = import.meta.env.VITE_API_URL || '/api/v1'

const client = axios.create({ baseURL: BASE })

// Inject JWT on every request
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

// Auto-refresh on 401
client.interceptors.response.use(
  r => r,
  async err => {
    if (err.response?.status === 401) {
      const rt = localStorage.getItem('refreshToken')
      if (rt) {
        try {
          const { data } = await axios.post(`${BASE}/auth/refresh`, { refreshToken: rt })
          localStorage.setItem('accessToken', data.accessToken)
          localStorage.setItem('refreshToken', data.refreshToken)
          err.config.headers.Authorization = `Bearer ${data.accessToken}`
          return client(err.config)
        } catch { localStorage.clear(); window.location.href = '/login' }
      }
    }
    return Promise.reject(err)
  }
)

export const authApi = {
  register: d => client.post('/auth/register', d),
  login:    d => client.post('/auth/login', d),
  refresh:  t => client.post('/auth/refresh', { refreshToken: t }),
  logout:   () => client.post('/auth/logout'),
  me:       () => client.get('/auth/me'),
}

export const riskApi = {
  getReport:    d => client.post('/risk/report', d),
  refreshReport:d => client.post('/risk/report/refresh', d),
  overview:     () => client.get('/risk/overview'),
}

export const disasterApi = {
  getActive:      () => client.get('/disasters/active'),
  getById:        id => client.get(`/disasters/${id}`),
  getByCountry:   cc => client.get(`/disasters/country/${cc}`),
  getByType:      t  => client.get(`/disasters/type/${t}`),
  getNear:        (lat,lon,km=500) => client.get(`/disasters/near?lat=${lat}&lon=${lon}&radiusKm=${km}`),
  create:         d  => client.post('/disasters', d),
  updateStatus:   (id,status) => client.patch(`/disasters/${id}/status`, { status }),
  updateSeverity: (id,sev)    => client.patch(`/disasters/${id}/severity`, { severity: sev }),
}

export const climateApi = {
  getByType:    t   => client.get(`/climate/metrics/${t}`),
  getByCountry: cc  => client.get(`/climate/country/${cc}`),
  getAnomalies: (th=1.5) => client.get(`/climate/anomalies?threshold=${th}`),
  getSummary:   ()  => client.get('/climate/summary'),
  record:       d   => client.post('/climate/metrics', d),
}

export const alertApi = {
  getActive:   (page=0,size=20) => client.get(`/alerts?page=${page}&size=${size}`),
  getCritical: () => client.get('/alerts/critical'),
  getByCountry:cc => client.get(`/alerts/country/${cc}`),
  getCount:    () => client.get('/alerts/count'),
  resolve:     id => client.patch(`/alerts/${id}/resolve`),
}

export default client
