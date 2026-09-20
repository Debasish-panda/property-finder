import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { DecimalPipe, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RentalProperty } from './property.model';
import { MapViewport, PropertyApiService } from './property-api.service';
import { AuthService } from '../auth.service';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { isMock, mockMapConfig } from '../mock-data';

interface MapConfig {
  provider: string;
  tileUrl: string;
  attribution: string;
  apiKey: string;
}
@Component({
  selector: 'app-map-view',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './map-view.component.html',
  styleUrl: './map-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MapViewComponent implements OnInit, OnDestroy {
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly propertyApi = inject(PropertyApiService);
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  protected readonly isBroker = this.auth.isBroker;
  protected readonly properties = signal<RentalProperty[]>([]);
  protected readonly visibleCount = signal(0);
  protected readonly loading = signal(false);
  protected readonly importMessage = signal('');
  protected readonly importError = signal('');
  private map?: import('leaflet').Map;
  private markerLayer?: import('leaflet').LayerGroup;
  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId))
      (isMock ? of(mockMapConfig) : this.http.get<MapConfig>('/api/config/map')).subscribe({
        next: (config) => void this.createMap(config),
        error: () => this.importError.set('Map configuration could not be loaded.'),
      });
  }
  ngOnDestroy(): void {
    this.map?.remove();
  }
  private async createMap(config: MapConfig): Promise<void> {
    const L = await import('leaflet');
    const el = this.elementRef.nativeElement.querySelector('#rental-map') as HTMLElement | null;
    if (!el || !config.tileUrl) return;
    this.map = L.map(el, { zoomControl: false }).setView([12.9716, 77.5946], 12);
    const tileUrl = config.apiKey
      ? `${config.tileUrl}${config.tileUrl.includes('?') ? '&' : '?'}key=${encodeURIComponent(config.apiKey)}`
      : config.tileUrl;
    L.tileLayer(tileUrl, { attribution: config.attribution, maxZoom: 19 }).addTo(this.map);
    L.control.zoom({ position: 'topright' }).addTo(this.map);
    this.markerLayer = L.layerGroup().addTo(this.map);
    this.map.on('moveend', () => this.loadVisibleProperties());
    this.loadVisibleProperties();
  }
  private loadVisibleProperties(): void {
    if (!this.map) return;
    const b = this.map.getBounds();
    const viewport: MapViewport = {
      north: b.getNorth(),
      south: b.getSouth(),
      east: b.getEast(),
      west: b.getWest(),
      latitude: this.map.getCenter().lat,
      longitude: this.map.getCenter().lng,
      zoom: this.map.getZoom(),
    };
    this.loading.set(true);
    this.propertyApi.getProperties(viewport).subscribe({
      next: (r) => {
        this.properties.set(r.properties);
        this.visibleCount.set(r.total);
        this.renderMarkers(r.properties);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
  private renderMarkers(properties: RentalProperty[]): void {
    if (!this.markerLayer) return;
    this.markerLayer.clearLayers();
    void import('leaflet').then((L) =>
      properties.forEach((p) =>
        L.marker([p.latitude, p.longitude], { title: p.title })
          .bindPopup(`<strong>${p.title}</strong><br>${p.locality} · ${p.rent}/month`)
          .addTo(this.markerLayer!),
      ),
    );
  }
  protected logout(): void {
    this.auth.logout();
  }
  protected onPropertyFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.importMessage.set('Uploading and validating properties...');
    this.importError.set('');
    this.propertyApi.importProperties(file).subscribe({
      next: (r) => {
        this.importMessage.set(
          `${r.imported} propert${r.imported === 1 ? 'y' : 'ies'} imported successfully.`,
        );
        this.loadVisibleProperties();
      },
      error: (e) => {
        this.importMessage.set('');
        this.importError.set(e?.error?.message ?? 'The file could not be imported.');
      },
      complete: () => (input.value = ''),
    });
  }
}
