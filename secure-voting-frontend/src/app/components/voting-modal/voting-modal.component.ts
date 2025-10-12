import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService, Election, Candidate } from '../../services/data.service';

@Component({
  selector: 'app-voting-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './voting-modal.component.html',
  styleUrls: ['./voting-modal.component.css']
})
export class VotingModalComponent implements OnInit, OnChanges {
  @Input() election!: Election;
  @Input() isOpen: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() voteSubmitted = new EventEmitter<{ electionId: number, candidateId: number }>();

  candidates: Candidate[] = [];
  selectedCandidate: Candidate | null = null;
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  error: string = '';

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    if (this.election) {
      this.loadCandidates();
    }
  }

  ngOnChanges(): void {
    if (this.election && this.isOpen) {
      this.loadCandidates();
    }
  }

  loadCandidates(): void {
    this.isLoading = true;
    this.error = '';
    
    // First check if candidates are already loaded in the election object
    if (this.election.candidates && this.election.candidates.length > 0) {
      this.candidates = this.election.candidates;
      this.isLoading = false;
    } else {
      // For testing, let's always show mock candidates if no candidates are found
      this.candidates = this.getMockCandidates();
      this.isLoading = false;
    }
  }

  private getMockCandidates(): Candidate[] {
    return [
      {
        candidateId: 1,
        name: 'John Smith',
        electionId: this.election.electionId,
        partyId: 1,
        wardId: 101
      },
      {
        candidateId: 2,
        name: 'Sarah Johnson',
        electionId: this.election.electionId,
        partyId: 2,
        wardId: 102
      },
      {
        candidateId: 3,
        name: 'Michael Brown',
        electionId: this.election.electionId,
        partyId: 1,
        wardId: 103
      },
      {
        candidateId: 4,
        name: 'Emily Davis',
        electionId: this.election.electionId,
        partyId: 3,
        wardId: 104
      }
    ];
  }

  selectCandidate(candidate: Candidate): void {
    this.selectedCandidate = candidate;
  }

  submitVote(): void {
    if (!this.selectedCandidate) {
      this.error = 'Please select a candidate before voting.';
      return;
    }

    this.isSubmitting = true;
    this.error = '';

    // Here you would typically call the voting API
    // For now, we'll simulate the vote submission
    setTimeout(() => {
      this.voteSubmitted.emit({
        electionId: this.election.electionId,
        candidateId: this.selectedCandidate!.candidateId
      });
      this.isSubmitting = false;
      this.closeModal();
    }, 1500);
  }

  closeModal(): void {
    this.selectedCandidate = null;
    this.error = '';
    this.close.emit();
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      this.closeModal();
    }
  }

  getFormattedDate(timestamp: number): string {
    return this.dataService.formatElectionDate(timestamp);
  }
}
