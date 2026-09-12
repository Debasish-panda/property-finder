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

@Injectable({ providedIn: 'root' })
export class PropertyApiService {
  private readonly http = inject(HttpClient);

  getProperties(viewport: MapViewport): Observable<MapListingResponse> {
    let params = new HttpParams();
    Object.entries(viewport).forEach(([key, value]) => params = params.set(key, value));
    return this.http.get<MapListingResponse>('/api/properties', { params });
  }
}