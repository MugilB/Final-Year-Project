import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-voter-registration',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink, HttpClientModule],
  templateUrl: './voter-registration.component.html',
  styleUrls: ['./voter-registration.component.css']
})
export class VoterRegistrationComponent implements OnInit {
  voterForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  wards: any[] = [];
  loadingWards = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private http: HttpClient
  ) {
    this.voterForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]+$/)]],
      dateOfBirth: ['', [Validators.required]],
      gender: ['', [Validators.required]],
      address: ['', [Validators.required, Validators.minLength(10)]],
      wardId: ['', [Validators.required]],
      bloodGroup: ['', [Validators.required]],
      aadharCardLink: ['', [Validators.required, Validators.pattern(/^https:\/\/drive\.google\.com\/.*$/)]],
      profilePictureLink: ['', [Validators.pattern(/^https:\/\/drive\.google\.com\/.*$/)]]
    });
  }

  ngOnInit(): void {
    this.loadWards();
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
          // If wards fail to load, set some default options
          this.wards = [
            { wardId: 1, wardName: 'Ward 1 - Central' },
            { wardId: 2, wardName: 'Ward 2 - North' },
            { wardId: 3, wardName: 'Ward 3 - South' },
            { wardId: 4, wardName: 'Ward 4 - East' },
            { wardId: 5, wardName: 'Ward 5 - West' }
          ];
        }
      });
  }




  onSubmit(): void {
    if (this.voterForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const voterData = this.voterForm.value;
      console.log('Voter Registration Data:', voterData);

      // Prepare data for backend with proper structure
      const registrationData = {
        // User account data
        email: voterData.email,
        
        // User details data (using camelCase as expected by backend)
        firstName: voterData.firstName,
        lastName: voterData.lastName,
        address: voterData.address,
        phoneNumber: voterData.phoneNumber,
        gender: voterData.gender,
        bloodGroup: voterData.bloodGroup,
        wardId: parseInt(voterData.wardId),
        dob: new Date(voterData.dateOfBirth).getTime(),
        aadharCardLink: voterData.aadharCardLink,
        profilePictureLink: voterData.profilePictureLink || null
      };

      // Call backend API
      const headers = new HttpHeaders({
        'Content-Type': 'application/json'
      });

      this.http.post('http://localhost:8081/api/voters/register', registrationData, { headers })
        .subscribe({
          next: (response: any) => {
            this.isLoading = false;
            this.successMessage = response.message || 'Voter registration submitted successfully! Your application is pending approval (Status: Waiting for Approval). You will be notified once approved.';
            
            // Reset form after successful submission
            this.voterForm.reset();
          },
          error: (error) => {
            this.isLoading = false;
            console.error('Error submitting voter registration:', error);
            
            if (error.status === 409) {
              this.errorMessage = 'Username or email already exists. Please choose different credentials.';
            } else if (error.status === 400) {
              this.errorMessage = 'Invalid data provided. Please check your information and try again.';
            } else if (error.error && error.error.error) {
              this.errorMessage = error.error.error;
            } else {
              this.errorMessage = 'Failed to submit voter registration. Please try again later.';
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
      if (field.errors['required']) {
        return `${this.getFieldDisplayName(fieldName)} is required`;
      }
      if (field.errors['minlength']) {
        return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['pattern']) {
        if (fieldName === 'phoneNumber') {
          return 'Please enter a valid phone number';
        } else if (fieldName === 'aadharCardLink') {
          return 'Please enter a valid Google Drive link (must start with https://drive.google.com/)';
        }
        return 'Invalid format';
      }
      if (field.errors['passwordMismatch']) {
        return 'Passwords do not match';
      }
    }
    return '';
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
      aadharCardLink: 'Upload Proof (Aadhar Card)',
      profilePictureLink: 'Profile Picture'
    };
    return displayNames[fieldName] || fieldName;
  }


  goBack(): void {
    this.router.navigate(['/signin']);
  }
}
