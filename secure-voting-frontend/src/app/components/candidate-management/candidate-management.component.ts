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
    } else {
      // By default, exclude REJECTED candidates unless explicitly filtered
      filtered = filtered.filter(candidate => {
        const candidateStatus = this.getCandidateStatus(candidate);
        return candidateStatus !== 'REJECTED';
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
      const candidate = this.selectedNominationCandidate;
      const candidateId = candidate.candidateId;
      const currentStatus = this.getCandidateStatus(candidate);
      const action = currentStatus === 'APPROVED' ? 'move to pending' : 'approve';
      
      if (confirm(`Are you sure you want to ${action} "${candidate.name}"?`)) {
        const newStatus = currentStatus === 'APPROVED' ? 'PENDING' : 'APPROVED';
        
        const request: UpdateCandidateStatusRequest = {
          status: newStatus,
          reviewNotes: `${action} by admin`,
          reviewedBy: 'Admin' // You can get this from auth service
        };
        
        this.dataService.updateCandidateStatus(candidateId, request).subscribe({
          next: (updatedCandidate) => {
            console.log('Candidate status updated successfully:', updatedCandidate);
            
            // Update the candidate in the local array
            const index = this.candidates.findIndex(
              c => c.candidateId === candidateId
            );
            if (index !== -1) {
              this.candidates[index] = updatedCandidate;
            }
            
            alert(`Candidate ${action}d successfully!`);
            this.closeNominationModal();
            // Optionally reload to ensure data consistency, but local update provides immediate feedback
            // this.loadCandidates();
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
      const candidate = this.selectedNominationCandidate;
      const candidateId = candidate.candidateId;
      const currentStatus = this.getCandidateStatus(candidate);
      const action = currentStatus === 'REJECTED' ? 'move to pending' : 'reject';
      
      if (confirm(`Are you sure you want to ${action} "${candidate.name}"?`)) {
        const newStatus = currentStatus === 'REJECTED' ? 'PENDING' : 'REJECTED';
        
        const request: UpdateCandidateStatusRequest = {
          status: newStatus,
          reviewNotes: `${action} by admin`,
          reviewedBy: 'Admin' // You can get this from auth service
        };
        
        this.dataService.updateCandidateStatus(candidateId, request).subscribe({
          next: (updatedCandidate) => {
            console.log('Candidate status updated successfully:', updatedCandidate);
            
            // If candidate was rejected, remove from local array immediately
            if (newStatus === 'REJECTED') {
              const index = this.candidates.findIndex(
                c => c.candidateId === candidateId
              );
              if (index !== -1) {
                this.candidates.splice(index, 1);
              }
            } else {
              // Update the candidate in the local array
              const index = this.candidates.findIndex(
                c => c.candidateId === candidateId
              );
              if (index !== -1) {
                this.candidates[index] = updatedCandidate;
              }
            }
            
            alert(`Candidate ${action}ed successfully!`);
            this.closeNominationModal();
            // Optionally reload to ensure data consistency, but local update provides immediate feedback
            // this.loadCandidates();
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
    // Fetch the candidate by ID to ensure we have the latest data with candidateDetails
    this.dataService.getCandidateById(candidate.candidateId).subscribe({
      next: (freshCandidate) => {
        this.isEditingCandidate = true;
        this.editingCandidate = freshCandidate; // Use the fresh candidate with full details
        this.showCandidateModal = true;
      },
      error: (error) => {
        console.error('Error fetching candidate details:', error);
        // Fallback to using the candidate from the list
        this.isEditingCandidate = true;
        this.editingCandidate = candidate;
        this.showCandidateModal = true;
      }
    });
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
    // Reset editing state after a short delay to prevent change detection issues
    setTimeout(() => {
      this.isEditingCandidate = false;
      this.editingCandidate = null;
    }, 100);
  }

  saveCandidate(event: { candidate: CreateCandidateRequest | UpdateCandidateRequest, isEdit: boolean }) {
    if (event.isEdit && this.editingCandidate) {
      const updateRequest = event.candidate as UpdateCandidateRequest;
      const candidateId = this.editingCandidate.candidateId;
      
      // Update existing candidate
      this.dataService.updateCandidate(candidateId, updateRequest).subscribe({
        next: (updatedCandidate) => {
          console.log('Candidate updated successfully:', updatedCandidate);
          
          // Immediately update the local candidate with the data we know was saved
          // This ensures the UI reflects the changes right away
          const index = this.candidates.findIndex(c => c.candidateId === candidateId);
          if (index !== -1) {
            // Merge the updated data with the existing candidate
            const existingDetails = this.candidates[index].candidateDetails;
            const updatedDetails = updatedCandidate.candidateDetails;
            
            // Merge all candidateDetails fields from the update request
            this.candidates[index] = {
              ...this.candidates[index],
              ...updatedCandidate,
              candidateDetails: {
                candidateId: candidateId, // Ensure candidateId is always present
                ...existingDetails,
                ...updatedDetails,
                // Update all fields from the request
                email: updateRequest.email !== undefined ? updateRequest.email : (updatedDetails?.email || existingDetails?.email),
                phoneNumber: updateRequest.phoneNumber !== undefined ? updateRequest.phoneNumber : (updatedDetails?.phoneNumber || existingDetails?.phoneNumber),
                gender: updateRequest.gender !== undefined ? updateRequest.gender : (updatedDetails?.gender || existingDetails?.gender),
                age: updateRequest.age !== undefined ? updateRequest.age : (updatedDetails?.age || existingDetails?.age),
                address: updateRequest.address !== undefined ? updateRequest.address : (updatedDetails?.address || existingDetails?.address),
                aadharCardLink: updateRequest.aadharCardLink !== undefined ? updateRequest.aadharCardLink : (updatedDetails?.aadharCardLink || existingDetails?.aadharCardLink),
                candidateImageLink: updateRequest.candidateImageLink !== undefined ? updateRequest.candidateImageLink : (updatedDetails?.candidateImageLink || existingDetails?.candidateImageLink),
                biography: updateRequest.biography !== undefined ? updateRequest.biography : (updatedDetails?.biography || existingDetails?.biography || undefined),
                manifestoSummary: updateRequest.manifestoSummary !== undefined ? updateRequest.manifestoSummary : (updatedDetails?.manifestoSummary || existingDetails?.manifestoSummary || undefined)
              }
            };
          }
          
          // Close modal first (before alert to avoid blocking)
          this.closeCandidateModal();
          
          // Fetch the candidate again after a short delay to ensure backend has persisted
          // This ensures we have the latest data when the user reopens the modal
          setTimeout(() => {
            this.dataService.getCandidateById(candidateId).subscribe({
              next: (freshCandidate) => {
                // Update the candidate in the list with the fresh data from backend
                const idx = this.candidates.findIndex(c => c.candidateId === candidateId);
                if (idx !== -1) {
                  this.candidates[idx] = freshCandidate;
                }
                // Only update editingCandidate if modal is not open to prevent infinite loop
                if (!this.showCandidateModal && this.editingCandidate?.candidateId === candidateId) {
                  this.editingCandidate = freshCandidate;
                }
              },
              error: (fetchError) => {
                console.error('Error fetching updated candidate:', fetchError);
                // Silently fail - we already updated the local data
              }
            });
          }, 500);
          
          // Show alert
          setTimeout(() => {
            alert('Candidate updated successfully!');
          }, 100);
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
          setTimeout(() => {
            alert('Candidate created successfully!');
          }, 100);
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
