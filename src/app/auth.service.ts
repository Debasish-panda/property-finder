import { isPlatformBrowser } from '@angular/common';
import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly storageKey = 'rentmap-authenticated';
  readonly isAuthenticated = signal(this.readSession());

  login(username: string, password: string): boolean {
    const validCredentials = username.trim().toLowerCase() === 'admin' && password === 'password';
    if (validCredentials) {
      this.isAuthenticated.set(true);
      if (isPlatformBrowser(this.platformId)) sessionStorage.setItem(this.storageKey, 'true');
    }
    return validCredentials;
  }

  logout(): void {
    this.isAuthenticated.set(false);
    if (isPlatformBrowser(this.platformId)) sessionStorage.removeItem(this.storageKey);
  }

  private readSession(): boolean {
    return isPlatformBrowser(this.platformId) && sessionStorage.getItem(this.storageKey) === 'true';
  }
}