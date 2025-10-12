import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { DataService, Election, Candidate } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';
import { ElectionCardComponent } from '../election-card/election-card.component';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ElectionCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: any = {};
  allElections: Election[] = [];
  isLoading = false;
  errorMessage = '';
  selectedFilter: string = 'all'; // 'all', 'eligible', 'voted', 'ended', 'pending'
  userVotingStatus: Map<number, boolean> = new Map(); // Cache for voting status
  private routerSubscription: Subscription = new Subscription();

  constructor(
    private authService: AuthService,
    private dataService: DataService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

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
}
