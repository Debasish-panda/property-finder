import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { MapViewComponent } from './map/map-view.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'signup', component: SignupComponent },
	{ path: 'map', component: MapViewComponent, canActivate: [authGuard] },
	{ path: '**', redirectTo: '' },
];
