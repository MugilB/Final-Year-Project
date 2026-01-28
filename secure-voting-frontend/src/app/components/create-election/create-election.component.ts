import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { DataService } from '../../services/data.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-create-election',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './create-election.component.html',
  styleUrl: './create-election.component.css'
})
export class CreateElectionComponent {
  electionData = {
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    rules: '',
    status: 'SCHEDULED'
  };

  isLoading = false;

  constructor(
    private router: Router,
    private dataService: DataService,
    private notificationService: NotificationService
  ) { }

  goBack() {
    this.router.navigate(['/admin']);
  }

  onSubmit() {
    if (!this.electionData.name || !this.electionData.startDate || !this.electionData.endDate) {
      alert('Please fill in all required fields.');
      return;
    }

    this.isLoading = true;

    // Convert datetime-local strings to timestamps if backend expects timestamps
    // AdminDashboard was sending them as is? Or converting?
    // Let's look at DataService later. For now, sending as object.
    // Actually, AdminDashboard interface had `startDate: number`.
    // So I should convert to timestamp.

    const payload = {
      ...this.electionData,
      startDate: new Date(this.electionData.startDate).getTime(),
      endDate: new Date(this.electionData.endDate).getTime()
    };

    this.dataService.createElection(payload).subscribe({
      next: (response) => {
        this.isLoading = false;
        alert('Election created successfully!');
        this.notificationService.notifyElectionUpdated();
        this.router.navigate(['/admin']);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error creating election:', error);
        alert('Failed to create election.');
      }
    });
  }
}
