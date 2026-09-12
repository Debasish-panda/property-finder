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
}
