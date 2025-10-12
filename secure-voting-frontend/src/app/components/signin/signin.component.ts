import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.css']
})
export class SigninComponent implements OnInit {
  signinForm: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.signinForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    // Check if user is already logged in
    if (this.authService.getToken()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    if (this.signinForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const credentials = {
        username: this.signinForm.value.username,
        password: this.signinForm.value.password
      };

      this.authService.login(credentials).subscribe({
        next: (response) => {
          this.isLoading = false;
          // Save token and user data
          this.authService.saveToken(response.accessToken);
          this.authService.saveUser({
            username: response.username,
            email: response.email,
            roles: response.roles
          });
          
          // Redirect based on user role
          const userRoles = response.roles || [];
          if (userRoles.includes('ADMIN') || userRoles.includes('ROLE_ADMIN')) {
            this.router.navigate(['/admin']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Login failed. Please try again.';
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.signinForm.controls).forEach(key => {
      const control = this.signinForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.signinForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        const displayName = fieldName === 'username' ? 'Voter ID' : fieldName.charAt(0).toUpperCase() + fieldName.slice(1);
        return `${displayName} is required`;
      }
      if (field.errors['minlength']) {
        const displayName = fieldName === 'username' ? 'Voter ID' : fieldName.charAt(0).toUpperCase() + fieldName.slice(1);
        return `${displayName} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
    }
    return '';
  }

  openVoterRegistration(): void {
    // Navigate to voter registration page
    this.router.navigate(['/voter-registration']);
  }

  openCandidateNomination(): void {
    // Navigate to candidate nomination page
    this.router.navigate(['/candidate-nomination']);
  }
}
