import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DataService, Election, Candidate } from '../../services/data.service';

@Component({
  selector: 'app-election-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './election-card.component.html',
  styleUrls: ['./election-card.component.css']
})
export class ElectionCardComponent implements OnInit, OnChanges {
  @Input() election!: Election;
  @Input() hasVoted: boolean = false;
  
  timeRemaining: string = '';
  formattedStartDate: string = '';
  formattedEndDate: string = '';
  isEligible: boolean = false;

  constructor(
    private dataService: DataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.updateElectionInfo();
    // Update time remaining every minute
    setInterval(() => {
      this.updateElectionInfo();
    }, 60000);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['hasVoted'] || changes['election']) {
      this.updateElectionInfo();
    }
  }

  private updateElectionInfo(): void {
    this.timeRemaining = this.dataService.getTimeRemaining(this.election.endDate);
    this.formattedStartDate = this.dataService.formatElectionDate(this.election.startDate);
    this.formattedEndDate = this.dataService.formatElectionDate(this.election.endDate);
    this.isEligible = this.dataService.isElectionEligible(this.election) && !this.hasVoted;
    
    // Debug logging
    console.log(`Election Card - ${this.election.name}:`, {
      hasVoted: this.hasVoted,
      isEligible: this.isEligible,
      electionEligible: this.dataService.isElectionEligible(this.election)
    });
  }


  onVoteClick(): void {
    if (this.isEligible && !this.hasVoted) {
      this.router.navigate(['/vote', this.election.electionId]);
    } else {
      console.log('Cannot vote: hasVoted =', this.hasVoted, ', isEligible =', this.isEligible);
    }
  }


  onViewDetails(): void {
    // Navigate to election details page (we'll create this later)
    this.router.navigate(['/election', this.election.electionId]);
  }

  getStatusColor(): string {
    if (this.hasVoted) {
      return 'voted';
    } else if (this.isEligible) {
      return 'eligible';
    } else if ((this.election.status === 'ACTIVE' || this.election.status === 'OPENED') && this.election.endDate <= Date.now()) {
      return 'ended';
    } else if (this.election.status === 'PENDING' || this.election.startDate > Date.now()) {
      return 'pending';
    } else {
      return 'ended';
    }
  }

  getStatusText(): string {
    if (this.hasVoted) {
      return '✓ Voted';
    } else if (this.isEligible) {
      return 'Vote Now';
    } else if ((this.election.status === 'ACTIVE' || this.election.status === 'OPENED') && this.election.endDate <= Date.now()) {
      return 'Ended';
    } else if (this.election.status === 'PENDING' || this.election.startDate > Date.now()) {
      return 'Not Started';
    } else {
      return 'Ended';
    }
  }

  getStatusDescription(): string {
    if (this.hasVoted) {
      return 'You have already voted in this election';
    } else if (this.isEligible) {
      return 'You are eligible to vote in this election';
    } else if ((this.election.status === 'ACTIVE' || this.election.status === 'OPENED') && this.election.endDate <= Date.now()) {
      return 'This election has ended';
    } else if (this.election.status === 'PENDING' || this.election.startDate > Date.now()) {
      return 'This election has not started yet';
    } else {
      return 'This election has ended';
    }
  }
}
