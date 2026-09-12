import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { RentalProperty } from '../property.model';

@Component({
  selector: 'app-property-popup',
  imports: [NgOptimizedImage],
  templateUrl: './property-popup.component.html',
  styleUrl: './property-popup.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PropertyPopupComponent {
  readonly property = input.required<RentalProperty>();
}
