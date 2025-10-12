import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

const AUTH_API = `${environment.apiUrl}/auth/`;

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  username: string;
  email: string;
  roles: string[];
}

export interface MessageResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(AUTH_API + 'signin', {
      username: credentials.username,
      password: credentials.password
    }, httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  register(user: RegisterData): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(AUTH_API + 'signup', {
      username: user.username,
      email: user.email,
      password: user.password
    }, httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  // --- Session Management ---

  signOut(): void {
    if (typeof window !== 'undefined') {
      window.sessionStorage.clear();
    }
  }

  public saveToken(token: string): void {
    if (typeof window !== 'undefined') {
      window.sessionStorage.removeItem('auth-token');
      window.sessionStorage.setItem('auth-token', token);
    }
  }

  public getToken(): string | null {
    if (typeof window !== 'undefined') {
      return window.sessionStorage.getItem('auth-token');
    }
    return null;
  }

  public saveUser(user: any): void {
    if (typeof window !== 'undefined') {
      window.sessionStorage.removeItem('auth-user');
      window.sessionStorage.setItem('auth-user', JSON.stringify(user));
    }
  }

  public getUser(): any {
    if (typeof window !== 'undefined') {
      const user = window.sessionStorage.getItem('auth-user');
      return user ? JSON.parse(user) : {};
    }
    return {};
  }

  public isLoggedIn(): boolean {
    return !!this.getToken();
  }

  public getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = error.error?.message || error.message || 'Server error occurred';
    }
    
    return throwError(() => ({ error: { message: errorMessage } }));
  }
}