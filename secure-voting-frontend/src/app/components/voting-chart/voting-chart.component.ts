import { Component, OnInit, OnChanges, Input, ViewChild, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartConfiguration, ChartData, ChartType, registerables } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { DataService, Election, Candidate } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';
import { Subscription } from 'rxjs';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-voting-chart',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './voting-chart.component.html',
  styleUrl: './voting-chart.component.css'
})
export class VotingChartComponent implements OnInit, OnChanges, OnDestroy {
  @Input() elections: Election[] = [];
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
  
  selectedElectionId: string | null = null;
  candidates: Candidate[] = [];
  isLoading = false;
  error: string | null = null;
  private filterSubscription?: Subscription;
  private isInternalChange = false;

  // Chart configuration
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: {
      intersect: false,
    },
    plugins: {
      legend: {
        display: true,
        position: 'top',
      },
      title: {
        display: true,
        text: 'Vote Distribution by Candidate'
      }
    },
    scales: {
      x: {
        type: 'category',
        display: true,
        title: {
          display: true,
          text: 'Candidates'
        }
      },
      y: {
        type: 'linear',
        display: true,
        beginAtZero: true,
        title: {
          display: true,
          text: 'Votes'
        },
        ticks: {
          stepSize: 5
        }
      }
    }
  };

  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Votes',
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',
          'rgba(16, 185, 129, 0.8)',
          'rgba(245, 158, 11, 0.8)',
          'rgba(239, 68, 68, 0.8)',
          'rgba(139, 92, 246, 0.8)',
          'rgba(236, 72, 153, 0.8)',
          'rgba(6, 182, 212, 0.8)',
          'rgba(34, 197, 94, 0.8)'
        ],
        borderColor: [
          'rgba(59, 130, 246, 1)',
          'rgba(16, 185, 129, 1)',
          'rgba(245, 158, 11, 1)',
          'rgba(239, 68, 68, 1)',
          'rgba(139, 92, 246, 1)',
          'rgba(236, 72, 153, 1)',
          'rgba(6, 182, 212, 1)',
          'rgba(34, 197, 94, 1)'
        ],
        borderWidth: 1
      }
    ]
  };

  constructor(
    private dataService: DataService,
    private notificationService: NotificationService
  ) {
    // Register Chart.js components
    Chart.register(...registerables);
  }

  ngOnInit() {
    console.log('VotingChartComponent ngOnInit called');
    console.log('Available elections:', this.elections);
    
    // Subscribe to chart filter changes from other charts
    this.filterSubscription = this.notificationService.chartFilterChanged$.subscribe(
      (electionId: string | null) => {
        if (!this.isInternalChange && this.selectedElectionId !== electionId) {
          console.log('VotingChart: Filter changed from external source:', electionId);
          this.selectedElectionId = electionId;
          this.loadRealChartData();
        }
        this.isInternalChange = false;
      }
    );
    
    // Set default election selection if elections are available
    if (this.elections && this.elections.length > 0) {
      this.selectedElectionId = this.elections[0].electionId.toString();
      this.loadRealChartData();
      // Notify other charts about the initial selection
      this.notificationService.notifyChartFilterChanged(this.selectedElectionId);
    } else {
      console.log('No elections available yet, waiting for input...');
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['elections'] && changes['elections'].currentValue) {
      console.log('Elections input changed:', changes['elections'].currentValue);
      if (this.elections && this.elections.length > 0 && !this.selectedElectionId) {
        this.selectedElectionId = this.elections[0].electionId.toString();
        this.loadRealChartData();
        // Notify other charts about the initial selection
        this.notificationService.notifyChartFilterChanged(this.selectedElectionId);
      }
    }
  }

  onElectionChange() {
    this.isInternalChange = true;
    // Notify other charts about the filter change
    this.notificationService.notifyChartFilterChanged(this.selectedElectionId);
    this.loadRealChartData();
  }
  
  ngOnDestroy() {
    if (this.filterSubscription) {
      this.filterSubscription.unsubscribe();
    }
  }

  private loadRealChartData() {
    if (!this.selectedElectionId) {
      console.log('No election selected');
      return;
    }

    this.isLoading = true;
    this.error = null;
    console.log('Loading real chart data for election:', this.selectedElectionId);

    // First, get approved candidates for the selected election
    this.dataService.getApprovedCandidatesByElection(Number(this.selectedElectionId)).subscribe({
      next: (candidates) => {
        console.log('Approved candidates loaded:', candidates);
        this.candidates = candidates;
        
        // Then get vote counts for the election
        this.dataService.getCandidateVoteCounts(Number(this.selectedElectionId)).subscribe({
          next: (voteCounts) => {
            console.log('Vote counts loaded:', voteCounts);
            this.updateChartDataWithVoteCounts(voteCounts);
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading vote counts:', error);
            this.error = 'Failed to load vote counts';
            this.isLoading = false;
            // Fallback to static data if vote counts fail
            this.loadStaticChartData();
          }
        });
      },
      error: (error) => {
        console.error('Error loading candidates:', error);
        this.error = 'Failed to load candidates';
        this.isLoading = false;
        // Fallback to static data if candidates fail
        this.loadStaticChartData();
      }
    });
  }

  private initializeStaticData() {
    // Create mock candidates data
    this.candidates = [
      { candidateId: 1, name: 'Vijay', electionId: 1, partyId: undefined, wardId: undefined, candidateDetails: undefined },
      { candidateId: 2, name: 'EPS', electionId: 1, partyId: 1, wardId: undefined, candidateDetails: undefined },
      { candidateId: 3, name: 'Kamal', electionId: 1, partyId: 2, wardId: undefined, candidateDetails: undefined },
      { candidateId: 4, name: 'Rajini', electionId: 1, partyId: 3, wardId: undefined, candidateDetails: undefined }
    ];
    
    this.loadStaticChartData();
  }

  private loadStaticChartData() {
    this.isLoading = false;
    this.error = null;
    console.log('Loading static chart data for election:', this.selectedElectionId);
    
    // Different static data based on selected election
    let staticVoteCounts: { [key: string]: number };
    let candidates: any[];
    
    switch (this.selectedElectionId) {
      case "1": // Tamil Nadu Assembly Election 2024
        candidates = [
          { candidateId: 1, name: 'Vijay', electionId: 1, partyId: undefined, wardId: undefined, candidateDetails: undefined },
          { candidateId: 2, name: 'EPS', electionId: 1, partyId: 1, wardId: undefined, candidateDetails: undefined },
          { candidateId: 3, name: 'Kamal', electionId: 1, partyId: 2, wardId: undefined, candidateDetails: undefined },
          { candidateId: 4, name: 'Rajini', electionId: 1, partyId: 3, wardId: undefined, candidateDetails: undefined }
        ];
        staticVoteCounts = {
          'Vijay': 45,
          'EPS': 38,
          'Kamal': 52,
          'Rajini': 41
        };
        break;
        
      case "2": // Lok Sabha Election 2024
        candidates = [
          { candidateId: 5, name: 'Modi', electionId: 2, partyId: 1, wardId: undefined, candidateDetails: undefined },
          { candidateId: 6, name: 'Rahul', electionId: 2, partyId: 2, wardId: undefined, candidateDetails: undefined },
          { candidateId: 7, name: 'Mamata', electionId: 2, partyId: 3, wardId: undefined, candidateDetails: undefined }
        ];
        staticVoteCounts = {
          'Modi': 78,
          'Rahul': 65,
          'Mamata': 58
        };
        break;
        
      case "3": // Municipal Corporation Election
        candidates = [
          { candidateId: 8, name: 'Kumar', electionId: 3, partyId: 1, wardId: undefined, candidateDetails: undefined },
          { candidateId: 9, name: 'Singh', electionId: 3, partyId: 2, wardId: undefined, candidateDetails: undefined },
          { candidateId: 10, name: 'Patel', electionId: 3, partyId: 3, wardId: undefined, candidateDetails: undefined },
          { candidateId: 11, name: 'Sharma', electionId: 3, partyId: 4, wardId: undefined, candidateDetails: undefined }
        ];
        staticVoteCounts = {
          'Kumar': 32,
          'Singh': 28,
          'Patel': 35,
          'Sharma': 25
        };
        break;
        
      default:
        candidates = this.candidates;
        staticVoteCounts = {
          'Vijay': 45,
          'EPS': 38,
          'Kamal': 52,
          'Rajini': 41
        };
    }
    
    this.candidates = candidates;
    console.log('Candidates loaded:', this.candidates.length);
    console.log('Vote counts:', staticVoteCounts);
    this.updateChartDataWithVoteCounts(staticVoteCounts);
  }

  private updateChartData() {
    // Mock vote data - fallback when real data is not available
    const mockVoteCounts = this.candidates.map((_, index) => Math.floor(Math.random() * 100) + 10);
    this.updateChartDataWithCounts(mockVoteCounts);
  }

  private updateChartDataWithVoteCounts(voteCounts: { [candidateName: string]: number }) {
    // Get vote counts for each candidate, defaulting to 0 if not found
    const counts = this.candidates.map(candidate => voteCounts[candidate.name] || 0);
    this.updateChartDataWithCounts(counts);
  }

  private updateChartDataWithCounts(counts: number[]) {
    console.log('Updating chart with counts:', counts);
    console.log('Candidate names:', this.candidates.map(candidate => candidate.name));
    this.barChartData = {
      labels: this.candidates.map(candidate => candidate.name),
      datasets: [
        {
          data: counts,
          label: 'Votes',
          backgroundColor: [
            'rgba(59, 130, 246, 0.8)',
            'rgba(16, 185, 129, 0.8)',
            'rgba(245, 158, 11, 0.8)',
            'rgba(239, 68, 68, 0.8)',
            'rgba(139, 92, 246, 0.8)',
            'rgba(236, 72, 153, 0.8)',
            'rgba(6, 182, 212, 0.8)',
            'rgba(34, 197, 94, 0.8)'
          ],
          borderColor: [
            'rgba(59, 130, 246, 1)',
            'rgba(16, 185, 129, 1)',
            'rgba(245, 158, 11, 1)',
            'rgba(239, 68, 68, 1)',
            'rgba(139, 92, 246, 1)',
            'rgba(236, 72, 153, 1)',
            'rgba(6, 182, 212, 1)',
            'rgba(34, 197, 94, 1)'
          ],
          borderWidth: 1
        }
      ]
    };
    
    // Force chart update
    setTimeout(() => {
      if (this.chart) {
        this.chart.update();
        console.log('Chart updated');
      }
    }, 100);
  }

  getSelectedElectionName(): string {
    const election = this.elections.find(e => e.electionId === Number(this.selectedElectionId));
    return election ? election.name : 'Select Election';
  }
}
