import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PricingPlan { id: number; code: string; name: string; amount: number; amountPaise: number; currency: string; validityDays: number; coverageInfo: string; maxPropertyListings: number; dailyLoadLimit: number; }
export interface CreateOrderResponse { orderId: string; amount: number; currency: string; keyId: string; planCode: string; paymentMethod: string; }

@Injectable({ providedIn: 'root' })
export class BillingApiService {
  private readonly http = inject(HttpClient);
  getPlans(): Observable<{ plans: PricingPlan[]; currency: string }> { return this.http.get<{ plans: PricingPlan[]; currency: string }>('/api/plans'); }
  createOrder(planCode: string, paymentMethod = 'all'): Observable<CreateOrderResponse> { return this.http.post<CreateOrderResponse>('/api/billing/orders', { planCode, paymentMethod }); }
  verifyPayment(payload: { razorpayOrderId: string; razorpayPaymentId: string; razorpaySignature: string }): Observable<{ message: string; status: string }> { return this.http.post<{ message: string; status: string }>('/api/billing/payments/verify', payload); }
  getSubscription(): Observable<unknown> { return this.http.get('/api/billing/subscription'); }
  getUsage(): Observable<unknown> { return this.http.get('/api/billing/usage'); }
}
