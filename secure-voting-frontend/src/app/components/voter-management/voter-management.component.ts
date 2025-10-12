import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService } from '../../services/data.service';

export interface VoterDetails {
  voterId: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  address: string;
  phoneNumber: string;
  gender: string;
  bloodGroup: string;
  wardId: number;
  dob: number;
  aadharCardLink: string;
  profilePictureLink: string;
  approvalStatus: number; // 0=rejected, 1=approved, 2=pending
  createdAt: number;
}

export interface Voter {
  username: string;
  email: string;
  role: string;
  isActive: boolean;
  approvalStatus: number;
  createdAt: number;
  voterDetails: VoterDetails;
}

@Component({
  selector: 'app-voter-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './voter-management.component.html',
  styleUrls: ['./voter-management.component.css']
})
export class VoterManagementComponent implements OnInit {
  voters: Voter[] = [];
  loading = false;
  selectedVoter: Voter | null = null;
  showVoterModal = false;
  // Removed filter - only show pending voters

  constructor(private dataService: DataService) { }

  ngOnInit(): void {
    this.loadVoters();
  }

  loadVoters(): void {
    this.loading = true;
    console.log('Loading pending voters...');
    this.dataService.getPendingVoters().subscribe({
      next: (voters) => {
        console.log('Pending voters loaded:', voters);
        this.voters = voters;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading pending voters:', error);
        this.loading = false;
      }
    });
  }

  // Removed getFilteredVoters - only show pending voters

  getStatusLabel(status: number): string {
    switch (status) {
      case 0: return 'REJECTED';
      case 1: return 'APPROVED';
      case 2: return 'PENDING';
      default: return 'UNKNOWN';
    }
  }

  getStatusClass(status: number): string {
    switch (status) {
      case 0: return 'status-rejected';
      case 1: return 'status-approved';
      case 2: return 'status-pending';
      default: return 'status-unknown';
    }
  }

  viewVoterDetails(voter: Voter): void {
    this.selectedVoter = voter;
    this.showVoterModal = true;
  }

  closeVoterModal(): void {
    this.showVoterModal = false;
    this.selectedVoter = null;
  }

  approveVoter(): void {
    if (!this.selectedVoter) return;
    
    const voterId = this.selectedVoter.voterDetails?.voterId || this.selectedVoter.username;
    if (!voterId) {
      console.error('Cannot approve voter: no valid voterId found');
      return;
    }
    
    this.dataService.updateVoterStatus(voterId, {
      status: 'APPROVED',
      reviewNotes: 'Approved by admin',
      reviewedBy: 'admin'
    }).subscribe({
      next: (updatedVoter) => {
        console.log('Voter approved successfully');
        this.closeVoterModal();
        this.loadVoters(); // Reload the list to remove approved voter
      },
      error: (error) => {
        console.error('Error approving voter:', error);
      }
    });
  }

  rejectVoter(): void {
    if (!this.selectedVoter) return;
    
    const voterId = this.selectedVoter.voterDetails?.voterId || this.selectedVoter.username;
    if (!voterId) {
      console.error('Cannot reject voter: no valid voterId found');
      return;
    }
    
    this.dataService.updateVoterStatus(voterId, {
      status: 'REJECTED',
      reviewNotes: 'Rejected by admin',
      reviewedBy: 'admin'
    }).subscribe({
      next: (updatedVoter) => {
        console.log('Voter rejected successfully');
        this.closeVoterModal();
        this.loadVoters(); // Reload the list to remove rejected voter
      },
      error: (error) => {
        console.error('Error rejecting voter:', error);
      }
    });
  }

  getApproveButtonText(): string {
    if (!this.selectedVoter) return 'Approve Voter';
    switch (this.selectedVoter.approvalStatus) {
      case 0: return 'Approve Voter';
      case 1: return 'Reject Voter';
      case 2: return 'Approve Voter';
      default: return 'Approve Voter';
    }
  }

  getRejectButtonText(): string {
    if (!this.selectedVoter) return 'Reject Voter';
    switch (this.selectedVoter.approvalStatus) {
      case 0: return 'Approve Voter';
      case 1: return 'Reject Voter';
      case 2: return 'Reject Voter';
      default: return 'Reject Voter';
    }
  }

  shouldShowApproveButton(): boolean {
    if (!this.selectedVoter) return true;
    return this.selectedVoter.approvalStatus !== 1; // Show if not already approved
  }

  shouldShowRejectButton(): boolean {
    if (!this.selectedVoter) return true;
    return this.selectedVoter.approvalStatus !== 0; // Show if not already rejected
  }

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString();
  }

  getWardName(wardId: number): string {
    const wardNames: { [key: number]: string } = {
      1: 'Ward 1 - Central',
      2: 'Ward 2 - North',
      3: 'Ward 3 - South',
      4: 'Ward 4 - East',
      5: 'Ward 5 - West'
    };
    return wardNames[wardId] || `Ward ${wardId}`;
  }

  getVoterImageLink(voter: Voter): string {
    return voter.voterDetails?.profilePictureLink || 'N/A';
  }

  getVoterAadharLink(voter: Voter): string {
    return voter.voterDetails?.aadharCardLink || 'N/A';
  }

  getVoterEmail(voter: Voter): string {
    return voter.voterDetails?.email || voter.email || 'N/A';
  }

  getVoterPhoneNumber(voter: Voter): string {
    return voter.voterDetails?.phoneNumber || 'N/A';
  }

  getVoterAddress(voter: Voter): string {
    return voter.voterDetails?.address || 'N/A';
  }

  getVoterGender(voter: Voter): string {
    return voter.voterDetails?.gender || 'N/A';
  }

  getVoterBloodGroup(voter: Voter): string {
    return voter.voterDetails?.bloodGroup || 'N/A';
  }

  getVoterFirstName(voter: Voter): string {
    return voter.voterDetails?.firstName || 'N/A';
  }

  getVoterLastName(voter: Voter): string {
    return voter.voterDetails?.lastName || 'N/A';
  }
}
