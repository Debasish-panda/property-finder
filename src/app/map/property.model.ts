export interface RentalProperty {
  id: number;
  title: string;
  locality: string;
  rent: string;
  type: string;
  bedrooms: string;
  position: { top: string; left: string };
  amenities: string[];
  verified: boolean;
  latitude: number;
  longitude: number;
  status: 'available' | 'rented';
  availableFrom: string;
  rentedOn?: string;
  negotiable: boolean;
  durationDays: number;
  commissionEarned: number;
}
