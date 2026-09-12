import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  protected readonly loginMode = signal<'otp' | 'password'>('password');
  protected readonly submitted = signal(false);
  protected readonly loginError = signal(false);
  protected identifier = '';
  protected secret = '';

  constructor(private readonly router: Router, private readonly auth: AuthService) {}

  protected setMode(mode: 'otp' | 'password'): void {
    this.loginMode.set(mode);
    this.submitted.set(false);
    this.loginError.set(false);
    this.secret = '';
  }

  protected submit(): void {
    const authenticated = this.loginMode() === 'password' && this.auth.login(this.identifier, this.secret);
    this.loginError.set(!authenticated);
    this.submitted.set(authenticated);
    if (authenticated) void this.router.navigateByUrl('/map');
  }

  protected goHome(): void {
    void this.router.navigateByUrl('/');
  }
}
