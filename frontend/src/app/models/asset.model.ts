export type AssetStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ASSIGNED';

export interface Asset {
  id: number;
  name: string;
  code: string;
  status: AssetStatus;
  purchaseDate: string;
  purchaseValue: number;
  currentValue: number;
  categoryId: number;
  usefulLifeYears: number;
}

export interface AssetPage {
  content: Asset[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface DepreciationInfo {
  assetId: number;
  purchaseValue: number;
  currentValue: number;
  annualDepreciation: number;
  accumulatedDepreciation: number;
  yearsElapsed: number;
  remainingLife: number;
  fullyDepreciated: boolean;
}
