import React, { useState, useEffect } from 'react'
import { Snackbar, Alert } from '@mui/material'
import { WifiOff, Wifi } from '@mui/icons-material'

/**
 * Component to detect and display network connectivity status
 */
const NetworkStatus: React.FC = () => {
  const [showOffline, setShowOffline] = useState(false)
  const [showOnline, setShowOnline] = useState(false)

  useEffect(() => {
    const handleOnline = () => {
      setShowOnline(true)
      setShowOffline(false)
      // Auto-hide "back online" message after 3 seconds
      setTimeout(() => setShowOnline(false), 3000)
    }

    const handleOffline = () => {
      setShowOffline(true)
      setShowOnline(false)
    }

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    // Show offline message if starting offline
    if (!navigator.onLine) {
      setShowOffline(true)
    }

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }, [])

  return (
    <>
      {/* Offline notification */}
      <Snackbar
        open={showOffline}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity="error"
          icon={<WifiOff />}
          sx={{ width: '100%' }}
        >
          No internet connection. Some features may not work.
        </Alert>
      </Snackbar>

      {/* Back online notification */}
      <Snackbar
        open={showOnline}
        autoHideDuration={3000}
        onClose={() => setShowOnline(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert
          severity="success"
          icon={<Wifi />}
          sx={{ width: '100%' }}
          onClose={() => setShowOnline(false)}
        >
          Back online!
        </Alert>
      </Snackbar>
    </>
  )
}

export default NetworkStatus
