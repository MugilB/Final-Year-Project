import { Component, OnInit, ViewChild, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataService, Candidate, Election, CreateCandidateRequest, UpdateCandidateRequest, UpdateCandidateStatusRequest } from '../../services/data.service';
import { CandidateModalComponent } from '../candidate-modal/candidate-modal.component';

@Component({
  selector: 'app-candidate-management',
  standalone: true,
  imports: [CommonModule, FormsModule, CandidateModalComponent],
  templateUrl: './candidate-management.component.html',
  styleUrls: ['./candidate-management.component.css']
})
export class CandidateManagementComponent implements OnInit, OnChanges {
  @ViewChild('candidateModal') candidateModalRef!: CandidateModalComponent;
  @Input() selectedElectionId: number | null = null;

  candidates: Candidate[] = [];
  elections: Election[] = [];
  isLoading = false;
  error: string | null = null;
  
  // Modal properties
  showCandidateModal = false;
  isEditingCandidate = false;
  editingCandidate: Candidate | null = null;

  // Nomination modal properties
  showNominationModal = false;
  selectedNominationCandidate: Candidate | null = null;

  // Filter properties
  selectedElectionFilter = 'all';
  selectedStatusFilter = 'all';
  searchTerm = '';

  constructor(private dataService: DataService) {}

  ngOnInit() {
    this.loadCandidates();
    this.loadElections();
  }

  ngOnChanges() {
    // When selectedElectionId changes, update the filter
    if (this.selectedElectionId !== null) {
      this.selectedElectionFilter = this.selectedElectionId.toString();
    }
  }

  loadCandidates() {
    this.isLoading = true;
    this.error = null;
    
    this.dataService.getAllCandidates().subscribe({
      next: (candidates) => {
        this.candidates = candidates;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading candidates:', error);
        this.error = 'Failed to load candidates';
        this.isLoading = false;
      }
    });
  }

  loadElections() {
    this.dataService.getAllElections().subscribe({
      next: (elections) => {
        this.elections = elections;
      },
      error: (error) => {
        console.error('Error loading elections:', error);
      }
    });
  }

  get filteredCandidates(): Candidate[] {
    let filtered = this.candidates;

    // Filter by election
    if (this.selectedElectionFilter !== 'all') {
      const electionId = parseInt(this.selectedElectionFilter);
      filtered = filtered.filter(candidate => candidate.electionId === electionId);
    }

    // Filter by status
    if (this.selectedStatusFilter !== 'all') {
      filtered = filtered.filter(candidate => {
        const candidateStatus = this.getCandidateStatus(candidate);
        return candidateStatus === this.selectedStatusFilter;
      });
    }

    // Filter by search term
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(candidate => 
        candidate.name.toLowerCase().includes(term)
      );
    }

    return filtered;
  }

  getElectionName(electionId: number): string {
    const election = this.elections.find(e => e.electionId === electionId);
    return election ? election.name : `Election ${electionId}`;
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'status-badge status-pending';
    
    switch (status.toUpperCase()) {
      case 'APPROVED':
        return 'status-badge status-approved';
      case 'REJECTED':
        return 'status-badge status-rejected';
      case 'PENDING':
      default:
        return 'status-badge status-pending';
    }
  }

  // Temporary type assertion to help TypeScript recognize the status property
  getCandidateStatus(candidate: any): string {
    return candidate.status || 'PENDING';
  }

  getCandidateImageLink(candidate: any): string | null {
    return candidate?.candidateDetails?.candidateImageLink || null;
  }

  getCandidateAadharLink(candidate: any): string | null {
    return candidate?.candidateDetails?.aadharCardLink || null;
  }

  getCandidateEmail(candidate: any): string | null {
    return candidate?.candidateDetails?.email || null;
  }

  getCandidatePhone(candidate: any): string | null {
    return candidate?.candidateDetails?.phoneNumber || null;
  }

  getCandidateGender(candidate: any): string | null {
    return candidate?.candidateDetails?.gender || null;
  }

  getCandidateAge(candidate: any): number | null {
    return candidate?.candidateDetails?.age || null;
  }

  getCandidateAddress(candidate: any): string | null {
    return candidate?.candidateDetails?.address || null;
  }

  getCandidateBiography(candidate: any): string | null {
    return candidate?.candidateDetails?.biography || null;
  }

  getCandidateManifesto(candidate: any): string | null {
    return candidate?.candidateDetails?.manifestoSummary || null;
  }

  getPartyName(partyId: number | null | undefined): string {
    if (!partyId) return 'Not specified';
    
    // Mapping based on actual database party_details table
    const partyNames: { [key: number]: string } = {
      1: 'TVK',
      2: 'Independent Candidate'
    };
    
    return partyNames[partyId] || `Party ${partyId}`;
  }

  getApproveButtonText(candidate: any): string {
    const status = this.getCandidateStatus(candidate);
    return status === 'APPROVED' ? 'Move to Pending' : 'Approve Candidate';
  }

  getRejectButtonText(candidate: any): string {
    const status = this.getCandidateStatus(candidate);
    return status === 'REJECTED' ? 'Move to Pending' : 'Reject Candidate';
  }

  shouldShowApproveButton(candidate: any): boolean {
    const status = this.getCandidateStatus(candidate);
    return status === 'PENDING' || status === 'APPROVED';
  }

  shouldShowRejectButton(candidate: any): boolean {
    const status = this.getCandidateStatus(candidate);
    return status === 'PENDING' || status === 'REJECTED';
  }

  viewNomination(candidate: Candidate): void {
    this.selectedNominationCandidate = candidate;
    this.showNominationModal = true;
  }

  closeNominationModal(): void {
    this.showNominationModal = false;
    this.selectedNominationCandidate = null;
  }

  approveCandidate(): void {
    if (this.selectedNominationCandidate) {
      const currentStatus = this.getCandidateStatus(this.selectedNominationCandidate);
      const action = currentStatus === 'APPROVED' ? 'move to pending' : 'approve';
      
      if (confirm(`Are you sure you want to ${action} "${this.selectedNominationCandidate.name}"?`)) {
        const newStatus = currentStatus === 'APPROVED' ? 'PENDING' : 'APPROVED';
        
        const request: UpdateCandidateStatusRequest = {
          status: newStatus,
          reviewNotes: `${action} by admin`,
          reviewedBy: 'Admin' // You can get this from auth service
        };
        
        this.dataService.updateCandidateStatus(this.selectedNominationCandidate.candidateId, request).subscribe({
          next: (updatedCandidate) => {
            console.log('Candidate status updated successfully:', updatedCandidate);
            alert(`Candidate ${action}d successfully!`);
            this.closeNominationModal();
            this.loadCandidates(); // Refresh the list
          },
          error: (error) => {
            console.error('Error updating candidate status:', error);
            alert(`Failed to ${action} candidate: ${error.status} - ${error.message || 'Unknown error'}`);
          }
        });
      }
    }
  }

  rejectCandidate(): void {
    if (this.selectedNominationCandidate) {
      const currentStatus = this.getCandidateStatus(this.selectedNominationCandidate);
      const action = currentStatus === 'REJECTED' ? 'move to pending' : 'reject';
      
      if (confirm(`Are you sure you want to ${action} "${this.selectedNominationCandidate.name}"?`)) {
        const newStatus = currentStatus === 'REJECTED' ? 'PENDING' : 'REJECTED';
        
        const request: UpdateCandidateStatusRequest = {
          status: newStatus,
          reviewNotes: `${action} by admin`,
          reviewedBy: 'Admin' // You can get this from auth service
        };
        
        this.dataService.updateCandidateStatus(this.selectedNominationCandidate.candidateId, request).subscribe({
          next: (updatedCandidate) => {
            console.log('Candidate status updated successfully:', updatedCandidate);
            alert(`Candidate ${action}ed successfully!`);
            this.closeNominationModal();
            this.loadCandidates(); // Refresh the list
          },
          error: (error) => {
            console.error('Error updating candidate status:', error);
            alert(`Failed to ${action} candidate: ${error.status} - ${error.message || 'Unknown error'}`);
          }
        });
      }
    }
  }

  createCandidate() {
    this.isEditingCandidate = false;
    this.editingCandidate = null;
    this.showCandidateModal = true;
  }

  editCandidate(candidate: Candidate) {
    this.isEditingCandidate = true;
    this.editingCandidate = candidate;
    this.showCandidateModal = true;
  }

  deleteCandidate(candidate: Candidate) {
    if (confirm(`Are you sure you want to delete candidate "${candidate.name}"?`)) {
      this.dataService.deleteCandidate(candidate.candidateId).subscribe({
        next: () => {
          this.loadCandidates();
          alert('Candidate deleted successfully!');
        },
        error: (error) => {
          console.error('Error deleting candidate:', error);
          alert(`Failed to delete candidate: ${error.status} - ${error.message || 'Unknown error'}`);
        }
      });
    }
  }

  closeCandidateModal() {
    this.showCandidateModal = false;
    this.isEditingCandidate = false;
    this.editingCandidate = null;
  }

  saveCandidate(event: { candidate: CreateCandidateRequest | UpdateCandidateRequest, isEdit: boolean }) {
    if (event.isEdit && this.editingCandidate) {
      // Update existing candidate
      this.dataService.updateCandidate(this.editingCandidate.candidateId, event.candidate as UpdateCandidateRequest).subscribe({
        next: (updatedCandidate) => {
          console.log('Candidate updated successfully:', updatedCandidate);
          this.loadCandidates();
          this.closeCandidateModal();
          alert('Candidate updated successfully!');
        },
        error: (error) => {
          console.error('Error updating candidate:', error);
          console.error('Error status:', error.status);
          console.error('Error message:', error.message);
          console.error('Error details:', error.error);
          this.candidateModalRef?.resetLoadingState();
          alert(`Failed to update candidate. Error: ${error.status} - ${error.message || 'Unknown error'}`);
        }
      });
    } else {
      // Create new candidate
      this.dataService.createCandidate(event.candidate as CreateCandidateRequest).subscribe({
        next: (newCandidate) => {
          console.log('Candidate created successfully:', newCandidate);
          this.loadCandidates();
          this.closeCandidateModal();
          alert('Candidate created successfully!');
        },
        error: (error) => {
          console.error('Error creating candidate:', error);
          console.error('Error status:', error.status);
          console.error('Error message:', error.message);
          console.error('Error details:', error.error);
          this.candidateModalRef?.resetLoadingState();
          alert(`Failed to create candidate. Error: ${error.status} - ${error.message || 'Unknown error'}`);
        }
      });
    }
  }

  clearFilters() {
    this.selectedElectionFilter = 'all';
    this.selectedStatusFilter = 'all';
    this.searchTerm = '';
  }
}
