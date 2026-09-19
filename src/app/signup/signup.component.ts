import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({ selector:'app-signup', imports:[FormsModule,RouterLink], templateUrl:'./signup.component.html', styleUrl:'./signup.component.scss', changeDetection:ChangeDetectionStrategy.OnPush })
export class SignupComponent {
  protected readonly error=signal(''); protected readonly submitted=signal(false); protected name=''; protected email=''; protected mobile=''; protected userType:'USER'|'BROKER'='USER'; protected password='';
  constructor(private readonly auth:AuthService,private readonly router:Router){}
  protected submit():void { this.error.set(''); this.auth.signup({fullName:this.name,email:this.email||undefined,mobile:this.mobile||undefined,password:this.password,role:this.userType}).subscribe({next:()=>{this.submitted.set(true);void this.router.navigateByUrl('/login');},error:e=>this.error.set(e?.error?.message??'Unable to create account.')}); }
}
