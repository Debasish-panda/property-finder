import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({ selector: 'app-login', imports: [FormsModule, RouterLink], templateUrl: './login.component.html', styleUrl: './login.component.scss', changeDetection: ChangeDetectionStrategy.OnPush })
export class LoginComponent {
  protected readonly loginMode = signal<'otp' | 'password'>('password'); protected readonly submitted = signal(false); protected readonly loginError = signal(''); protected readonly otpRequested = signal(false); protected identifier=''; protected secret='';
  constructor(private readonly router: Router, private readonly auth: AuthService) {}
  protected setMode(mode:'otp'|'password'):void { this.loginMode.set(mode); this.loginError.set(''); this.submitted.set(false); this.otpRequested.set(false); this.secret=''; }
  protected submit():void { this.loginError.set(''); if(this.loginMode()==='otp'&&!this.otpRequested()){this.auth.requestOtp(this.identifier).subscribe({next:()=>this.otpRequested.set(true),error:e=>this.loginError.set(e?.error?.message??'Unable to send OTP.')});return;} const request=this.loginMode()==='otp'?this.auth.verifyOtp(this.identifier,this.secret):this.auth.login(this.identifier,this.secret); request.subscribe({next:()=>{this.submitted.set(true);void this.router.navigateByUrl('/map');},error:e=>this.loginError.set(e?.error?.message??'Unable to sign in.')}); }
}
