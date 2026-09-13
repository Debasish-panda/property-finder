import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RentalProperty } from './property.model';

export interface MapViewport {
  north: number; south: number; east: number; west: number;
  latitude: number; longitude: number; zoom: number;
}

export interface MapListingResponse {
  properties: RentalProperty[];
  total: number;
  viewport: MapViewport;
}

export interface BrokerDashboardSummary {
  totalListings: number;
  availableListings: number;
  rentedListings: number;
  occupancyRate: number;
  commissionEarned: number;
  commissionMonths: number;
}

export interface BrokerDashboardResponse {
  properties: RentalProperty[];
  summary: BrokerDashboardSummary;
}

@Injectable({ providedIn: 'root' })
export class PropertyApiService {
  private readonly http = inject(HttpClient);

  getProperties(viewport: MapViewport): Observable<MapListingResponse> {
    let params = new HttpParams();
    Object.entries(viewport).forEach(([key, value]) => params = params.set(key, value));
    return this.http.get<MapListingResponse>('/api/properties', { params });
  }

  getBrokerDashboard(): Observable<BrokerDashboardResponse> {
    return this.http.get<BrokerDashboardResponse>('/api/broker/dashboard');
  }
}