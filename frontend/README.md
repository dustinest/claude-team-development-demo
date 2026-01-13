# Fractional Stock Trading Platform - Frontend

React-based web application for fractional stock trading with real-time pricing, multi-currency wallets, and portfolio management.

## Tech Stack

- **Framework**: React 18.2
- **Build Tool**: Vite 5.1
- **Language**: TypeScript 5.3
- **UI Library**: Material-UI (MUI) v7.3
- **Routing**: React Router v6.22
- **HTTP Client**: Axios 1.6
- **Date Formatting**: date-fns 3.3
- **Deployment**: Docker + Nginx

## Features

### Pages (9 total)
- **Landing Page**: Public marketing page with feature highlights
- **Signup Page**: User registration with email, username, phone
- **Login Page**: Session authentication with User ID
- **Dashboard**: Overview with quick action cards
- **Wallet**: Multi-currency balance management (USD, EUR, GBP)
- **Market**: Browse and filter 20+ securities (stocks and indices)
- **Trading**: Buy/sell stocks with fractional shares (0.01 precision)
- **Portfolio**: View holdings with average purchase price
- **Transactions**: Complete transaction history with filters

### Key Features
- ✅ Fractional share trading (minimum 0.01 shares)
- ✅ Multi-currency support (USD, EUR, GBP)
- ✅ Real-time security pricing
- ✅ Wallet operations (deposit, withdraw, exchange)
- ✅ Order types: By Amount (USD) or By Quantity (shares)
- ✅ Session persistence with localStorage
- ✅ Error boundary for crash recovery
- ✅ Network status detection
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ User-friendly error messages
- ✅ Loading states throughout

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── common/           # Reusable components
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── ErrorAlert.tsx
│   │   │   ├── CurrencyDisplay.tsx
│   │   │   ├── ProtectedRoute.tsx
│   │   │   ├── ErrorBoundary.tsx
│   │   │   └── NetworkStatus.tsx
│   │   ├── forms/            # Form components
│   │   │   ├── DepositForm.tsx
│   │   │   ├── WithdrawForm.tsx
│   │   │   ├── ExchangeForm.tsx
│   │   │   └── TradeForm.tsx
│   │   └── layout/           # Layout components
│   │       ├── MainLayout.tsx
│   │       └── Navbar.tsx
│   ├── context/              # React Context providers
│   │   ├── AuthContext.tsx
│   │   └── NotificationContext.tsx
│   ├── hooks/                # Custom React hooks
│   │   ├── useAuth.ts
│   │   ├── useNotification.ts
│   │   └── useCurrency.ts
│   ├── pages/                # Page components
│   │   ├── LandingPage.tsx
│   │   ├── SignupPage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── WalletPage.tsx
│   │   ├── MarketPage.tsx
│   │   ├── TradingPage.tsx
│   │   ├── PortfolioPage.tsx
│   │   └── TransactionsPage.tsx
│   ├── services/             # API integration
│   │   ├── api.ts            # Axios client
│   │   └── endpoints/        # Service modules
│   │       ├── auth.service.ts
│   │       ├── user.service.ts
│   │       ├── wallet.service.ts
│   │       ├── securities.service.ts
│   │       ├── trading.service.ts
│   │       ├── portfolio.service.ts
│   │       └── transactions.service.ts
│   ├── types/                # TypeScript types
│   │   ├── api.types.ts      # API request/response types
│   │   └── enums.ts          # Enums
│   ├── App.tsx               # Root component
│   └── main.tsx              # Entry point
├── Dockerfile                # Production build
├── nginx.conf                # Nginx configuration
├── E2E_TESTING.md           # Testing guide
└── package.json
```

## Getting Started

### Prerequisites
- Node.js 20+
- npm 9+
- Docker (for deployment)

### Development Setup

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Configure environment:**
   ```bash
   # .env.development (auto-loaded by Vite)
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   VITE_APP_NAME=Trading Platform
   ```

3. **Start development server:**
   ```bash
   npm run dev
   ```
   Opens at http://localhost:3000 (with Vite HMR)

4. **Ensure backend is running:**
   ```bash
   # From project root
   cd ..
   docker-compose up api-gateway
   ```

### Build for Production

```bash
npm run build      # Outputs to dist/
npm run preview    # Test production build locally
```

## Docker Deployment

### Build Docker Image
```bash
docker build -t trading-frontend .
```

### Run Standalone
```bash
docker run -p 3000:80 trading-frontend
```

### Run with Docker Compose
```bash
# From project root
cd ..
docker-compose up frontend
```

Access at http://localhost:3000

## API Integration

### Base URL Configuration
- **Development**: http://localhost:8080/api/v1 (proxied via Vite)
- **Production**: /api/v1 (proxied via nginx)

### Service Endpoints
- `POST /signup` - User registration
- `GET /users/{userId}` - Get user details
- `GET /wallets/{userId}/balances` - Get wallet balances
- `POST /wallets/{userId}/deposit` - Deposit funds
- `POST /wallets/{userId}/withdraw` - Withdraw funds
- `POST /wallets/{userId}/exchange` - Exchange currency
- `GET /securities` - List all securities
- `POST /trades/buy` - Buy securities
- `POST /trades/sell` - Sell securities
- `GET /portfolio/{userId}` - Get holdings
- `GET /transactions/{userId}` - Get transaction history

### Error Handling
All API errors are intercepted and converted to user-friendly messages:
- 400: "Invalid request. Please check your input."
- 401: "Authentication required. Please log in again."
- 403: "Access denied."
- 404: "Resource not found."
- 500: "Server error. Please try again later."
- Network error: "Network error. Please check your internet connection."

## State Management

### Context API
- **AuthContext**: User authentication, localStorage persistence
- **NotificationContext**: Toast notifications (6s auto-hide)

### Local Storage
- Key: `trading_platform_user_id`
- Persists user session across page refreshes
- Cleared on logout

## Routing

### Public Routes
- `/` - Landing page
- `/signup` - User signup
- `/login` - User login

### Protected Routes (requires authentication)
- `/dashboard` - User dashboard
- `/wallet` - Wallet management
- `/market` - Browse securities
- `/trade` - Execute trades
- `/portfolio` - View holdings
- `/transactions` - Transaction history

### Route Guards
`ProtectedRoute` component checks authentication and redirects to `/login` if not authenticated.

## Testing

### End-to-End Testing
See [E2E_TESTING.md](./E2E_TESTING.md) for complete test flow (20 test cases).

### Manual Testing Checklist
1. Sign up new user
2. Deposit $10,000 USD
3. Buy $1,000 of AAPL
4. View portfolio
5. Sell shares
6. Check transaction history
7. Logout and login again

### Browser Console
- Watch for network errors
- Check localStorage: `localStorage.getItem('trading_platform_user_id')`

## Performance

### Bundle Size
- **Production build**: ~617 KB (minified)
- **Gzipped**: ~191 KB
- **Initial load**: < 2s on 3G

### Optimization
- Code splitting recommended for larger apps
- Static assets cached for 1 year
- Gzip compression enabled

### Suggested Improvements
```javascript
// vite.config.ts - Add manual chunks
build: {
  rollupOptions: {
    output: {
      manualChunks: {
        'mui': ['@mui/material', '@mui/icons-material'],
        'router': ['react-router-dom'],
        'vendor': ['react', 'react-dom']
      }
    }
  }
}
```

## Environment Variables

### Development (.env.development)
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=Trading Platform
```

### Production (.env.production)
```bash
VITE_API_BASE_URL=/api/v1
VITE_APP_NAME=Trading Platform
```

## Nginx Configuration

### SPA Routing
All routes fallback to `index.html` for client-side routing.

### API Proxy
`/api/*` requests are proxied to `api-gateway:8080`

### Security Headers
- X-Frame-Options: SAMEORIGIN
- X-Content-Type-Options: nosniff
- X-XSS-Protection: 1; mode=block

### Caching
Static assets (js, css, images) cached for 1 year.

## Troubleshooting

### Port 3000 already in use
```bash
# Find and kill process
lsof -ti:3000 | xargs kill -9
```

### CORS errors
- Verify API Gateway CORS configuration
- Check `api-gateway` allows http://localhost:3000

### Build fails
```bash
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Docker build fails
```bash
docker system prune -a
docker-compose build --no-cache frontend
```

## Development Guidelines

### Component Pattern
```typescript
import React, { useState } from 'react'
import { Box, Typography } from '@mui/material'

interface Props {
  title: string
}

const MyComponent: React.FC<Props> = ({ title }) => {
  const [state, setState] = useState('')

  return (
    <Box>
      <Typography variant="h4">{title}</Typography>
    </Box>
  )
}

export default MyComponent
```

### Service Pattern
```typescript
import apiClient from '../api'

export const myService = {
  getData: async (id: string): Promise<Data> => {
    const response = await apiClient.get<Data>(`/endpoint/${id}`)
    return response.data
  }
}
```

### Error Handling
```typescript
try {
  await myService.getData(id)
  showNotification('Success!', 'success')
} catch (error: any) {
  const message = error?.message || 'Operation failed'
  showNotification(message, 'error')
}
```

## Browser Support

- Chrome 120+
- Firefox 120+
- Safari 17+
- Edge 120+

## License

Part of the Fractional Stock Trading Platform project.

## Contributing

This frontend was built following the multi-role workflow:
- Product Owner: Requirements and user stories
- Senior Engineer: Architecture and design decisions
- Developer: Implementation
- Q/A: Testing and validation

---

**Built with Claude Code**
