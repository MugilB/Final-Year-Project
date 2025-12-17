import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { User, CreateUserRequest, UpdateUserRequest } from '../../services/data.service';

@Component({
  selector: 'app-user-modal',
  templateUrl: './user-modal.component.html',
  styleUrls: ['./user-modal.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule]
})
export class UserModalComponent implements OnInit, OnChanges {
  @Input() isOpen: boolean = false;
  @Input() user: User | null = null;
  @Input() isEdit: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<{ user: CreateUserRequest | UpdateUserRequest, isEdit: boolean }>();

  userForm: FormGroup;
  isSubmitting: boolean = false;
  wards: any[] = [];
  loadingWards = false;

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.userForm = this.fb.group({
      // Account Information
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['USER', [Validators.required]],
      approvalStatus: [1], // Default to approved
      
      // Personal Information
      firstName: [''],
      lastName: [''],
      phoneNumber: [''],
      dateOfBirth: [''],
      gender: [''],
      address: [''],
      wardId: [''],
      bloodGroup: [''],
      
      // Document Information
      aadharCardLink: [''],
      profilePictureLink: ['']
    });
  }

  ngOnInit(): void {
    this.loadWards();
    this.isSubmitting = false; // Ensure loading state is reset on init
    this.loadUserData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Reload data when modal opens or when user/isEdit changes
    const isOpenChanged = changes['isOpen'];
    const userChanged = changes['user'];
    const isEditChanged = changes['isEdit'];
    
    // Always reload when modal becomes open
    if (isOpenChanged && this.isOpen) {
      // Reset loading state when modal opens
      this.isSubmitting = false;
      
      // Small delay to ensure all inputs are set
      setTimeout(() => {
        this.loadUserData();
      }, 150);
    } 
    // Also reload if user or edit mode changes while modal is open
    else if (this.isOpen && (userChanged || isEditChanged)) {
      // Reset loading state
      this.isSubmitting = false;
      
      setTimeout(() => {
        this.loadUserData();
      }, 150);
    }
  }

  private loadUserData(): void {
    if (this.isEdit && this.user) {
      console.log('Loading user data:', this.user);
      
      // Handle null/undefined/empty values properly
      const cleanValue = (value: any, placeholders: string[] = []) => {
        if (!value || value === 'null' || value === 'undefined') return '';
        const strValue = String(value).trim();
        if (strValue === '' || placeholders.includes(strValue)) return '';
        return strValue;
      };
      
      this.userForm.patchValue({
        username: this.user.voterId || this.user.username || '',
        email: this.user.email || '',
        role: this.user.role || 'USER',
        approvalStatus: this.user.approvalStatus || 1,
        firstName: cleanValue(this.user.firstName),
        lastName: cleanValue(this.user.lastName),
        phoneNumber: cleanValue(this.user.phoneNumber),
        dateOfBirth: this.user.dateOfBirth ? new Date(this.user.dateOfBirth).toISOString().split('T')[0] : '',
        gender: cleanValue(this.user.gender),
        address: cleanValue(this.user.address, ['Address not provided']),
        wardId: this.user.wardId !== undefined && this.user.wardId !== null ? String(this.user.wardId) : '',
        bloodGroup: cleanValue(this.user.bloodGroup),
        aadharCardLink: cleanValue(this.user.aadharCardLink, ['Aadhar Card not provided']),
        profilePictureLink: cleanValue(this.user.profilePictureLink, ['Profile Picture not provided'])
      });
      
      // Make all fields optional for editing
      this.userForm.get('username')?.clearValidators();
      this.userForm.get('username')?.updateValueAndValidity();
      
      this.userForm.get('email')?.clearValidators();
      this.userForm.get('email')?.updateValueAndValidity();
      
      this.userForm.get('password')?.clearValidators();
      this.userForm.get('password')?.updateValueAndValidity();
      
      this.userForm.get('role')?.clearValidators();
      this.userForm.get('role')?.updateValueAndValidity();
    } else if (!this.isEdit) {
      // Reset form for create
      this.userForm.reset();
      this.userForm.patchValue({ 
        role: 'USER',
        approvalStatus: 1
      });
      
      // Set required validators for create mode
      this.userForm.get('username')?.setValidators([Validators.required, Validators.minLength(3)]);
      this.userForm.get('username')?.updateValueAndValidity();
      
      this.userForm.get('email')?.setValidators([Validators.required, Validators.email]);
      this.userForm.get('email')?.updateValueAndValidity();
      
      this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.userForm.get('password')?.updateValueAndValidity();
      
      this.userForm.get('role')?.setValidators([Validators.required]);
      this.userForm.get('role')?.updateValueAndValidity();
    }
  }

  closeModal(): void {
    this.isSubmitting = false;
    this.close.emit();
  }

  resetLoadingState(): void {
    this.isSubmitting = false;
  }

  onSubmit(): void {
    if ((this.isEdit ? !this.userForm.invalid : this.userForm.valid) && !this.isSubmitting) {
      this.isSubmitting = true;
      
      const formValue = this.userForm.value;
      
      if (this.isEdit && this.user) {
        const updateRequest: UpdateUserRequest = {};
        
        // Only include fields that have values
        if (formValue.username && formValue.username.trim() !== '') {
          updateRequest.voterId = formValue.username;
        }
        if (formValue.email && formValue.email.trim() !== '') {
          updateRequest.email = formValue.email;
        }
        if (formValue.password && formValue.password.trim() !== '') {
          (updateRequest as any).password = formValue.password;
        }
        if (formValue.role) {
          updateRequest.roles = [formValue.role];
        }
        if (formValue.approvalStatus !== undefined) {
          updateRequest.approvalStatus = formValue.approvalStatus;
        }
        if (formValue.firstName && formValue.firstName.trim() !== '') {
          updateRequest.firstName = formValue.firstName;
        }
        if (formValue.lastName && formValue.lastName.trim() !== '') {
          updateRequest.lastName = formValue.lastName;
        }
        if (formValue.phoneNumber && formValue.phoneNumber.trim() !== '') {
          updateRequest.phoneNumber = formValue.phoneNumber;
        }
        if (formValue.dateOfBirth) {
          updateRequest.dateOfBirth = new Date(formValue.dateOfBirth).getTime();
        }
        if (formValue.gender && formValue.gender.trim() !== '') {
          updateRequest.gender = formValue.gender;
        }
        if (formValue.address && formValue.address.trim() !== '') {
          updateRequest.address = formValue.address;
        }
        if (formValue.wardId && formValue.wardId.toString().trim() !== '') {
          updateRequest.wardId = parseInt(formValue.wardId);
        }
        if (formValue.bloodGroup && formValue.bloodGroup.trim() !== '') {
          updateRequest.bloodGroup = formValue.bloodGroup;
        }
        if (formValue.aadharCardLink && formValue.aadharCardLink.trim() !== '') {
          updateRequest.aadharCardLink = formValue.aadharCardLink;
        }
        if (formValue.profilePictureLink && formValue.profilePictureLink.trim() !== '') {
          updateRequest.profilePictureLink = formValue.profilePictureLink;
        }
        
        this.save.emit({ user: updateRequest, isEdit: true });
      } else {
        const createRequest: CreateUserRequest = {
          voterId: formValue.username,
          email: formValue.email,
          password: formValue.password,
          roles: [formValue.role],
          firstName: formValue.firstName,
          lastName: formValue.lastName,
          phoneNumber: formValue.phoneNumber,
          dateOfBirth: formValue.dateOfBirth ? new Date(formValue.dateOfBirth).getTime() : undefined,
          gender: formValue.gender,
          address: formValue.address,
          wardId: formValue.wardId ? parseInt(formValue.wardId) : undefined,
          bloodGroup: formValue.bloodGroup,
          aadharCardLink: formValue.aadharCardLink,
          profilePictureLink: formValue.profilePictureLink
        };
        
        this.save.emit({ user: createRequest, isEdit: false });
      }
    }
  }

  getFieldError(fieldName: string): string {
    const field = this.userForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['minlength']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
    }
    return '';
  }

  get isFormValid(): boolean {
    if (this.isEdit) {
      // For edit mode, form is valid if it has no errors (all fields are optional)
      return !this.userForm.invalid;
    } else {
      // For create mode, form must be completely valid
      return this.userForm.valid;
    }
  }

  get modalTitle(): string {
    return this.isEdit ? 'Edit User' : 'Create New User';
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

  getWardName(wardId: number): string {
    const ward = this.wards.find(w => w.wardId === wardId);
    return ward ? ward.wardName : `Ward ${wardId}`;
  }
}
