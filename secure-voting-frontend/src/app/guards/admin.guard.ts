import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const user = this.authService.getUser();
    const token = this.authService.getToken();
    const hasAdminRole = user?.roles?.includes('ADMIN');
    const hasRoleAdmin = user?.roles?.includes('ROLE_ADMIN');
    const isAdmin = hasAdminRole || hasRoleAdmin;
    
    console.log('AdminGuard - canActivate check:', {
      user,
      token: token ? 'Token exists' : 'No token',
      roles: user?.roles,
      hasAdminRole,
      hasRoleAdmin,
      isAdmin
    });
    
    if (!user || !token) {
      console.log('AdminGuard - No user or token, redirecting to signin');
      this.router.navigate(['/signin']);
      return false;
    }
    
    if (!isAdmin) {
      console.log('AdminGuard - User is not admin, redirecting to dashboard');
      this.router.navigate(['/dashboard']);
      return false;
    }
    
    console.log('AdminGuard - Access granted to admin panel');
    return true;
  }
}
