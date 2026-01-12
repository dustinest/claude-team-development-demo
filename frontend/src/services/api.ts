import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Request interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add any auth tokens here if needed in the future
    console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  (error: AxiosError) => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log(`API Response: ${response.status} ${response.config.url}`)
    return response
  },
  (error: AxiosError) => {
    // Enhanced error handling with user-friendly messages
    let errorMessage = 'An unexpected error occurred'

    if (error.response) {
      // Server responded with error status
      const status = error.response.status
      const data = error.response.data as any

      console.error('API Error:', status, data)

      switch (status) {
        case 400:
          errorMessage = data?.message || 'Invalid request. Please check your input.'
          break
        case 401:
          errorMessage = 'Authentication required. Please log in again.'
          break
        case 403:
          errorMessage = 'Access denied. You do not have permission.'
          break
        case 404:
          errorMessage = 'Resource not found.'
          break
        case 409:
          errorMessage = data?.message || 'Conflict. The operation could not be completed.'
          break
        case 422:
          errorMessage = data?.message || 'Validation error. Please check your input.'
          break
        case 500:
          errorMessage = 'Server error. Please try again later.'
          break
        case 503:
          errorMessage = 'Service temporarily unavailable. Please try again later.'
          break
        default:
          errorMessage = data?.message || `Server error (${status}). Please try again.`
      }

      // Attach user-friendly message to error
      error.message = errorMessage
    } else if (error.request) {
      // Request made but no response
      console.error('Network Error:', error.message)
      error.message = 'Network error. Please check your internet connection.'
    } else if (error.code === 'ECONNABORTED') {
      // Timeout error
      error.message = 'Request timeout. Please try again.'
    } else {
      // Error setting up request
      console.error('Error:', error.message)
      error.message = errorMessage
    }

    return Promise.reject(error)
  }
)

export default apiClient
