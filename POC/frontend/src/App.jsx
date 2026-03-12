import { useState } from 'react'
import './App.css'
import AddProductionBatch from './components/AddProductionBatch'
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'

function App() {
  const queryClient = new QueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <div>
        <AddProductionBatch />
      </div>
    </QueryClientProvider>
  )
}

export default App
