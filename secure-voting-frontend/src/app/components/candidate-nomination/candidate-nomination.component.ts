import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-candidate-nomination',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink, HttpClientModule],
  templateUrl: './candidate-nomination.component.html',
  styleUrls: ['./candidate-nomination.component.css']
})
export class CandidateNominationComponent implements OnInit {
  candidateForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  elections: any[] = [];
  loadingElections = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private http: HttpClient
  ) {
    this.candidateForm = this.formBuilder.group({
      candidateName: ['', [Validators.required, Validators.minLength(2), this.noNumbersValidator]],
      gender: ['', [Validators.required]],
      age: ['', [Validators.required, Validators.min(18), Validators.max(100)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]+$/)]],
      address: ['', [Validators.required, Validators.minLength(10)]],
      party: ['', [Validators.required]],
      customParty: [''],
      partySecretCode: ['', [Validators.required, Validators.minLength(6)]],
      aadharCardLink: ['', [Validators.required, Validators.pattern(/^https:\/\/drive\.google\.com\/.*$/)]],
      candidateImageLink: ['', [Validators.pattern(/^https:\/\/drive\.google\.com\/.*$/)]],
      electionId: ['', [Validators.required]]
    }, { validators: this.partyValidation });
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

  ngOnInit(): void {
    this.loadElections();
  }

  loadElections(): void {
    this.loadingElections = true;
    console.log('Loading elections from API...');
    
    this.http.get('http://localhost:8081/api/elections/for-nominations')
      .subscribe({
        next: (elections: any) => {
          console.log('Elections loaded successfully:', elections);
          this.elections = elections;
          this.loadingElections = false;
        },
        error: (error) => {
          console.error('Error loading elections:', error);
          console.error('Error details:', error.error);
          console.error('Error status:', error.status);
          this.loadingElections = false;
          this.errorMessage = 'Failed to load elections. Please refresh the page.';
        }
      });
  }

  partyValidation(form: FormGroup) {
    const party = form.get('party');
    const customParty = form.get('customParty');
    
    if (party?.value === 'Other') {
      if (!customParty?.value || customParty.value.trim().length < 2) {
        customParty?.setErrors({ required: true });
        return { customPartyRequired: true };
      }
    }
    
    if (customParty?.hasError('required')) {
      customParty.setErrors(null);
    }
    
    return null;
  }

  onSubmit(): void {
    if (this.candidateForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const formData = this.candidateForm.value;
      
      // Determine the final party name
      let finalPartyName = formData.party;
      if (formData.party === 'Other' && formData.customParty) {
        finalPartyName = formData.customParty.trim();
      }

      const candidateData = {
        ...formData,
        party: finalPartyName,
        electionId: parseInt(formData.electionId)
      };

      console.log('Candidate Nomination Data:', candidateData);

      // Call backend API
      const headers = new HttpHeaders({
        'Content-Type': 'application/json'
      });

      this.http.post('http://localhost:8081/api/candidate-nominations', candidateData, { headers })
        .subscribe({
          next: (response: any) => {
            this.isLoading = false;
            this.successMessage = response.message || 'Candidate nomination submitted successfully! We will review your application and get back to you.';
            
            // Reset form after successful submission
            this.candidateForm.reset();
          },
          error: (error) => {
            this.isLoading = false;
            console.error('Error submitting nomination:', error);
            
            if (error.error && error.error.error) {
              this.errorMessage = error.error.error;
            } else {
              this.errorMessage = 'Failed to submit nomination. Please try again.';
            }
          }
        });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.candidateForm.controls).forEach(key => {
      const control = this.candidateForm.get(key);
      control?.markAsTouched();
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.candidateForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${this.getFieldDisplayName(fieldName)} is required`;
      }
      if (field.errors['minlength']) {
        return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['maxlength']) {
        return `${this.getFieldDisplayName(fieldName)} must not exceed ${field.errors['maxlength'].requiredLength} characters`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['min']) {
        return `${this.getFieldDisplayName(fieldName)} must be at least ${field.errors['min'].min}`;
      }
      if (field.errors['max']) {
        return `${this.getFieldDisplayName(fieldName)} must not exceed ${field.errors['max'].max}`;
      }
      if (field.errors['containsNumbers']) {
        return `${this.getFieldDisplayName(fieldName)} cannot contain numbers`;
      }
      if (field.errors['pattern']) {
        if (fieldName === 'phoneNumber') {
          return 'Please enter a valid phone number';
        } else if (fieldName === 'aadharCardLink') {
          return 'Please enter a valid Google Drive link (must start with https://drive.google.com/)';
        }
        return 'Invalid format';
      }
    }
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      candidateName: 'Candidate Name',
      gender: 'Gender',
      age: 'Age',
      email: 'Email',
      phoneNumber: 'Phone Number',
      address: 'Address',
      party: 'Political Party',
      customParty: 'Political Party Name',
      partySecretCode: 'Party Secret Code',
      aadharCardLink: 'Aadhar Card Link',
      candidateImageLink: 'Candidate Image Link',
      electionId: 'Election'
    };
    return displayNames[fieldName] || fieldName;
  }

  getSecretCodePlaceholder(): string {
    const partyValue = this.candidateForm.get('party')?.value;
    if (partyValue === 'Independent Candidate') {
      return 'INDEPENDENT_SECRET_2024';
    }
    return 'Enter your party secret code';
  }

  formatDate(timestamp: number): string {
    return timestamp.toString();
  }

  goBack(): void {
    this.router.navigate(['/signin']);
  }
}
