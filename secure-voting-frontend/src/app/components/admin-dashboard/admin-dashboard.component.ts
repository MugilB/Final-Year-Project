import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DataService, User, CreateUserRequest, UpdateUserRequest } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserModalComponent } from '../user-modal/user-modal.component';
import { CandidateManagementComponent } from '../candidate-management/candidate-management.component';
import { VotingChartComponent } from '../voting-chart/voting-chart.component';
import { VoterParticipationChartComponent } from '../voter-participation-chart/voter-participation-chart.component';
import { VoterManagementComponent } from '../voter-management/voter-management.component';

interface Election {
  electionId: number;
  name: string;
  startDate: number;
  endDate: number;
  status: string;
  description?: string;
  rules?: string;
  candidates?: any[];
}


interface Block {
  blockHeight: number;
  hash: string;
  previousHash: string;
  electionId?: number;
  electionName?: string;
  voterId: string;
  data: string;
  timestamp: number;
  nonce: number;
}

interface Activity {
  type: string;
  icon: string;
  title: string;
  time: string;
}


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTooltipModule, UserModalComponent, CandidateManagementComponent, VotingChartComponent, VoterParticipationChartComponent, VoterManagementComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css'],
})
export class AdminDashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('electionChart') electionChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('votingChart') votingChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('userModal') userModalRef!: any;

  currentUser: any = null;
  elections: Election[] = [];
  users: User[] = [];
  blocks: Block[] = [];
  recentActivities: Activity[] = [];
  isLoading = false;
  error: string | null = null;
  selectedFilter = 'all';
  activeSection = 'dashboard'; // 'dashboard', 'elections', 'candidates', 'users', 'blockchain', 'analytics', 'settings'
  selectedElectionForCandidates: number | null = null;

  // Statistics
  totalBlocks = 0;
  totalVotes = 0;
  latestBlock: Block | null = null;

  // Modal properties
  showElectionModal = false;
  isEditingElection = false;

  // UI Properties
  isSidebarCollapsed = false; // Initialize to false (expanded) as per user request to show styled sidebar

  // User Modal
  showUserModal: boolean = false;
  editingUser: User | null = null;
  isEditMode: boolean = false;

  // Vote Modal
  showVoteModal: boolean = false;
  selectedBlock: Block | null = null;
  decryptedVote: any = null;
  isLoadingVote: boolean = false;
  voteError: string | null = null;
  isSaving = false;
  currentElection: Election | null = null;
  electionFormData = {
    name: '',
    description: '',
    rules: '',
    startDate: '',
    endDate: '',
    status: 'SCHEDULED'
  };

  constructor(
    private authService: AuthService,
    private dataService: DataService,
    private notificationService: NotificationService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Check if user is logged in and is admin
    const user = this.authService.getUser();
    const token = this.authService.getToken();

    console.log('Admin Dashboard - Current user:', user);
    console.log('Admin Dashboard - Current token:', token ? 'Token exists' : 'No token');
    console.log('Admin Dashboard - Is admin:', this.isAdmin());

    // Temporarily disable redirects for testing
    /*
    if (!user || !token) {
      console.warn('User not logged in, redirecting to signin');
      this.router.navigate(['/signin']);
      return;
    }
    
    if (!this.isAdmin()) {
      console.warn('User is not admin, redirecting to dashboard');
      this.router.navigate(['/dashboard']);
      return;
    }
    */

    console.log('Admin Dashboard - Loading data...');
    this.loadUserData();
    this.loadElections();
    this.loadUsers();
    this.loadBlocks();
    this.loadRecentActivities();
  }

  ngAfterViewInit(): void {
    // Initialize charts after view is ready
    setTimeout(() => {
      this.initializeCharts();
    }, 100);
  }

  loadUserData(): void {
    this.currentUser = this.authService.getUser();
    if (!this.currentUser || !this.isAdmin()) {
      this.router.navigate(['/dashboard']);
    }
  }

  isAdmin(): boolean {
    return this.currentUser?.roles?.includes('ADMIN') || this.currentUser?.roles?.includes('ROLE_ADMIN') || false;
  }

  loadElections(): void {
    this.isLoading = true;
    this.error = null;

    this.dataService.getAllElections().subscribe({
      next: (elections) => {
        this.elections = elections;
        this.isLoading = false;
        console.log('Loaded elections with details:', elections);
      },
      error: (error) => {
        console.error('Error loading elections:', error);
        this.error = 'Failed to load elections';
        this.isLoading = false;
      }
    });
  }

  loadUsers(): void {
    this.isLoading = true;
    this.error = null;
    this.dataService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
        console.log('Loaded users:', users);
        // Debug: Check if personal info is present
        users.forEach(user => {
          console.log(`User ${user.voterId}: firstName=${user.firstName}, lastName=${user.lastName}, phoneNumber=${user.phoneNumber}`);
        });
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.error = 'Failed to load users';
        this.isLoading = false;
        // Fallback to mock data if API fails
        this.users = [
          {
            voterId: 'VOTER_001',
            username: 'john_doe',
            email: 'john@example.com',
            role: 'USER',
            active: true,
            firstName: 'John',
            lastName: 'Doe',
            phoneNumber: '1234567890',
            gender: 'Male',
            address: '123 Main St',
            wardId: 1,
            bloodGroup: 'O+',
            approvalStatus: 1
          },
          {
            voterId: 'VOTER_002',
            username: 'jane_smith',
            email: 'jane@example.com',
            role: 'USER',
            active: true,
            firstName: 'Jane',
            lastName: 'Smith',
            phoneNumber: '0987654321',
            gender: 'Female',
            address: '456 Oak Ave',
            wardId: 2,
            bloodGroup: 'A+',
            approvalStatus: 1
          },
          {
            voterId: 'ADMIN_VOTER_ID',
            username: 'admin_user',
            email: 'admin@example.com',
            role: 'ADMIN',
            active: true,
            firstName: 'Admin',
            lastName: 'User',
            phoneNumber: '5555555555',
            gender: 'Other',
            address: '789 Admin Blvd',
            wardId: 1,
            bloodGroup: 'AB+',
            approvalStatus: 1
          }
        ];
      }
    });
  }

  getFilteredElections(): Election[] {
    const now = Date.now();
    switch (this.selectedFilter) {
      case 'active':
        return this.elections.filter(e => {
          // Filter based on timestamp
          return now >= e.startDate && now <= e.endDate;
        });
      case 'ended':
        return this.elections.filter(e => {
          // Filter based on timestamp
          return now > e.endDate;
        });
      case 'pending':
        return this.elections.filter(e => {
          // Filter based on timestamp
          return now < e.startDate;
        });
      default:
        return this.elections;
    }
  }

  getFilterCount(filter: string): number {
    const now = Date.now();
    switch (filter) {
      case 'active':
        return this.elections.filter(e => now >= e.startDate && now <= e.endDate).length;
      case 'ended':
        return this.elections.filter(e => now > e.endDate).length;
      case 'pending':
        return this.elections.filter(e => now < e.startDate).length;
      default:
        return this.elections.length;
    }
  }

  onFilterChange(filter: string): void {
    this.selectedFilter = filter;
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    // Clear selected election when manually navigating to candidates section
    if (section === 'candidates') {
      this.selectedElectionForCandidates = null;
    }
  }

  getElectionStatus(election: Election): string {
    const now = Date.now();

    // Calculate status based on timestamp (not just from DB)
    // This ensures we always show the real-time status
    if (now > election.endDate) {
      return 'ended';
    } else if (now >= election.startDate && now <= election.endDate) {
      return 'active';
    } else if (now < election.startDate) {
      return 'pending';
    }

    // Fallback to DB status if timestamps are invalid
    if (election.status === 'ACTIVE' || election.status === 'OPENED') {
      return 'active';
    } else if (election.status === 'ENDED' || election.status === 'CLOSED') {
      return 'ended';
    } else if (election.status === 'PENDING' || election.status === 'SCHEDULED') {
      return 'pending';
    }
    return 'inactive';
  }

  getFormattedDate(timestamp: number): string {
    return this.dataService.formatElectionDate(timestamp);
  }

  getTimeRemaining(timestamp: number): string {
    return this.dataService.getTimeRemaining(timestamp);
  }

  createElection(): void {
    // Navigate to the new full-page create election component
    this.router.navigate(['/create-election']);
  }

  editElection(election: Election): void {
    console.log('Editing election:', election);
    console.log('Election description:', election.description);
    console.log('Election rules:', election.rules);

    this.isEditingElection = true;
    this.currentElection = election;
    this.electionFormData = {
      name: election.name,
      description: election.description || '',
      rules: election.rules || '',
      startDate: this.formatDateForInput(election.startDate),
      endDate: this.formatDateForInput(election.endDate),
      status: election.status
    };

    console.log('Election form data:', this.electionFormData);
    this.showElectionModal = true;
  }

  deleteElection(election: Election): void {
    if (confirm(`Are you sure you want to delete "${election.name}"?`)) {
      this.dataService.deleteElection(election.electionId).subscribe({
        next: () => {
          console.log('Election deleted successfully');
          this.loadElections(); // Refresh the elections list
        },
        error: (error) => {
          console.error('Error deleting election:', error);
          alert('Failed to delete election. Please try again.');
        }
      });
    }
  }

  manageCandidates(election: Election): void {
    // Switch to candidates section and filter by the selected election
    this.activeSection = 'candidates';
    // Store the election ID for filtering in the candidate management component
    this.selectedElectionForCandidates = election.electionId;
  }

  viewResults(election: Election): void {
    // Navigate to view results page
    console.log('View results for election:', election);
  }

  createUser(): void {
    this.editingUser = null;
    this.isEditMode = false;
    this.showUserModal = true;
  }

  editUser(user: User): void {
    // Find the user from the current list to ensure we have the latest data
    const freshUser = this.users.find(u => (u.voterId || u.username) === (user.voterId || user.username)) || user;
    this.editingUser = { ...freshUser }; // Create a copy to ensure reactivity
    this.isEditMode = true;
    this.showUserModal = true;
  }

  deleteUser(user: User): void {
    const userId = user.voterId || user.username;
    if (!userId) {
      console.error('Cannot delete user: no valid ID found');
      return;
    }
    if (confirm(`Are you sure you want to delete user "${userId}"?`)) {
      this.dataService.deleteUser(userId).subscribe({
        next: (response) => {
          console.log('User deleted successfully:', response);
          this.loadUsers(); // Reload users list
          alert('User deleted successfully!');
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          alert('Failed to delete user. Please try again.');
        }
      });
    }
  }

  getAdminUsersCount(): number {
    return this.users.filter(u => u.role === 'ADMIN' || u.role === 'ROLE_ADMIN').length;
  }

  // User Modal Event Handlers
  closeUserModal(): void {
    this.showUserModal = false;
    this.editingUser = null;
    this.isEditMode = false;
  }

  saveUser(event: { user: CreateUserRequest | UpdateUserRequest, isEdit: boolean }): void {
    if (event.isEdit && this.editingUser) {
      // Update existing user - use voterId as the identifier
      const userId = this.editingUser.voterId || this.editingUser.username;
      if (!userId) {
        console.error('Cannot update user: no valid ID found');
        return;
      }
      console.log('Updating user with ID:', userId);
      const updateRequest = event.user as UpdateUserRequest;

      this.dataService.updateUser(userId, updateRequest).subscribe({
        next: (updatedUser) => {
          console.log('User updated successfully:', updatedUser);

          // Immediately update the local user with the data we know was saved
          const index = this.users.findIndex(u => (u.voterId || u.username) === userId);
          if (index !== -1) {
            // Merge the updated data with the existing user
            // Use the request data if provided, otherwise keep existing or use response
            this.users[index] = {
              ...this.users[index],
              ...updatedUser,
              address: updateRequest.address !== undefined && updateRequest.address !== null && updateRequest.address.trim() !== ''
                ? updateRequest.address
                : (updatedUser.address || this.users[index].address || undefined),
              wardId: updateRequest.wardId !== undefined && updateRequest.wardId !== null
                ? updateRequest.wardId
                : (updatedUser.wardId !== undefined ? updatedUser.wardId : this.users[index].wardId),
              aadharCardLink: updateRequest.aadharCardLink !== undefined && updateRequest.aadharCardLink !== null && updateRequest.aadharCardLink.trim() !== ''
                ? updateRequest.aadharCardLink
                : (updatedUser.aadharCardLink || this.users[index].aadharCardLink || undefined),
              profilePictureLink: updateRequest.profilePictureLink !== undefined && updateRequest.profilePictureLink !== null && updateRequest.profilePictureLink.trim() !== ''
                ? updateRequest.profilePictureLink
                : (updatedUser.profilePictureLink || this.users[index].profilePictureLink || undefined)
            };
          } else {
            // If not found, reload the entire list
            this.loadUsers();
          }

          // Reset loading state before closing
          this.userModalRef?.resetLoadingState();

          // Close modal first
          this.closeUserModal();

          // Reload users list to get fresh data
          this.loadUsers();

          // Show alert after modal is closed
          setTimeout(() => {
            alert('User updated successfully!');
          }, 100);
        },
        error: (error) => {
          console.error('Error updating user:', error);
          console.error('Error status:', error.status);
          console.error('Error message:', error.message);
          console.error('Error details:', error.error);
          this.userModalRef?.resetLoadingState();
          alert(`Failed to update user. Error: ${error.status} - ${error.message || 'Unknown error'}`);
        }
      });
    } else {
      // Create new user
      this.dataService.createUser(event.user as CreateUserRequest).subscribe({
        next: (createdUser) => {
          console.log('User created successfully:', createdUser);

          // Reset loading state before closing
          this.userModalRef?.resetLoadingState();

          this.loadUsers();
          this.closeUserModal();

          setTimeout(() => {
            alert('User created successfully!');
          }, 100);
        },
        error: (error) => {
          console.error('Error creating user:', error);
          console.error('Error status:', error.status);
          console.error('Error message:', error.message);
          console.error('Error details:', error.error);
          this.userModalRef?.resetLoadingState();
          alert(`Failed to create user. Error: ${error.status} - ${error.message || 'Unknown error'}`);
        }
      });
    }
  }

  loadBlocks(): void {
    console.log('Loading blocks...');
    this.isLoading = true;
    this.dataService.getAllBlocks().subscribe({
      next: (blocks) => {
        console.log('Blocks loaded successfully:', blocks);
        this.blocks = blocks;
        this.totalBlocks = blocks.length;
        this.latestBlock = blocks.length > 0 ? blocks[blocks.length - 1] : null;
        this.totalVotes = blocks.length; // Assuming each block represents a vote
        this.isLoading = false;
        console.log('Total blocks:', this.totalBlocks);
        console.log('Latest block:', this.latestBlock);
      },
      error: (error) => {
        console.error('Error loading blocks:', error);
        console.error('Full error object:', JSON.stringify(error, null, 2));
        this.error = 'Failed to load blockchain data';
        this.isLoading = false;
      }
    });
  }

  loadRecentActivities(): void {
    this.recentActivities = [
      {
        type: 'election',
        icon: 'fas fa-vote-yea',
        title: 'New election "System Placeholder" created',
        time: '2 hours ago'
      },
      {
        type: 'user',
        icon: 'fas fa-user-plus',
        title: 'New user "john_doe" registered',
        time: '4 hours ago'
      },
      {
        type: 'vote',
        icon: 'fas fa-check-circle',
        title: 'Vote cast in "TN_CM" election',
        time: '6 hours ago'
      },
      {
        type: 'block',
        icon: 'fas fa-link',
        title: 'New block added to blockchain',
        time: '8 hours ago'
      }
    ];
  }

  getVoteCountForElection(electionId: number): number {
    return this.blocks.filter(block => block.electionId === electionId).length;
  }

  getBlockchainIntegrity(): number {
    // Mock integrity calculation
    return 99.9;
  }

  refreshBlocks(): void {
    this.loadBlocks();
  }

  viewBlockDetails(block: Block): void {
    console.log('View block details:', block);
    // Implement block details modal or navigation
    alert(`Block Details:\nHeight: ${block.blockHeight}\nHash: ${block.hash}\nPrevious Hash: ${block.previousHash}\nElection: ${block.electionName || 'N/A'}\nVoter: ${block.voterId}\nTimestamp: ${new Date(block.timestamp).toLocaleString()}\nNonce: ${block.nonce}`);
  }

  viewBlockData(block: Block): void {
    console.log('View block data:', block);
    // Show full data content
    alert(`Block Data:\n${block.data}`);
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      console.log('Hash copied to clipboard');
      // You could show a toast notification here
    }).catch(err => {
      console.error('Failed to copy hash:', err);
    });
  }

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  // Modal control methods
  closeElectionModal(): void {
    this.showElectionModal = false;
    this.isEditingElection = false;
    this.currentElection = null;
    this.isSaving = false;
  }

  saveElection(): void {
    if (this.isSaving) return;

    this.isSaving = true;

    const electionData = {
      name: this.electionFormData.name,
      description: this.electionFormData.description,
      rules: this.electionFormData.rules,
      startDate: new Date(this.electionFormData.startDate).getTime(),
      endDate: new Date(this.electionFormData.endDate).getTime(),
      status: this.electionFormData.status
    };

    if (this.isEditingElection && this.currentElection) {
      // Update existing election
      this.dataService.updateElection(this.currentElection.electionId, electionData).subscribe({
        next: (updatedElection) => {
          console.log('Election updated successfully:', updatedElection);
          this.loadElections();
          this.notificationService.notifyElectionUpdated();
          this.closeElectionModal();
        },
        error: (error) => {
          console.error('Error updating election:', error);
          alert('Failed to update election. Please try again.');
          this.isSaving = false;
        }
      });
    } else {
      // Create new election
      this.dataService.createElection(electionData).subscribe({
        next: (createdElection) => {
          console.log('Election created successfully:', createdElection);
          this.loadElections();
          this.notificationService.notifyElectionUpdated();
          this.closeElectionModal();
        },
        error: (error) => {
          console.error('Error creating election:', error);
          alert('Failed to create election. Please try again.');
          this.isSaving = false;
        }
      });
    }
  }

  formatDateForInput(timestamp: number): string {
    const date = new Date(timestamp);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  getElectionDuration(): string {
    if (!this.electionFormData.startDate || !this.electionFormData.endDate) {
      return '';
    }

    const startDate = new Date(this.electionFormData.startDate);
    const endDate = new Date(this.electionFormData.endDate);
    const durationMs = endDate.getTime() - startDate.getTime();

    if (durationMs <= 0) {
      return 'Invalid duration';
    }

    const days = Math.floor(durationMs / (1000 * 60 * 60 * 24));
    const hours = Math.floor((durationMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));

    if (days > 0) {
      return `${days} day${days > 1 ? 's' : ''}, ${hours} hour${hours > 1 ? 's' : ''}`;
    } else if (hours > 0) {
      return `${hours} hour${hours > 1 ? 's' : ''}, ${minutes} minute${minutes > 1 ? 's' : ''}`;
    } else {
      return `${minutes} minute${minutes > 1 ? 's' : ''}`;
    }
  }

  initializeCharts(): void {
    // Initialize charts using Chart.js or similar library
    // This is a placeholder for chart initialization
    console.log('Initializing charts...');
  }

  goToUserDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  signOut(): void {
    this.authService.signOut();
    this.router.navigate(['/signin']);
  }

  testBackendConnection(): void {
    // Check if user is logged in
    const user = this.authService.getUser();
    const token = this.authService.getToken();

    console.log('Current user:', user);
    console.log('Current token:', token ? 'Token exists' : 'No token');

    this.dataService.testBackendConnection().subscribe({
      next: (response) => {
        console.log('Backend test response:', response);
        alert(`Backend connection successful: ${response}`);
      },
      error: (error) => {
        console.error('Backend connection test failed:', error);
        console.error('Full error object:', JSON.stringify(error, null, 2));
        console.error('Error status:', error.status);
        console.error('Error statusText:', error.statusText);
        console.error('Error url:', error.url);

        let errorMessage = 'Unknown error';
        if (error.status === 0) {
          errorMessage = 'Backend server is not running or not accessible';
        } else if (error.status === 401) {
          errorMessage = 'Authentication required - please log in';
        } else if (error.status === 403) {
          errorMessage = 'Access forbidden - insufficient permissions';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        } else if (error.message) {
          errorMessage = error.message;
        }

        alert(`Backend connection failed. Error: ${errorMessage}`);
      }
    });
  }

  updateElectionStatuses(): void {
    this.isLoading = true;
    this.dataService.updateElectionStatuses().subscribe({
      next: (response) => {
        console.log('Election statuses updated:', response);
        this.loadElections(); // Refresh the elections list
        this.isLoading = false;
        alert('Election statuses updated successfully!');
      },
      error: (error) => {
        console.error('Error updating election statuses:', error);
        console.error('Full error object:', JSON.stringify(error, null, 2));
        this.isLoading = false;
        alert(`Failed to update election statuses. Error: ${error.error?.message || error.message || 'Unknown error'}`);
      }
    });
  }

  // Vote Modal Methods
  viewVote(block: Block): void {
    this.selectedBlock = block;
    this.showVoteModal = true;
    this.isLoadingVote = true;
    this.voteError = null;
    this.decryptedVote = null;

    // Call the backend to decrypt the vote from the steganographic image
    this.dataService.decryptVoteFromBlock(block.blockHeight).subscribe({
      next: (voteData) => {
        console.log('Decrypted vote data:', voteData);
        this.decryptedVote = voteData;
        this.isLoadingVote = false;
      },
      error: (error) => {
        console.error('Error decrypting vote:', error);
        this.voteError = 'Failed to decrypt vote data. The vote may be corrupted or the decryption key may be invalid.';
        this.isLoadingVote = false;
      }
    });
  }

  closeVoteModal(): void {
    this.showVoteModal = false;
    this.selectedBlock = null;
    this.decryptedVote = null;
    this.voteError = null;
    this.isLoadingVote = false;
  }

  getFormattedTimestamp(timestamp: number | undefined): string {
    if (timestamp === undefined || timestamp === null) {
      return 'N/A';
    }
    return this.getFormattedDate(timestamp);
  }
}
