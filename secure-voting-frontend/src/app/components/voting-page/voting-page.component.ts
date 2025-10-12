import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { DataService, Election, Candidate } from '../../services/data.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-voting-page',
  standalone: true,
  imports: [CommonModule, FormsModule, MatTableModule, MatSortModule, MatProgressBarModule],
  templateUrl: './voting-page.component.html',
  styleUrls: ['./voting-page.component.css']
})
export class VotingPageComponent implements OnInit {
  election: Election | null = null;
  candidates: Candidate[] = [];
  selectedCandidate: Candidate | null = null;
  isLoading: boolean = true;
  isSubmitting: boolean = false;
  error: string = '';
  electionId: number = 0;
  displayedColumns: string[] = ['select', 'name', 'party', 'ward', 'actions'];
  showConfirmationCard: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dataService: DataService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.electionId = +params['id'];
      if (this.electionId) {
        this.loadElectionAndCandidates();
      }
    });
  }

  private loadElectionAndCandidates(): void {
    this.isLoading = true;
    this.error = '';

    // Load election details
    this.dataService.getAllElectionsWithCandidates().subscribe({
      next: (elections) => {
        this.election = elections.find(e => e.electionId === this.electionId) || null;
        if (this.election) {
          if (this.election.candidates && this.election.candidates.length > 0) {
            this.candidates = this.election.candidates;
          } else {
            this.candidates = this.getMockCandidates();
          }
          this.isLoading = false;
        } else {
          this.error = 'Election not found';
          this.isLoading = false;
        }
      },
      error: (error) => {
        console.error('Error loading election:', error);
        this.error = 'Failed to load election details';
        this.isLoading = false;
      }
    });
  }

  private getMockCandidates(): Candidate[] {
    return [
      {
        candidateId: 1,
        name: 'John Smith',
        electionId: this.electionId,
        partyId: 1,
        wardId: 101
      },
      {
        candidateId: 2,
        name: 'Sarah Johnson',
        electionId: this.electionId,
        partyId: 2,
        wardId: 102
      },
      {
        candidateId: 3,
        name: 'Michael Brown',
        electionId: this.electionId,
        partyId: 1,
        wardId: 103
      },
      {
        candidateId: 4,
        name: 'Emily Davis',
        electionId: this.electionId,
        partyId: 3,
        wardId: 104
      }
    ];
  }

  selectCandidate(candidate: Candidate): void {
    this.selectedCandidate = candidate;
  }

  openConfirmationCard(): void {
    if (!this.selectedCandidate) {
      this.error = 'Please select a candidate before voting.';
      return;
    }
    this.showConfirmationCard = true;
  }

  closeConfirmationCard(): void {
    this.showConfirmationCard = false;
  }

  confirmVote(): void {
    if (!this.selectedCandidate) {
      this.error = 'Please select a candidate before voting.';
      return;
    }

    this.isSubmitting = true;
    this.error = '';

    // Here you would typically call the voting API
    // For now, we'll simulate the vote submission
    setTimeout(() => {
      this.isSubmitting = false;
      this.showConfirmationCard = false;
      // Navigate back to dashboard with success message
      this.router.navigate(['/dashboard'], { 
        queryParams: { 
          voteSuccess: 'true',
          candidateName: this.selectedCandidate!.name 
        } 
      });
    }, 1500);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  signOut(): void {
    this.authService.signOut();
    this.router.navigate(['/signin']);
  }

  getFormattedDate(timestamp: number): string {
    return this.dataService.formatElectionDate(timestamp);
  }

  getTimeRemaining(timestamp: number): string {
    return this.dataService.getTimeRemaining(timestamp);
  }

  selectAll(): void {
    // This method can be used for future functionality
    console.log('Select all candidates functionality');
  }

  getElectionEndDate(): string {
    if (this.election?.endDate) {
      return this.getFormattedDate(this.election.endDate);
    }
    return 'N/A';
  }
}
