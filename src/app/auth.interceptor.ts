import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService); const token = auth.token();
  const outgoing = token && !request.url.endsWith('/auth/login/password') && !request.url.endsWith('/auth/login/otp/request') && !request.url.endsWith('/auth/login/otp/verify') && !request.url.endsWith('/auth/signup')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;
  return next(outgoing).pipe(catchError(error => { if (error.status === 401 && token) auth.clear(); return throwError(() => error); }));
};
