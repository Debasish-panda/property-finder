import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-signup',
  imports: [FormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SignupComponent {
  protected readonly submitted = signal(false);
  protected name = '';
  protected email = '';
  protected mobile = '';
  protected userType = 'user';
  protected password = '';

  protected submit(): void {
    this.submitted.set(true);
  }
}
