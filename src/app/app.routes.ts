import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { MapViewComponent } from './map/map-view.component';
import { authGuard, brokerGuard } from './auth.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProfileComponent } from './profile.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'map', component: MapViewComponent, canActivate: [authGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [brokerGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
