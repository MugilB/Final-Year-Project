import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormGroupDirective } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-voter-registration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink,
    HttpClientModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './voter-registration.component.html',
  styleUrls: ['./voter-registration.component.css']
})
export class VoterRegistrationComponent implements OnInit {
  @ViewChild(FormGroupDirective) formGroupDirective!: FormGroupDirective;
  voterForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  wards: any[] = [];
  loadingWards = false;
  isEligible = false;
  ageMessage = '';

  get isFormValid(): boolean {
    if (!this.voterForm) return false;

    // Use Angular's built-in form validation
    // This checks all validators including required, minLength, pattern, etc.
    return this.voterForm.valid;
  }

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private http: HttpClient
  ) {
    this.voterForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2), this.noNumbersValidator]],
      lastName: ['', [Validators.required, this.noNumbersValidator]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]+$/)]],
      dateOfBirth: ['', [Validators.required, this.ageValidator.bind(this)]],
      gender: ['', [Validators.required]],
      address: ['', [Validators.required, Validators.minLength(10)]],
      wardId: ['', [Validators.required]],
      bloodGroup: ['', [Validators.required]],
      aadharCardLink: ['', [Validators.required, Validators.pattern(/^https:\/\/drive\.google\.com\/.*$/)]],
      profilePictureLink: ['', [(control: any) => {
        if (!control.value || control.value.trim() === '') {
          return null; // Valid if empty (optional field)
        }
        const urlPattern = /^https:\/\/drive\.google\.com\/.*$/;
        return urlPattern.test(control.value) ? null : { pattern: true };
      }]]
    });
  }

  ngOnInit(): void {
    this.loadWards();

    // Subscribe to dateOfBirth changes to check eligibility dynamically
    this.voterForm.get('dateOfBirth')?.valueChanges.subscribe((dob: string) => {
      this.checkEligibility(dob);
    });
  }

  noNumbersValidator(control: any) {
    if (!control.value) {
      return null; // Let required validator handle empty values
    }

    const value = control.value.toString().trim();
    // Check if the value contains any numbers (0-9)
    if (/\d/.test(value)) {
      return { containsNumbers: true };
    }

    return null;
  }

  ageValidator(control: any) {
    if (!control.value) {
      return null; // Let required validator handle empty values
    }

    const today = new Date();
    const birthDate = new Date(control.value);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }

    if (age < 18) {
      return { ageIneligible: true };
    }

    return null;
  }

  checkEligibility(dob: string): void {
    if (!dob) {
      this.isEligible = false;
      this.ageMessage = '';
      return;
    }

    const today = new Date();
    const birthDate = new Date(dob);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }

    if (age < 18) {
      this.isEligible = false;
      this.ageMessage = `You are ${age} years old. You must be at least 18 years old to register as a voter.`;
    } else {
      this.isEligible = true;
      this.ageMessage = `You are ${age} years old. You are eligible to register as a voter.`;
    }
  }

  loadWards(): void {
    this.loadingWards = true;
    this.http.get<any[]>('http://localhost:8081/api/wards')
      .subscribe({
        next: (wards) => {
          this.wards = wards;
          this.loadingWards = false;
        },
        error: (error) => {
          console.error('Error loading wards:', error);
          this.loadingWards = false;
          // Set empty array on error to prevent template errors
          this.wards = [];
        }
      });
  }

  onSubmit(): void {
    if (this.voterForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const formValue = this.voterForm.value;

      // Transform data to match backend expectations
      const wardIdValue = parseInt(formValue.wardId);
      if (isNaN(wardIdValue)) {
        this.isLoading = false;
        this.errorMessage = 'Please select a valid ward';
        return;
      }

      // Convert date to timestamp (date input returns YYYY-MM-DD format)
      let dobTimestamp: number | null = null;
      if (formValue.dateOfBirth) {
        let date: Date;
        if (formValue.dateOfBirth instanceof Date) {
          date = formValue.dateOfBirth;
        } else {
          date = new Date(formValue.dateOfBirth);
        }
        dobTimestamp = date.getTime();
        if (isNaN(dobTimestamp)) {
          this.isLoading = false;
          this.errorMessage = 'Please enter a valid date of birth';
          return;
        }
      }

      const voterData = {
        email: formValue.email?.trim(),
        firstName: formValue.firstName?.trim(),
        lastName: formValue.lastName?.trim(),
        address: formValue.address?.trim(),
        phoneNumber: formValue.phoneNumber?.trim(),
        gender: formValue.gender,
        bloodGroup: formValue.bloodGroup,
        wardId: wardIdValue,
        dob: dobTimestamp,
        aadharCardLink: formValue.aadharCardLink?.trim(),
        profilePictureLink: formValue.profilePictureLink && formValue.profilePictureLink.trim() !== ''
          ? formValue.profilePictureLink.trim()
          : null
      };

      console.log('Voter Registration Data:', voterData);

      const headers = new HttpHeaders({
        'Content-Type': 'application/json'
      });

      this.http.post('http://localhost:8081/api/voters/register', voterData, { headers })
        .subscribe({
          next: (response: any) => {
            this.isLoading = false;
            this.successMessage = response.message || 'Voter registration submitted successfully! Your account will be activated after verification.';

            // Reset form after successful submission
            this.voterForm.reset();
            if (this.formGroupDirective) {
              this.formGroupDirective.resetForm();
            }
          },
          error: (error) => {
            this.isLoading = false;
            console.error('Error submitting voter registration:', error);
            console.error('Error details:', error.error);

            if (error.error) {
              // Handle validation errors
              if (error.error.validationErrors) {
                const validationErrors = error.error.validationErrors;
                const errorMessages: string[] = [];

                // Map backend field names to form field names if needed
                Object.keys(validationErrors).forEach(field => {
                  const fieldName = this.mapBackendFieldToFormField(field);
                  const errorMsg = validationErrors[field];
                  errorMessages.push(`${this.getFieldDisplayName(fieldName)}: ${errorMsg}`);

                  // Set error on the form control if it exists
                  const formControl = this.voterForm.get(fieldName);
                  if (formControl) {
                    formControl.setErrors({ backendError: true });
                    formControl.markAsTouched();
                  }
                });

                this.errorMessage = errorMessages.join('; ');
              } else if (error.error.error) {
                this.errorMessage = error.error.error;
              } else {
                this.errorMessage = 'Failed to submit voter registration. Please try again.';
              }
            } else {
              this.errorMessage = 'Failed to submit voter registration. Please try again.';
            }
          }
        });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.voterForm.controls).forEach(key => {
      const control = this.voterForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.voterForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['backendError']) {
        // Backend error will be shown in the general error message
        return '';
      }
      if (field.errors['required']) {
        return `${this.getFieldDisplayName(fieldName)} is required`;
      }
      if (field.errors['minlength']) {
        return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['ageIneligible']) {
        return 'You must be at least 18 years old to register as a voter';
      }
      if (field.errors['containsNumbers']) {
        return `${this.getFieldDisplayName(fieldName)} cannot contain numbers`;
      }
      if (field.errors['pattern']) {
        if (fieldName === 'phoneNumber') {
          return 'Please enter a valid phone number';
        } else if (fieldName === 'aadharCardLink') {
          return 'Please enter a valid Google Drive link (must start with https://drive.google.com/)';
        } else if (fieldName === 'profilePictureLink') {
          return 'Please enter a valid Google Drive link (must start with https://drive.google.com/)';
        }
        return 'Invalid format';
      }
    }
    return '';
  }

  private mapBackendFieldToFormField(backendField: string): string {
    // Map backend field names to form field names
    const fieldMap: { [key: string]: string } = {
      'dob': 'dateOfBirth',
      'wardId': 'wardId',
      'aadharCardLink': 'aadharCardLink',
      'profilePictureLink': 'profilePictureLink'
    };
    return fieldMap[backendField] || backendField;
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      firstName: 'First Name',
      lastName: 'Last Name',
      email: 'Email',
      phoneNumber: 'Phone Number',
      dateOfBirth: 'Date of Birth',
      gender: 'Gender',
      address: 'Address',
      wardId: 'Ward ID',
      bloodGroup: 'Blood Group',
      aadharCardLink: 'Aadhar Card Link',
      profilePictureLink: 'Profile Picture Link'
    };
    return displayNames[fieldName] || fieldName;
  }

  formatDate(timestamp: number): string {
    return timestamp.toString();
  }

  goBack(): void {
    this.router.navigate(['/signin']);
  }
}
