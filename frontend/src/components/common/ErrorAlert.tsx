import React from 'react'
import { Alert, AlertTitle } from '@mui/material'

interface ErrorAlertProps {
  message: string
  title?: string
}

/**
 * Error alert component for displaying error messages
 * @param message - Error message to display
 * @param title - Optional title (defaults to "Error")
 */
const ErrorAlert: React.FC<ErrorAlertProps> = ({ message, title = 'Error' }) => {
  return (
    <Alert severity="error" sx={{ mb: 2 }}>
      <AlertTitle>{title}</AlertTitle>
      {message}
    </Alert>
  )
}

export default ErrorAlert
