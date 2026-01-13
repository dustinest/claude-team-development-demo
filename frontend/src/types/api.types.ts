import { Currency, OrderType, SecurityType, TransactionType, TradeType, TradeStatus } from './enums'

// ============================================
// Auth & User Types
// ============================================

export interface SignupRequest {
  email: string
  username: string
  phoneNumber: string
}

export interface SignupResponse {
  userId: string
  email: string
  username: string
  phoneNumber: string
}

export interface User {
  userId: string
  email: string
  username: string
  phoneNumber: string
  createdAt: string // ISO 8601 timestamp
}

// ============================================
// Wallet Types
// ============================================

export interface WalletBalance {
  userId: string
  currency: Currency
  balance: string // BigDecimal as string to preserve precision
}

export interface DepositRequest {
  currency: Currency
  amount: number
}

export interface WithdrawRequest {
  currency: Currency
  amount: number
}

export interface ExchangeRequest {
  fromCurrency: Currency
  toCurrency: Currency
  amount: number
}

export interface ExchangeResponse {
  fromBalance: string
  toBalance: string
  convertedAmount: string
  exchangeRate: string
  fee: string
}

// ============================================
// Securities Types
// ============================================

export interface Security {
  symbol: string
  name: string
  type: SecurityType
  currentPrice: string // BigDecimal as string
  openPrice: string
  highPrice: string
  lowPrice: string
  lastUpdated: string // ISO 8601 timestamp
}

export interface SecurityPrice {
  symbol: string
  price: string // BigDecimal as string
}

// ============================================
// Trading Types
// ============================================

export interface TradeRequest {
  userId: string
  symbol: string
  currency: Currency
  orderType: OrderType
  amount?: number // Used when orderType is BY_AMOUNT
  quantity?: number // Used when orderType is BY_QUANTITY
}

export interface TradeResponse {
  id: string // UUID
  userId: string // UUID
  symbol: string
  tradeType: TradeType
  orderType: OrderType
  quantity: string // BigDecimal as string
  pricePerUnit: string // BigDecimal as string
  currency: Currency
  totalAmount: string // BigDecimal as string
  fees: string // BigDecimal as string
  status: TradeStatus
  createdAt: string // ISO 8601 timestamp
  completedAt?: string // ISO 8601 timestamp, optional
}

// ============================================
// Portfolio Types
// ============================================

export interface Holding {
  id: string // UUID
  userId: string // UUID
  symbol: string
  quantity: string // BigDecimal as string
  averagePrice: string // BigDecimal as string
  currency: Currency
  updatedAt: string // ISO 8601 timestamp
}

// ============================================
// Transaction Types
// ============================================

export interface Transaction {
  id: string // UUID
  userId: string // UUID
  type: TransactionType
  currency: Currency
  amount: string // BigDecimal as string
  fees: string // BigDecimal as string
  relatedEntityId?: string // UUID, optional
  metadata?: string // JSON string, optional
  createdAt: string // ISO 8601 timestamp
}

// ============================================
// Error Response
// ============================================

export interface ErrorResponse {
  error: string
  message?: string
  timestamp?: string
}
