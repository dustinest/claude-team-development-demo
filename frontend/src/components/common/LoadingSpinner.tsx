import React from 'react'
import { Box, CircularProgress } from '@mui/material'

/**
 * Centered loading spinner component
 * Used to indicate loading state during async operations
 */
const LoadingSpinner: React.FC = () => {
  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="200px"
    >
      <CircularProgress />
    </Box>
  )
}

export default LoadingSpinner
