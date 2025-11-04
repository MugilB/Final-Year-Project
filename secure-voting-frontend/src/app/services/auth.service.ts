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

  // --- Cookie Management ---
  
  private setCookie(name: string, value: string, days: number = 7): void {
    if (typeof document !== 'undefined') {
      const date = new Date();
      date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
      const expires = 'expires=' + date.toUTCString();
      document.cookie = `${name}=${value};${expires};path=/;SameSite=Strict`;
    }
  }

  private getCookie(name: string): string | null {
    if (typeof document !== 'undefined') {
      const nameEQ = name + '=';
      const ca = document.cookie.split(';');
      for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
      }
    }
    return null;
  }

  private deleteCookie(name: string): void {
    if (typeof document !== 'undefined') {
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
    }
  }

  // --- Session Management ---

  signOut(): void {
    if (typeof document !== 'undefined') {
      this.deleteCookie('auth-token');
      this.deleteCookie('auth-user');
      // Also clear sessionStorage for backward compatibility
      if (typeof window !== 'undefined') {
        window.sessionStorage.clear();
      }
    }
  }

  public saveToken(token: string): void {
    // Store token in cookie with 7 days expiration (adjust as needed)
    this.setCookie('auth-token', token, 7);
    // Also keep in sessionStorage for backward compatibility during transition
    if (typeof window !== 'undefined') {
      window.sessionStorage.setItem('auth-token', token);
    }
  }

  public getToken(): string | null {
    // Try to get from cookie first
    const cookieToken = this.getCookie('auth-token');
    if (cookieToken) {
      return cookieToken;
    }
    // Fallback to sessionStorage for backward compatibility
    if (typeof window !== 'undefined') {
      const sessionToken = window.sessionStorage.getItem('auth-token');
      // If found in sessionStorage but not in cookie, migrate it
      if (sessionToken) {
        this.setCookie('auth-token', sessionToken, 7);
        return sessionToken;
      }
    }
    return null;
  }

  public saveUser(user: any): void {
    // Store user data in cookie
    const userJson = JSON.stringify(user);
    this.setCookie('auth-user', userJson, 7);
    // Also keep in sessionStorage for backward compatibility
    if (typeof window !== 'undefined') {
      window.sessionStorage.setItem('auth-user', userJson);
    }
  }

  public getUser(): any {
    // Try to get from cookie first
    const cookieUser = this.getCookie('auth-user');
    if (cookieUser) {
      try {
        return JSON.parse(cookieUser);
      } catch (e) {
        return {};
      }
    }
    // Fallback to sessionStorage for backward compatibility
    if (typeof window !== 'undefined') {
      const sessionUser = window.sessionStorage.getItem('auth-user');
      if (sessionUser) {
        try {
          const user = JSON.parse(sessionUser);
          // Migrate to cookie
          this.setCookie('auth-user', sessionUser, 7);
          return user;
        } catch (e) {
          return {};
        }
      }
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

  changePassword(voterId: string, newPassword: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(AUTH_API + 'reset-password', {
      voterId: voterId,
      newPassword: newPassword
    }, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
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