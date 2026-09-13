import { ChangeDetectionStrategy, Component, ElementRef, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RentalProperty } from './property.model';
import { MapViewport, PropertyApiService } from './property-api.service';
import { AuthService } from '../auth.service';

interface ImportedProperty {
  address: string;
  buildingName: string;
  houseNumber: string;
  ownerName: string;
  ownerMobile: string;
  bhkType: string;
  rent: string;
  deposit: string;
  furnishing: string;
  amenities: string;
  maintenanceCharges: string;
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
  protected readonly isBroker = this.auth.isBroker;
  protected readonly properties = signal<RentalProperty[]>([]);
  protected readonly visibleCount = signal(0);
  protected readonly loading = signal(false);
  protected readonly importedProperties = signal<ImportedProperty[]>([]);
  protected readonly importMessage = signal('');
  protected readonly importError = signal('');
  private map?: import('leaflet').Map;
  private markerLayer?: import('leaflet').LayerGroup;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) void this.createMap();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private async createMap(): Promise<void> {
    const L = await import('leaflet');
    const mapElement = this.elementRef.nativeElement.querySelector('#rental-map') as HTMLElement | null;
    if (!mapElement) return;
    this.map = L.map(mapElement, { zoomControl: false }).setView([12.9716, 77.5946], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors', maxZoom: 19,
    }).addTo(this.map);
    L.control.zoom({ position: 'topright' }).addTo(this.map);
    this.markerLayer = L.layerGroup().addTo(this.map);
    this.map.on('moveend', () => this.loadVisibleProperties());
    this.loadVisibleProperties();
  }

  private loadVisibleProperties(): void {
    if (!this.map) return;
    const bounds = this.map.getBounds();
    const viewport: MapViewport = {
      north: bounds.getNorth(), south: bounds.getSouth(), east: bounds.getEast(), west: bounds.getWest(),
      latitude: this.map.getCenter().lat, longitude: this.map.getCenter().lng, zoom: this.map.getZoom(),
    };
    this.loading.set(true);
    this.propertyApi.getProperties(viewport).subscribe({
      next: (response) => {
        this.properties.set(response.properties);
        this.visibleCount.set(response.total);
        this.renderMarkers(response.properties);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private renderMarkers(properties: RentalProperty[]): void {
    if (!this.markerLayer) return;
    this.markerLayer.clearLayers();
    void import('leaflet').then((L) => properties.forEach((property) => L.marker(
      [property.latitude, property.longitude], { title: property.title },
    ).bindPopup(`<strong>${property.title}</strong><br>${property.locality} · ${property.rent}/month`).addTo(this.markerLayer!)));
  }

  protected logout(): void {
    this.auth.logout();
  }

  protected onPropertyFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.importMessage.set('');
    this.importError.set('');
    const reader = new FileReader();
    reader.onload = async () => {
      try {
        const XLSX = await import('xlsx');
        const workbook = XLSX.read(reader.result, { type: 'array' });
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
        const rows = XLSX.utils.sheet_to_json<Record<string, unknown>>(firstSheet, { defval: '' });
        const imported = rows.map((row) => this.mapImportedProperty(row)).filter((property): property is ImportedProperty => property !== null);
        if (!imported.length) throw new Error('No property rows were found. Check that the first row contains column headings.');
        this.importedProperties.set(imported);
        this.importMessage.set(`${imported.length} propert${imported.length === 1 ? 'y' : 'ies'} imported successfully.`);
      } catch (error) {
        this.importError.set(error instanceof Error ? error.message : 'The file could not be imported.');
      } finally {
        input.value = '';
      }
    };
    reader.onerror = () => this.importError.set('The file could not be read.');
    reader.readAsArrayBuffer(file);
  }

  private mapImportedProperty(row: Record<string, unknown>): ImportedProperty | null {
    const values = new Map(Object.entries(row).map(([key, value]) => [this.normalizeHeader(key), String(value ?? '').trim()]));
    const get = (...headers: string[]): string => headers.map((header) => values.get(this.normalizeHeader(header)) ?? '').find(Boolean) ?? '';
    const address = get('address', 'locality', 'location');
    const rent = get('rent', 'monthly rent', 'monthly rental');
    if (!address && !rent) return null;
    return {
      address,
      buildingName: get('building name', 'building'),
      houseNumber: get('house number', 'house no', 'flat number', 'flat no'),
      ownerName: get('owner name', 'owner'),
      ownerMobile: get('owner mobile number', 'owner mobile', 'owner phone', 'mobile number'),
      bhkType: get('bhk type', 'bhk', 'bedrooms'),
      rent,
      deposit: get('deposit', 'security deposit'),
      furnishing: get('furnishing', 'furnished status'),
      amenities: get('amenities', 'building amenities'),
      maintenanceCharges: get('maintenance charges', 'maintenance', 'maintenance charge'),
    };
  }

  private normalizeHeader(header: string): string {
    return header.toLowerCase().replace(/[^a-z0-9]/g, '');
  }
}
