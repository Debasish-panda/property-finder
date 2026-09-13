import { isPlatformBrowser } from '@angular/common';
import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';

export type UserRole = 'user' | 'broker';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly storageKey = 'rentmap-authenticated';
  private readonly roleKey = 'rentmap-role';
  readonly isAuthenticated = signal(this.readSession());
  readonly role = signal<UserRole | null>(this.readRole());
  readonly isBroker = computed(() => this.role() === 'broker');

  login(username: string, password: string): boolean {
    const normalizedUsername = username.trim().toLowerCase();
    const validCredentials = (normalizedUsername === 'admin' || normalizedUsername === 'broker') && password === 'password';
    if (validCredentials) {
      this.isAuthenticated.set(true);
      const role: UserRole = 'broker';
      this.role.set(role);
      if (isPlatformBrowser(this.platformId)) {
        sessionStorage.setItem(this.storageKey, 'true');
        sessionStorage.setItem(this.roleKey, role);
      }
    }
    return validCredentials;
  }

  logout(): void {
    this.isAuthenticated.set(false);
    this.role.set(null);
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem(this.storageKey);
      sessionStorage.removeItem(this.roleKey);
    }
  }

  private readSession(): boolean {
    return isPlatformBrowser(this.platformId) && sessionStorage.getItem(this.storageKey) === 'true';
  }

  private readRole(): UserRole | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return sessionStorage.getItem(this.roleKey) === 'broker' ? 'broker' : null;
  }
}