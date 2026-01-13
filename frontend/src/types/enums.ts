export enum Currency {
  USD = 'USD',
  EUR = 'EUR',
  GBP = 'GBP',
}

export enum OrderType {
  BY_AMOUNT = 'BY_AMOUNT',
  BY_QUANTITY = 'BY_QUANTITY',
}

export enum TradeType {
  BUY = 'BUY',
  SELL = 'SELL',
}

export enum TradeStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export enum SecurityType {
  STOCK = 'STOCK',
  STOCK_INDEX = 'STOCK_INDEX',
  BOND_INDEX = 'BOND_INDEX',
}

export enum TransactionType {
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  BUY = 'BUY',
  SELL = 'SELL',
  CURRENCY_EXCHANGE = 'CURRENCY_EXCHANGE',
}
