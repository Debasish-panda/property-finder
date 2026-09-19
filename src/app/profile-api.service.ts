import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProfileData {
  profile: { id: number; fullName: string; email?: string; mobile?: string; role: 'USER' | 'BROKER'; enabled: boolean; createdAt: string };
  subscription: { active: boolean; status?: string; startsAt?: string; expiresAt?: string; plan?: { code: string; name: string; amount: number; currency: string; validityDays: number; coverageInfo: string; maxPropertyListings: number; dailyLoadLimit: number } };
  usage: { date: string; dailyLoadsUsed: number; dailyLoadLimit: number; remainingLoads: number; propertyListingLimit: number };
  listingCount: number;
  session: { expiresAt: string; remainingSeconds: number };
}

@Injectable({ providedIn: 'root' })
export class ProfileApiService {
  private readonly http = inject(HttpClient);
  getProfile(): Observable<ProfileData> { return this.http.get<ProfileData>('/api/profile'); }
}
