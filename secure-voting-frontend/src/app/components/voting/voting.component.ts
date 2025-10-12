import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DataService, Election, Candidate } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-voting',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './voting.component.html',
  styleUrl: './voting.component.css'
})
export class VotingComponent implements OnInit {
  elections: Election[] = [];
  candidates: Candidate[] = [];
  selectedElection: Election | null = null;
  selectedElectionId: number | null = null;
  selectedCandidateId: number | null = null;
  isLoading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private dataService: DataService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    // Check if electionId is provided in route
    this.route.params.subscribe(params => {
      if (params['electionId']) {
        const electionId = +params['electionId'];
        this.selectedElectionId = electionId;
        // Load elections first, then load election details and candidates
        this.loadElectionsAndDetails(electionId);
      } else {
        this.loadElections();
      }
    });
  }

  loadElections() {
    this.isLoading = true;
    this.error = null;
    
    this.dataService.getAllElections().subscribe({
      next: (elections) => {
        this.elections = elections;
        this.isLoading = false;
      },
      error: (error) => {
        this.error = 'Failed to load elections';
        this.isLoading = false;
      }
    });
  }

  loadElectionsAndDetails(electionId: number) {
    this.isLoading = true;
    this.error = null;
    
    this.dataService.getAllElections().subscribe({
      next: (elections) => {
        this.elections = elections;
        // Now that elections are loaded, find the specific election
        this.selectedElection = elections.find(e => e.electionId === electionId) || null;
        if (this.selectedElection) {
          this.loadCandidates();
        } else {
          this.error = 'Election not found';
          this.isLoading = false;
        }
      },
      error: (error) => {
        this.error = 'Failed to load elections';
        this.isLoading = false;
      }
    });
  }


  loadCandidates() {
    if (!this.selectedElectionId) return;
    
    this.isLoading = true;
    this.error = null;
    
    this.dataService.getApprovedCandidatesByElection(this.selectedElectionId).subscribe({
      next: (candidates) => {
        this.candidates = candidates;
        this.isLoading = false;
      },
      error: (error) => {
        this.error = 'Failed to load candidates';
        this.isLoading = false;
      }
    });
  }

  submitVote() {
    if (!this.selectedElectionId || !this.selectedCandidateId) {
      this.error = 'Please select a candidate';
      return;
    }

    this.isLoading = true;
    this.error = null;
    this.success = null;

    const voteData = {
      electionId: this.selectedElectionId,
      candidateId: this.selectedCandidateId
    };

    this.dataService.submitVote(voteData).subscribe({
      next: (response) => {
        this.success = 'Vote submitted successfully! Your vote has been encrypted and stored securely.';
        this.resetForm();
        this.isLoading = false;
        
        // Notify dashboard to refresh voting status
        console.log('=== Vote submitted successfully, notifying dashboard ===');
        this.notificationService.notifyElectionUpdated();
        
        // Navigate back to dashboard after a short delay
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 2000);
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to submit vote. Please try again.';
        this.isLoading = false;
      }
    });
  }

  resetForm() {
    this.selectedElectionId = null;
    this.selectedCandidateId = null;
    this.candidates = [];
  }


  getSelectedCandidateName(): string {
    const candidate = this.candidates.find(c => c.candidateId === this.selectedCandidateId);
    return candidate ? candidate.name : 'Select Candidate';
  }

  selectCandidate(candidateId: number): void {
    this.selectedCandidateId = candidateId;
    this.error = null; // Clear any previous errors
  }

  getPartyName(partyId?: number): string {
    // For now, return a simple mapping or you can implement a proper party service
    if (!partyId) return 'Independent';
    
    // Simple mapping - you can enhance this with a proper party service
    const partyMap: { [key: number]: string } = {
      1: 'Democratic Party',
      2: 'Republican Party',
      3: 'Green Party',
      4: 'Libertarian Party'
    };
    
    return partyMap[partyId] || `Party ${partyId}`;
  }

  viewCandidateDetails(candidate: Candidate, event: Event): void {
    event.stopPropagation(); // Prevent row selection when clicking details button
    
    // For now, just show an alert with candidate details
    // You can enhance this to show a modal or navigate to a details page
    const details = `
      Name: ${candidate.name}
      Party: ${this.getPartyName(candidate.partyId)}
      ${candidate.candidateDetails?.biography ? `Bio: ${candidate.candidateDetails.biography}` : ''}
      ${candidate.candidateDetails?.manifestoSummary ? `Manifesto: ${candidate.candidateDetails.manifestoSummary}` : ''}
    `.trim();
    
    alert(details);
  }

  goBackToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  formatDate(dateValue: string | number): string {
    if (!dateValue) return 'N/A';
    
    try {
      // Handle both string and number (timestamp) formats
      const date = typeof dateValue === 'number' ? new Date(dateValue) : new Date(dateValue);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
      });
    } catch (error) {
      return 'Invalid Date';
    }
  }

  getTimeRemaining(endDateValue: string | number): string {
    if (!endDateValue) return 'N/A';
    
    try {
      // Handle both string and number (timestamp) formats
      const endDate = typeof endDateValue === 'number' ? new Date(endDateValue) : new Date(endDateValue);
      const now = new Date();
      const diff = endDate.getTime() - now.getTime();
      
      if (diff <= 0) {
        return 'Election Ended';
      }
      
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      
      if (days > 0) {
        return `${days}d ${hours}h ${minutes}m remaining`;
      } else if (hours > 0) {
        return `${hours}h ${minutes}m remaining`;
      } else {
        return `${minutes}m remaining`;
      }
    } catch (error) {
      return 'Invalid Date';
    }
  }
}
