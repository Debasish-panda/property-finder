import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  imports: [DecimalPipe, RouterLink, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  private readonly router = inject(Router);
  private readonly currentUrl = signal(this.router.url);
  protected readonly isAuthPage = computed(
    () =>
      this.currentUrl().startsWith('/login') ||
      this.currentUrl().startsWith('/signup') ||
      this.currentUrl().startsWith('/map') ||
      this.currentUrl().startsWith('/dashboard'),
  );
  protected readonly menuOpen = signal(false);
  protected readonly selectedFilter = signal('All homes');
  protected readonly navLinks = [
    { label: 'Explore', href: '#explore' },
    { label: 'For Brokers', href: '#brokers' },
    { label: 'How It Works', href: '#how-it-works' },
    { label: 'Pricing', href: '#pricing' },
  ];
  protected readonly filters = ['All homes', '1 BHK', '2 BHK', '3 BHK', 'Under ₹20K', 'Under ₹30K'];
  protected readonly featureCards = [
    {
      icon: '⌖',
      title: 'Map-first search',
      text: 'Explore available homes visually instead of scrolling through endless listings.',
    },
    {
      icon: '↔',
      title: 'Compare prices',
      text: 'See listings from different sources and quickly spot the best available price.',
    },
    {
      icon: '◌',
      title: 'Discover better areas',
      text: 'Understand rental prices and explore neighbourhoods that fit your budget.',
    },
    {
      icon: '✓',
      title: 'Fresh listings',
      text: 'Know when a property was last updated or verified before you reach out.',
    },
  ];
  protected readonly renterBenefits = [
    'Search by locality',
    'Search by budget',
    'Compare prices',
    'Explore nearby areas',
    'Save properties',
  ];
  protected readonly brokerBenefits = [
    {
      number: '01',
      title: 'Easy property management',
      text: 'Keep your inventory visible, current and easy to share.',
    },
    {
      number: '02',
      title: 'More qualified leads',
      text: 'Meet renters who already know what they are looking for.',
    },
    {
      number: '03',
      title: 'Affordable plans',
      text: 'Grow at a price that makes sense for your business.',
    },
  ];
  protected readonly steps = [
    { number: '01', title: 'Search', text: 'Enter a locality, landmark or area.' },
    { number: '02', title: 'Explore', text: 'Explore available rentals directly on the map.' },
    {
      number: '03',
      title: 'Compare',
      text: 'Compare prices and sources before contacting the property.',
    },
  ];
  protected readonly pricingPlans = [
    {
      id: 'small',
      name: 'Small broker',
      loads: 100,
      price: 500,
      description: 'For focused daily activity.',
    },
    {
      id: 'medium',
      name: 'Medium broker',
      loads: 400,
      price: 1000,
      description: 'For a growing property book.',
    },
    {
      id: 'large',
      name: 'Large broker',
      loads: 1000,
      price: 5000,
      description: 'For high-volume operations.',
    },
  ] as const;
  protected readonly pricingOpen = signal(false);
  protected readonly dailyLoads = signal(100);
  protected readonly selectedPlan = computed(
    () => this.pricingPlans.find((plan) => this.dailyLoads() <= plan.loads) ?? this.pricingPlans[2],
  );
  protected readonly selectedPrice = computed(() => this.selectedPlan().price);

  constructor() {
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => this.currentUrl.set(event.urlAfterRedirects));
  }

  protected selectFilter(filter: string): void {
    this.selectedFilter.set(filter);
  }
  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
  protected openPricing(): void {
    this.pricingOpen.set(true);
  }
  protected closePricing(): void {
    this.pricingOpen.set(false);
  }
  protected selectPlan(loads: number): void {
    this.dailyLoads.set(loads);
  }
  protected updateDailyLoads(event: Event): void {
    const input = event.target as HTMLInputElement;
    const loads = Number.parseInt(input.value, 10);
    this.dailyLoads.set(Number.isFinite(loads) && loads > 0 ? Math.min(loads, 1000) : 1);
  }
}
