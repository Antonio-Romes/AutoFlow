import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthResponse } from '../models/auth-response';

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storageKey = 'autoflow.auth';

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap((tokens) => localStorage.setItem(this.storageKey, JSON.stringify(tokens)))
    );
  }

  getAccessToken(): string | null {
    const saved = localStorage.getItem(this.storageKey);
    if (!saved) return null;
    try {
      return (JSON.parse(saved) as AuthResponse).accessToken ?? null;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
  }
}
