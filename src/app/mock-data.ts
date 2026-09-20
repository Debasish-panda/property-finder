import type { AuthResponse, SignupRequest, UserRole } from './auth.service';
import type { CreateOrderResponse, PricingPlan } from './billing-api.service';
import type { ProfileData } from './profile-api.service';
import type {
  BrokerDashboardResponse,
  MapListingResponse,
  MapViewport,
} from './map/property-api.service';
import type { RentalProperty } from './map/property.model';

// make false while pushing the code
export const isMock = false;

export const mockMapConfig = {
  provider: 'openstreetmap',
  tileUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  attribution: '&copy; OpenStreetMap contributors',
  apiKey: '',
};

export const mockAuthResponse: AuthResponse = {
  token: 'mock-property-finder-token',
  expiresIn: 86_400,
  userId: 101,
  fullName: 'Aarav Mehta',
  role: 'BROKER',
};

export const mockOtpResponse = { message: 'Mock OTP sent. Use any six-digit code to continue.' };

export const mockSignupResponse = (request: SignupRequest): AuthResponse => ({
  ...mockAuthResponse,
  fullName: request.fullName,
  role: request.role as UserRole,
});

export const mockPlans: PricingPlan[] = [
  {
    id: 1,
    code: 'STARTER',
    name: 'Starter',
    amount: 499,
    amountPaise: 49_900,
    currency: 'INR',
    validityDays: 30,
    coverageInfo: 'For getting started with local listings',
    maxPropertyListings: 10,
    dailyLoadLimit: 50,
  },
  {
    id: 2,
    code: 'GROWTH',
    name: 'Growth',
    amount: 999,
    amountPaise: 99_900,
    currency: 'INR',
    validityDays: 90,
    coverageInfo: 'More listings and higher daily limits',
    maxPropertyListings: 50,
    dailyLoadLimit: 250,
  },
];

export const mockProperties: RentalProperty[] = [
  {
    id: 1,
    title: 'Sunlit 2 BHK near Indiranagar',
    locality: 'Indiranagar',
    rent: '32000',
    type: 'Apartment',
    bedrooms: '2 BHK',
    position: { top: '34%', left: '64%' },
    amenities: ['Parking', 'Power backup', 'Gym'],
    verified: true,
    latitude: 12.9784,
    longitude: 77.6408,
    status: 'available',
    availableFrom: '2026-09-25',
    negotiable: true,
    durationDays: 180,
    commissionEarned: 0,
  },
  {
    id: 2,
    title: 'Quiet 1 BHK in Koramangala',
    locality: 'Koramangala',
    rent: '24000',
    type: 'Apartment',
    bedrooms: '1 BHK',
    position: { top: '58%', left: '48%' },
    amenities: ['Balcony', 'Security'],
    verified: true,
    latitude: 12.9352,
    longitude: 77.6245,
    status: 'available',
    availableFrom: '2026-10-01',
    negotiable: false,
    durationDays: 365,
    commissionEarned: 0,
  },
  {
    id: 3,
    title: 'Spacious family home in HSR Layout',
    locality: 'HSR Layout',
    rent: '41000',
    type: 'Independent house',
    bedrooms: '3 BHK',
    position: { top: '72%', left: '35%' },
    amenities: ['Garden', 'Parking', 'Pet friendly'],
    verified: false,
    latitude: 12.9121,
    longitude: 77.6446,
    status: 'rented',
    availableFrom: '',
    rentedOn: '2026-08-15',
    negotiable: true,
    durationDays: 365,
    commissionEarned: 18_000,
  },
  {
    id: 4,
    title: 'Modern studio in Whitefield',
    locality: 'Whitefield',
    rent: '19000',
    type: 'Studio',
    bedrooms: '1 BHK',
    position: { top: '29%', left: '86%' },
    amenities: ['Furnished', 'Lift'],
    verified: true,
    latitude: 12.9698,
    longitude: 77.7499,
    status: 'available',
    availableFrom: '2026-09-28',
    negotiable: true,
    durationDays: 90,
    commissionEarned: 0,
  },
];

export const mockProfile: ProfileData = {
  profile: {
    id: 101,
    fullName: 'Aarav Mehta',
    email: 'aarav@example.com',
    mobile: '+91 98765 43210',
    role: 'BROKER',
    enabled: true,
    createdAt: '2025-11-12T10:00:00Z',
  },
  subscription: {
    active: true,
    status: 'ACTIVE',
    startsAt: '2026-09-01T10:00:00Z',
    expiresAt: '2026-11-30T10:00:00Z',
    plan: mockPlans[1],
  },
  usage: {
    date: '2026-09-20',
    dailyLoadsUsed: 42,
    dailyLoadLimit: 250,
    remainingLoads: 208,
    propertyListingLimit: 50,
  },
  listingCount: 3,
  session: { expiresAt: '2026-09-21T10:00:00Z', remainingSeconds: 86_400 },
};

export const mockMapResponse = (viewport: MapViewport): MapListingResponse => ({
  properties: mockProperties,
  total: mockProperties.length,
  viewport,
});

export const mockDashboard: BrokerDashboardResponse = {
  properties: mockProperties,
  summary: {
    totalListings: 4,
    availableListings: 3,
    rentedListings: 1,
    occupancyRate: 25,
    commissionEarned: 18_000,
    commissionMonths: 1,
  },
};

export const mockOrder = (planCode: string, paymentMethod: string): CreateOrderResponse => ({
  orderId: `mock_order_${planCode.toLowerCase()}`,
  amount: mockPlans.find((plan) => plan.code === planCode)?.amountPaise ?? mockPlans[0].amountPaise,
  currency: 'INR',
  keyId: 'mock_razorpay_key',
  planCode,
  paymentMethod,
});
