import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProfileApiService, ProfileData } from './profile-api.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, DecimalPipe, RouterLink],
  template: `
    <main class="profile-page">
      <header class="profile-header">
        <a routerLink="/" class="brand">Rent<span>Map</span></a
        ><a routerLink="/map">Back to map</a>
      </header>
      @if (loading()) {
        <p>Loading profile...</p>
      }
      @if (error()) {
        <p role="alert">{{ error() }}</p>
      }
      @if (profile(); as data) {
        <section class="profile-card">
          <p class="eyebrow">Your account</p>
          <h1>{{ data.profile.fullName }}</h1>
          <p>{{ data.profile.email || data.profile.mobile }}</p>
          <p>Role: {{ data.profile.role }}</p>
          <p>Member since: {{ data.profile.createdAt | date }}</p>
        </section>
        <section class="profile-grid">
          <article class="profile-card">
            <p class="eyebrow">Current plan</p>
            @if (data.subscription.active && data.subscription.plan; as plan) {
              <h2>{{ plan.name }}</h2>
              <strong>₹{{ plan.amount | number }}</strong>
              <p>{{ plan.coverageInfo }}</p>
              <p>
                Valid for {{ plan.validityDays }} days · expires
                {{ data.subscription.expiresAt | date }}
              </p>
              <p>Listings: {{ data.listingCount }}/{{ plan.maxPropertyListings }}</p>
            } @else {
              <h2>No active plan</h2>
              <p>Choose a plan to unlock broker features.</p>
            }
          </article>
          <article class="profile-card">
            <p class="eyebrow">Today’s usage</p>
            <h2>{{ data.usage.remainingLoads | number }} loads remaining</h2>
            <p>
              {{ data.usage.dailyLoadsUsed }} of {{ data.usage.dailyLoadLimit }} daily loads used
            </p>
            <p>
              Property listings: {{ data.listingCount }}/{{
                data.usage.propertyListingLimit || '—'
              }}
            </p>
          </article>
        </section>
        <p class="session-note">Session expires {{ data.session.expiresAt | date: 'medium' }}</p>
      }
    </main>
  `,
  styles: [
    `
      .profile-page {
        max-width: 960px;
        margin: 0 auto;
        padding: 2rem;
      }
      .profile-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
      }
      .brand {
        font-size: 1.5rem;
        font-weight: 700;
        text-decoration: none;
      }
      .brand span {
        color: #c77b48;
      }
      .profile-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 1rem;
      }
      .profile-card {
        padding: 1.5rem;
        border: 1px solid #e6e0d8;
        border-radius: 16px;
        background: #fff;
        box-shadow: 0 8px 30px #352d2510;
      }
      .eyebrow {
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.12em;
        color: #9a765d;
      }
      .session-note {
        color: #756d67;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileComponent implements OnInit {
  private readonly api = inject(ProfileApiService);
  private readonly auth = inject(AuthService);
  protected readonly profile = signal<ProfileData | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  ngOnInit(): void {
    this.api.getProfile().subscribe({
      next: (value) => {
        this.profile.set(value);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(error?.error?.message ?? 'Unable to load profile.');
        this.loading.set(false);
      },
    });
  }
}
