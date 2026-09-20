import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';
import { BrokerDashboardResponse, PropertyApiService } from '../map/property-api.service';
import { RentalProperty } from '../map/property.model';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  private readonly propertyApi = inject(PropertyApiService);
  private readonly auth = inject(AuthService);
  protected readonly dashboard = signal<BrokerDashboardResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly availableProperties = computed(
    () => this.dashboard()?.properties.filter((property) => property.status === 'available') ?? [],
  );
  protected readonly rentedProperties = computed(
    () => this.dashboard()?.properties.filter((property) => property.status === 'rented') ?? [],
  );

  ngOnInit(): void {
    this.propertyApi.getBrokerDashboard().subscribe({
      next: (response) => {
        this.dashboard.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('We could not load your listing summary. Please try again.');
        this.loading.set(false);
      },
    });
  }

  protected statusLabel(property: RentalProperty): string {
    return property.status === 'rented' ? 'Rented out' : 'Available';
  }

  protected logout(): void {
    this.auth.logout();
  }
}
