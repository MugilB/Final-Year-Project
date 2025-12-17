import { Component, OnInit, OnChanges, Input, ViewChild, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartConfiguration, ChartData, ChartType, registerables } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { DataService, Election, Block, Voter } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-voter-participation-chart',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './voter-participation-chart.component.html',
  styleUrl: './voter-participation-chart.component.css'
})
export class VoterParticipationChartComponent implements OnInit, OnChanges, OnDestroy {
  @Input() elections: Election[] = [];
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
  
  selectedElectionId: string | null = null;
  isLoading = false;
  error: string | null = null;
  
  totalVoters = 0;
  votedCount = 0;
  notVotedCount = 0;
  
  private filterSubscription?: Subscription;
  private isInternalChange = false;

  // Chart configuration
  public pieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom'
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const label = context.label || '';
            const value = typeof context.parsed === 'number' ? context.parsed : 0;
            const dataArray = context.dataset.data as number[];
            const total = dataArray.reduce((a, b) => {
              const numA = typeof a === 'number' ? a : 0;
              const numB = typeof b === 'number' ? b : 0;
              return numA + numB;
            }, 0);
            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
            return `${label}: ${value} (${percentage}%)`;
          }
        }
      }
    }
  };

  public pieChartType: ChartType = 'pie';
  public pieChartData: ChartData<'pie'> = {
    labels: ['Voted', 'Not Voted'],
    datasets: [
      {
        data: [0, 0],
        backgroundColor: [
          'rgba(34, 197, 94, 0.8)',  // Green for Voted
          'rgba(239, 68, 68, 0.8)'   // Red for Not Voted
        ],
        borderColor: [
          'rgba(34, 197, 94, 1)',    // Green border for Voted
          'rgba(239, 68, 68, 1)'     // Red border for Not Voted
        ],
        borderWidth: 1
      }
    ]
  };

  constructor(
    private dataService: DataService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    // Subscribe to chart filter changes from other charts
    this.filterSubscription = this.notificationService.chartFilterChanged$.subscribe(
      (electionId: string | null) => {
        if (!this.isInternalChange && this.selectedElectionId !== electionId) {
          console.log('VoterParticipationChart: Filter changed from external source:', electionId);
          this.selectedElectionId = electionId;
          this.loadChartData();
        }
        this.isInternalChange = false;
      }
    );
    
    if (this.elections.length > 0 && !this.selectedElectionId) {
      this.selectedElectionId = this.elections[0].electionId.toString();
      this.loadChartData();
      // Notify other charts about the initial selection
      this.notificationService.notifyChartFilterChanged(this.selectedElectionId);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['elections'] && !changes['elections'].firstChange) {
      if (this.elections.length > 0 && !this.selectedElectionId) {
        this.selectedElectionId = this.elections[0].electionId.toString();
        this.loadChartData();
        // Notify other charts about the initial selection
        this.notificationService.notifyChartFilterChanged(this.selectedElectionId);
      }
    }
  }

  onElectionChange(): void {
    this.isInternalChange = true;
    // Notify other charts about the filter change
    this.notificationService.notifyChartFilterChanged(this.selectedElectionId);
    this.loadChartData();
  }
  
  ngOnDestroy(): void {
    if (this.filterSubscription) {
      this.filterSubscription.unsubscribe();
    }
  }

  private loadChartData(): void {
    if (!this.selectedElectionId) {
      console.log('No election selected');
      return;
    }

    // Check authentication and admin role before making API call
    const token = this.authService.getToken();
    const user = this.authService.getUser();
    const isAdmin = user?.roles?.includes('ADMIN') || user?.roles?.includes('ROLE_ADMIN');
    
    if (!token) {
      console.error('No authentication token found');
      this.error = 'Authentication required. Please log in again.';
      this.isLoading = false;
      this.resetChartData();
      return;
    }

    if (!isAdmin) {
      console.error('User does not have ADMIN role', { user, roles: user?.roles });
      this.error = 'Admin access required to load voter data.';
      this.isLoading = false;
      this.resetChartData();
      return;
    }

    this.isLoading = true;
    this.error = null;
    const electionId = Number(this.selectedElectionId);

    // Load approved voters and blocks in parallel
    const approvedVoters$ = this.dataService.getApprovedVoters();
    const blocks$ = this.dataService.getBlocksByElection(electionId);

    approvedVoters$.subscribe({
      next: (voters) => {
        this.totalVoters = voters.length;
        console.log('Total approved voters:', this.totalVoters);
        
        blocks$.subscribe({
          next: (blocks) => {
            console.log('Blocks loaded for election:', blocks.length);
            
            // Get unique voters who voted (exclude SYSTEM)
            const uniqueVoters = new Set<string>();
            blocks.forEach(block => {
              if (block.voterId && block.voterId !== 'SYSTEM') {
                uniqueVoters.add(block.voterId);
              }
            });
            
            this.votedCount = uniqueVoters.size;
            this.notVotedCount = Math.max(0, this.totalVoters - this.votedCount);
            
            console.log('Voted:', this.votedCount, 'Not voted:', this.notVotedCount);
            
            // Update chart data
            this.updateChartData();
            this.isLoading = false;
          },
          error: (error) => {
            console.error('Error loading blocks:', error);
            const errorMessage = this.getErrorMessage(error);
            this.error = `Failed to load vote data: ${errorMessage}`;
            this.isLoading = false;
            this.resetChartData();
          }
        });
      },
      error: (error) => {
        console.error('Error loading approved voters:', error);
        const errorMessage = this.getErrorMessage(error);
        this.error = `Failed to load voter data: ${errorMessage}`;
        this.isLoading = false;
        this.resetChartData();
      }
    });
  }

  private getErrorMessage(error: any): string {
    if (error?.status === 401) {
      return 'Unauthorized. Your session may have expired. Please log in again.';
    } else if (error?.status === 403) {
      return 'Access forbidden. Admin role required.';
    } else if (error?.status === 404) {
      return 'Resource not found.';
    } else if (error?.status === 500) {
      return 'Server error. Please try again later.';
    } else if (error?.error?.message) {
      return error.error.message;
    } else if (error?.message) {
      return error.message;
    } else {
      return 'An unexpected error occurred.';
    }
  }

  private updateChartData(): void {
    this.pieChartData = {
      labels: ['Voted', 'Not Voted'],
      datasets: [
        {
          data: [this.votedCount, this.notVotedCount],
          backgroundColor: [
            'rgba(34, 197, 94, 0.8)',  // Green for Voted
            'rgba(239, 68, 68, 0.8)'   // Red for Not Voted
          ],
          borderColor: [
            'rgba(34, 197, 94, 1)',    // Green border for Voted
            'rgba(239, 68, 68, 1)'     // Red border for Not Voted
          ],
          borderWidth: 1
        }
      ]
    };
    
    if (this.chart) {
      this.chart.update();
    }
  }

  private resetChartData(): void {
    this.votedCount = 0;
    this.notVotedCount = 0;
    this.totalVoters = 0;
    this.updateChartData();
  }
}

