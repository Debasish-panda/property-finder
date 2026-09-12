import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';
import { RentalProperty } from '../property.model';
import { PropertyPopupComponent } from '../property-popup/property-popup.component';

@Component({
  selector: 'app-map-pin',
  imports: [PropertyPopupComponent],
  templateUrl: './map-pin.component.html',
  styleUrl: './map-pin.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MapPinComponent {
  readonly property = input.required<RentalProperty>();
  protected readonly hovered = signal(false);
}
