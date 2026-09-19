import { Injectable, inject, PLATFORM_ID, computed, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export type UserRole = 'USER' | 'BROKER';
export interface AuthResponse { token: string; expiresIn: number; userId: number; fullName: string; role: UserRole; }
export interface SignupRequest { fullName: string; email?: string; mobile?: string; password: string; role: 'USER' | 'BROKER'; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient); private readonly router = inject(Router); private readonly platformId = inject(PLATFORM_ID);
  private readonly tokenKey = 'property-finder-access-token'; private readonly roleKey = 'property-finder-role';
  readonly token = signal(this.read(this.tokenKey)); readonly role = signal<UserRole | null>(this.read(this.roleKey) as UserRole | null);
  readonly isAuthenticated = computed(() => !!this.token()); readonly isBroker = computed(() => this.role() === 'BROKER');
  login(identifier: string, password: string): Observable<AuthResponse> { return this.http.post<AuthResponse>('/api/auth/login/password', { identifier, password }).pipe(tap(response => this.store(response))); }
  requestOtp(identifier: string): Observable<{ message: string }> { return this.http.post<{ message: string }>('/api/auth/login/otp/request', { identifier }); }
  verifyOtp(identifier: string, code: string): Observable<AuthResponse> { return this.http.post<AuthResponse>('/api/auth/login/otp/verify', { identifier, code }).pipe(tap(response => this.store(response))); }
  signup(request: SignupRequest): Observable<AuthResponse> { return this.http.post<AuthResponse>('/api/auth/signup', request); }
  logout(): void { const token = this.token(); if (token) this.http.post('/api/auth/logout', {}, { headers: { Authorization: `Bearer ${token}` } }).subscribe({ complete: () => this.clear(), error: () => this.clear() }); else this.clear(); }
  clear(): void { this.token.set(null); this.role.set(null); if (isPlatformBrowser(this.platformId)) { sessionStorage.removeItem(this.tokenKey); sessionStorage.removeItem(this.roleKey); } void this.router.navigateByUrl('/login'); }
  private store(response: AuthResponse): void { this.token.set(response.token); this.role.set(response.role); if (isPlatformBrowser(this.platformId)) { sessionStorage.setItem(this.tokenKey, response.token); sessionStorage.setItem(this.roleKey, response.role); } }
  private read(key: string): string | null { return isPlatformBrowser(this.platformId) ? sessionStorage.getItem(key) : null; }
}
