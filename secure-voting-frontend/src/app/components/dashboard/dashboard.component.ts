import { Component, OnInit, OnDestroy, ChangeDetectorRef, HostListener } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { DataService, Election, Candidate } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';
import { ElectionCardComponent } from '../election-card/election-card.component';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ElectionCardComponent, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: any = {};
  userFullName: string = '';
  allElections: Election[] = [];
  isLoading = false;
  errorMessage = '';
  selectedFilter: string = 'all'; // 'all', 'eligible', 'voted', 'ended', 'pending'
  userVotingStatus: Map<number, boolean> = new Map(); // Cache for voting status
  private routerSubscription: Subscription = new Subscription();
  showProfileMenu = false;
  showChangePasswordModal = false;
  changePasswordForm: FormGroup;
  isChangingPassword = false;
  passwordChangeErrorMessage = '';
  passwordChangeSuccessMessage = '';
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private authService: AuthService,
    private dataService: DataService,
    private notificationService: NotificationService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private formBuilder: FormBuilder
  ) {
    this.changePasswordForm = this.formBuilder.group({
      newPassword: ['', [Validators.required, Validators.minLength(8), this.passwordPolicyValidator]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    // Subscribe to password changes for real-time validation feedback
    this.changePasswordForm.get('newPassword')?.valueChanges.subscribe(() => {
      this.changePasswordForm.get('newPassword')?.updateValueAndValidity();
      this.changePasswordForm.get('confirmPassword')?.updateValueAndValidity();
      
      // Enable/disable confirm password based on new password validity
      const newPasswordControl = this.changePasswordForm.get('newPassword');
      const confirmPasswordControl = this.changePasswordForm.get('confirmPassword');
      
      if (newPasswordControl && confirmPasswordControl) {
        if (newPasswordControl.valid) {
          confirmPasswordControl.enable();
        } else {
          confirmPasswordControl.disable();
          confirmPasswordControl.setValue('');
        }
      }
    });
    
    // Initially disable confirm password field
    this.changePasswordForm.get('confirmPassword')?.disable();
  }

  ngOnInit(): void {
    console.log('=== DashboardComponent ngOnInit() called ===');
    
    // Check if user is logged in
    const isLoggedIn = this.authService.isLoggedIn();
    console.log('Is logged in:', isLoggedIn);
    
    if (!isLoggedIn) {
      console.log('User not logged in, redirecting to signin');
      this.router.navigate(['/signin']);
      return;
    }

    // Get current user data
    this.currentUser = this.authService.getUser();
    console.log('Current user:', this.currentUser);
    
    // Load user details to get firstName and lastName
    this.loadUserDetails();
    
    // Load elections
    console.log('Loading elections...');
    this.loadElections();

    // Subscribe to router events to refresh data when navigating back to dashboard
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        if (event.url === '/dashboard') {
          // Refresh elections when navigating back to dashboard
          this.loadElections();
        }
      });

    // Subscribe to election update notifications
    this.routerSubscription.add(
      this.notificationService.electionUpdated$.subscribe(() => {
        console.log('=== Election update notification received ===');
        this.loadElections();
        // Also refresh voting status when elections are updated
        this.loadVotingStatus();
      })
    );
  }

  ngOnDestroy(): void {
    // Clean up subscription
    this.routerSubscription.unsubscribe();
  }

  loadUserDetails(): void {
    console.log('=== loadUserDetails() called ===');
    const voterId = this.currentUser?.username || this.currentUser?.voterId;
    console.log('Current voterId:', voterId);
    
    // Use the new /users/me endpoint to get current user's details with firstName/lastName
    this.dataService.getCurrentUser().subscribe({
      next: (user: any) => {
        console.log('=== SUCCESS: Got current user details from API ===');
        console.log('Full response:', JSON.stringify(user, null, 2));
        console.log('firstName type:', typeof user.firstName, 'value:', user.firstName);
        console.log('lastName type:', typeof user.lastName, 'value:', user.lastName);
        
        // Update currentUser with full details
        this.currentUser = { ...this.currentUser, ...user };
        
        // Handle null/undefined values properly
        const firstName = user.firstName || '';
        const lastName = user.lastName || '';
        
        console.log('Processed firstName:', firstName, 'is empty?', firstName.trim() === '');
        console.log('Processed lastName:', lastName, 'is empty?', lastName.trim() === '');
        
        // Build full name from firstName and lastName
        if (firstName && lastName && firstName.trim() !== '' && lastName.trim() !== '') {
          this.userFullName = `${firstName.trim()} ${lastName.trim()}`;
          console.log('✅✅✅ Set userFullName from firstName + lastName:', this.userFullName);
          this.cdr.detectChanges(); // Force change detection
        } else if (firstName && firstName.trim() !== '') {
          this.userFullName = firstName.trim();
          console.log('✅✅✅ Set userFullName from firstName only:', this.userFullName);
          this.cdr.detectChanges(); // Force change detection
        } else if (lastName && lastName.trim() !== '') {
          this.userFullName = lastName.trim();
          console.log('✅✅✅ Set userFullName from lastName only:', this.userFullName);
          this.cdr.detectChanges(); // Force change detection
        } else {
          // If firstName and lastName are null/empty, keep using voterId as fallback
          this.userFullName = this.currentUser?.username || this.currentUser?.voterId || 'User';
          console.warn('⚠️⚠️⚠️ firstName and lastName are null/empty in database!');
          console.warn('⚠️ Using voterId as fallback:', this.userFullName);
          console.warn('⚠️ Please update the user_details table with firstName and lastName for voterId:', voterId);
          this.cdr.detectChanges();
        }
      },
      error: (error: any) => {
        console.error('=== ERROR: Loading current user details failed ===');
        console.error('Full error object:', error);
        console.error('Error status:', error?.status);
        console.error('Error message:', error?.message);
        console.error('Error URL:', error?.url);
        
        // Fallback: check if currentUser already has firstName/lastName
        if (this.currentUser?.firstName && this.currentUser?.lastName &&
            this.currentUser.firstName.trim() !== '' && this.currentUser.lastName.trim() !== '') {
          this.userFullName = `${this.currentUser.firstName} ${this.currentUser.lastName}`.trim();
          console.log('Using firstName/lastName from currentUser (fallback):', this.userFullName);
        } else if (this.currentUser?.firstName && this.currentUser.firstName.trim() !== '') {
          this.userFullName = this.currentUser.firstName.trim();
          console.log('Using firstName from currentUser (fallback):', this.userFullName);
        } else if (this.currentUser?.lastName && this.currentUser.lastName.trim() !== '') {
          this.userFullName = this.currentUser.lastName.trim();
          console.log('Using lastName from currentUser (fallback):', this.userFullName);
        } else {
          this.userFullName = this.currentUser?.username || this.currentUser?.voterId || 'User';
          console.warn('Using voterId/username (final fallback):', this.userFullName);
        }
        this.cdr.detectChanges();
      }
    });
  }

  getFullName(): string {
    // Priority 1: Check if we've already loaded a valid full name (not voterId)
    if (this.userFullName && 
        this.userFullName.trim() !== '' && 
        this.userFullName !== this.currentUser?.username && 
        this.userFullName !== this.currentUser?.voterId &&
        !this.userFullName.startsWith('VOTER_') &&
        !this.userFullName.match(/^VOTER_\d+$/)) {
      return this.userFullName;
    }
    
    // Priority 2: Check if currentUser has firstName/lastName directly (from API response)
    if (this.currentUser?.firstName && this.currentUser?.lastName) {
      const firstNameTrimmed = (this.currentUser.firstName || '').trim();
      const lastNameTrimmed = (this.currentUser.lastName || '').trim();
      if (firstNameTrimmed !== '' && lastNameTrimmed !== '') {
        const fullName = `${firstNameTrimmed} ${lastNameTrimmed}`;
        this.userFullName = fullName;
        return fullName;
      }
    }
    
    // Priority 3: Check if we have just firstName
    if (this.currentUser?.firstName) {
      const firstNameTrimmed = (this.currentUser.firstName || '').trim();
      if (firstNameTrimmed !== '' && !firstNameTrimmed.startsWith('VOTER_')) {
        this.userFullName = firstNameTrimmed;
        return firstNameTrimmed;
      }
    }
    
    // Priority 4: Check if we have just lastName
    if (this.currentUser?.lastName) {
      const lastNameTrimmed = (this.currentUser.lastName || '').trim();
      if (lastNameTrimmed !== '' && !lastNameTrimmed.startsWith('VOTER_')) {
        this.userFullName = lastNameTrimmed;
        return lastNameTrimmed;
      }
    }
    
    // Final fallback - use voterId/username only if we really don't have a name
    return this.currentUser?.username || this.currentUser?.voterId || 'User';
  }

  loadElections(): void {
    console.log('=== loadElections() called ===');
    this.isLoading = true;
    this.errorMessage = '';

    // Get all elections with candidates
    console.log('Calling getAllElectionsWithCandidates()...');
    this.dataService.getAllElectionsWithCandidates().subscribe({
      next: (elections) => {
        console.log('Elections loaded successfully:', elections);
        this.allElections = elections;
        this.loadVotingStatus();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading elections with candidates:', error);
        console.log('Falling back to regular elections...');
        // Fallback to regular elections if with-candidates fails
        this.dataService.getAllElections().subscribe({
          next: (elections) => {
            this.allElections = elections;
            this.isLoading = false;
          },
          error: (fallbackError) => {
            // Final fallback to open elections
            this.dataService.getOpenElections().subscribe({
              next: (elections) => {
                this.allElections = elections;
                this.isLoading = false;
              },
              error: (finalError) => {
                this.errorMessage = 'Failed to load elections. Please try again later.';
                this.isLoading = false;
              }
            });
          }
        });
      }
    });
  }

  signOut(): void {
    this.authService.signOut();
    this.router.navigate(['/signin']);
  }

  refreshElections(): void {
    this.loadElections();
  }

  // Method to manually refresh voting status (for debugging)
  refreshVotingStatus(): void {
    console.log('=== Manually refreshing voting status ===');
    this.loadVotingStatus();
  }

  loadVotingStatus(): void {
    console.log('=== loadVotingStatus() called ===');
    console.log('Current user:', this.currentUser);
    
    if (!this.currentUser?.username) {
      console.log('No current user, skipping voting status check');
      return;
    }

    // Clear previous status
    this.userVotingStatus.clear();

    // The username field from login response is actually the voterId
    const voterId = this.currentUser.username;
    console.log(`Checking voting status for voter ID: ${voterId}`);
    console.log(`Number of elections to check: ${this.allElections.length}`);

    // Check voting status for each election
    this.allElections.forEach(election => {
      console.log(`Checking voting status for election: ${election.name} (ID: ${election.electionId})`);
      this.dataService.hasUserVotedInElection(voterId, election.electionId).subscribe({
        next: (status) => {
          this.userVotingStatus.set(election.electionId, status.hasVoted);
          console.log(`✅ Voting status for election ${election.electionId} (${election.name}): ${status.hasVoted}`);
        },
        error: (error) => {
          console.error(`❌ Error checking voting status for election ${election.electionId}:`, error);
          // Default to false if there's an error
          this.userVotingStatus.set(election.electionId, false);
        }
      });
    });
  }

  hasUserVoted(electionId: number): boolean {
    return this.userVotingStatus.get(electionId) || false;
  }

  isElectionEnded(election: Election): boolean {
    // Check if election is manually ended by admin
    if (election.status === 'CLOSED' || election.status === 'ENDED' || election.status === 'CANCELLED') {
      return true;
    }
    
    // Check if election is naturally ended by time
    if ((election.status === 'ACTIVE' || election.status === 'OPENED') && election.endDate <= Date.now()) {
      return true;
    }
    
    return false;
  }

  getFilteredElections(): Election[] {
    if (this.selectedFilter === 'all') {
      return this.allElections;
    }
    
    return this.allElections.filter(election => {
      const hasVoted = this.hasUserVoted(election.electionId);
      const isEligible = this.dataService.isElectionEligible(election) && !hasVoted;
      const isEnded = this.isElectionEnded(election);
      const isPending = election.status === 'PENDING' || election.startDate > Date.now();
      
      // Debug logging
      console.log(`Election ${election.name} (${election.electionId}):`, {
        hasVoted,
        isEligible,
        isEnded,
        isPending,
        selectedFilter: this.selectedFilter
      });
      
      switch (this.selectedFilter) {
        case 'eligible':
          return isEligible;
        case 'voted':
          return hasVoted;
        case 'ended':
          return isEnded;
        case 'pending':
          return isPending;
        default:
          return true;
      }
    });
  }

  getElectionStatus(election: Election): string {
    const hasVoted = this.hasUserVoted(election.electionId);
    const isEligible = this.dataService.isElectionEligible(election) && !hasVoted;
    const isEnded = this.isElectionEnded(election);
    const isPending = election.status === 'PENDING' || election.startDate > Date.now();
    
    if (hasVoted) return 'voted';
    if (isEligible) return 'eligible';
    if (isEnded) return 'ended';
    if (isPending) return 'pending';
    return 'ENDED';
  }

  onFilterChange(filter: string): void {
    this.selectedFilter = filter;
  }

  getFilterCount(filter: string): number {
    if (filter === 'all') return this.allElections.length;
    
    return this.allElections.filter(election => {
      const hasVoted = this.hasUserVoted(election.electionId);
      const isEligible = this.dataService.isElectionEligible(election) && !hasVoted;
      const isEnded = this.isElectionEnded(election);
      const isPending = election.status === 'PENDING' || election.startDate > Date.now();
      
      switch (filter) {
        case 'eligible':
          return isEligible;
        case 'voted':
          return hasVoted;
        case 'ended':
          return isEnded;
        case 'pending':
          return isPending;
        default:
          return true;
      }
    }).length;
  }

  isAdmin(): boolean {
    const hasAdminRole = this.currentUser?.roles?.includes('ADMIN');
    const hasRoleAdmin = this.currentUser?.roles?.includes('ROLE_ADMIN');
    const isAdmin = hasAdminRole || hasRoleAdmin || false;
    
    console.log('isAdmin() check:', {
      currentUser: this.currentUser,
      roles: this.currentUser?.roles,
      hasAdminRole,
      hasRoleAdmin,
      isAdmin
    });
    
    return isAdmin;
  }

  goToVote(): void {
    this.router.navigate(['/vote']);
  }

  goToAdmin(): void {
    console.log('=== goToAdmin() called ===');
    console.log('Current user:', this.currentUser);
    console.log('User roles:', this.currentUser?.roles);
    console.log('Is admin:', this.isAdmin());
    
    // Check if user is logged in
    const token = this.authService.getToken();
    console.log('JWT Token:', token ? 'Token exists' : 'No token');
    
    try {
      console.log('Attempting to navigate to /admin');
      
      // Test navigation with promise handling
      this.router.navigate(['/admin']).then(success => {
        console.log('Navigation result:', success);
        if (success) {
          console.log('✅ Successfully navigated to admin panel');
        } else {
          console.log('❌ Navigation failed');
        }
      }).catch(error => {
        console.error('❌ Navigation error:', error);
      });
      
      console.log('Navigation to admin panel initiated');
    } catch (error) {
      console.error('Error navigating to admin panel:', error);
    }
  }

  testNavigation(): void {
    console.log('=== testNavigation() called ===');
    console.log('Testing navigation to signup page...');
    
    this.router.navigate(['/signup']).then(success => {
      console.log('Test navigation result:', success);
      if (success) {
        console.log('✅ Test navigation successful');
      } else {
        console.log('❌ Test navigation failed');
      }
    }).catch(error => {
      console.error('❌ Test navigation error:', error);
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-dropdown-container')) {
      this.closeProfileMenu();
    }
  }

  toggleProfileMenu(): void {
    this.showProfileMenu = !this.showProfileMenu;
  }

  closeProfileMenu(): void {
    this.showProfileMenu = false;
  }

  openChangePasswordModal(): void {
    this.showChangePasswordModal = true;
    this.showProfileMenu = false;
    this.passwordChangeErrorMessage = '';
    this.passwordChangeSuccessMessage = '';
    this.showNewPassword = false;
    this.showConfirmPassword = false;
    this.changePasswordForm.reset();
    // Disable confirm password initially
    this.changePasswordForm.get('confirmPassword')?.disable();
  }

  closeChangePasswordModal(): void {
    this.showChangePasswordModal = false;
    this.passwordChangeErrorMessage = '';
    this.passwordChangeSuccessMessage = '';
    this.showNewPassword = false;
    this.showConfirmPassword = false;
    this.changePasswordForm.reset();
    // Disable confirm password when closing
    this.changePasswordForm.get('confirmPassword')?.disable();
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  isNewPasswordValid(): boolean {
    const newPasswordControl = this.changePasswordForm.get('newPassword');
    return newPasswordControl ? newPasswordControl.valid : false;
  }

  private passwordPolicyValidator = (control: any): { [key: string]: any } | null => {
    if (!control.value) {
      return null;
    }

    const password = control.value;
    const errors: any = {};

    if (password.length < 8) {
      errors['minLength'] = true;
    }

    if (!/[A-Z]/.test(password)) {
      errors['noUppercase'] = true;
    }

    if (!/[a-z]/.test(password)) {
      errors['noLowercase'] = true;
    }

    if (!/[0-9]/.test(password)) {
      errors['noDigit'] = true;
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  private passwordMatchValidator = (group: FormGroup): { [key: string]: any } | null => {
    const newPassword = group.get('newPassword');
    const confirmPassword = group.get('confirmPassword');
    
    if (!newPassword || !confirmPassword) {
      return null;
    }
    
    if (newPassword.value && confirmPassword.value && newPassword.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    
    return null;
  };

  getPasswordPolicyStatus(): { requirement: string; met: boolean }[] {
    const password = this.changePasswordForm.get('newPassword')?.value || '';

    return [
      {
        requirement: 'At least 8 characters',
        met: password.length >= 8
      },
      {
        requirement: 'At least one uppercase letter',
        met: /[A-Z]/.test(password)
      },
      {
        requirement: 'At least one lowercase letter',
        met: /[a-z]/.test(password)
      },
      {
        requirement: 'At least one digit',
        met: /[0-9]/.test(password)
      }
    ];
  }

  getPasswordFieldError(fieldName: string): string {
    const field = this.changePasswordForm.get(fieldName);
    if (field?.errors && (field.touched || field.dirty)) {
      if (field.errors['required']) {
        return fieldName === 'newPassword' ? 'New Password is required' : 'Confirm Password is required';
      }
      if (field.errors['minlength'] || field.errors['minLength']) {
        return 'Password must be at least 8 characters';
      }
    }
    
    // Check for password mismatch error on the form group
    if (fieldName === 'confirmPassword' && this.changePasswordForm.errors?.['passwordMismatch'] && field?.touched) {
      return 'Passwords do not match';
    }
    
    return '';
  }

  onChangePasswordSubmit(): void {
    if (this.changePasswordForm.valid) {
      this.isChangingPassword = true;
      this.passwordChangeErrorMessage = '';
      this.passwordChangeSuccessMessage = '';

      const newPassword = this.changePasswordForm.get('newPassword')?.value;
      const voterId = this.currentUser?.username || this.currentUser?.voterId;

      if (!voterId) {
        this.passwordChangeErrorMessage = 'Unable to identify user. Please try again.';
        this.isChangingPassword = false;
        return;
      }

      this.authService.changePassword(voterId, newPassword).subscribe({
        next: (response: any) => {
          this.isChangingPassword = false;
          this.passwordChangeSuccessMessage = response.message || 'Password changed successfully!';
          this.changePasswordForm.reset();
          
          // Close modal after 2 seconds
          setTimeout(() => {
            this.closeChangePasswordModal();
          }, 2000);
        },
        error: (error) => {
          this.isChangingPassword = false;
          console.error('Error changing password:', error);
          
          if (error.error?.error) {
            this.passwordChangeErrorMessage = error.error.error;
          } else if (error.error?.message) {
            this.passwordChangeErrorMessage = error.error.message;
          } else {
            this.passwordChangeErrorMessage = 'Failed to change password. Please try again later.';
          }
        }
      });
    } else {
      this.markPasswordFormGroupTouched();
    }
  }

  private markPasswordFormGroupTouched(): void {
    Object.keys(this.changePasswordForm.controls).forEach(key => {
      const control = this.changePasswordForm.get(key);
      control?.markAsTouched();
    });
  }
}
