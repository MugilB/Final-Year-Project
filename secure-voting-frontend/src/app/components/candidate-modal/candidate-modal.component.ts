import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateCandidateRequest, UpdateCandidateRequest, Election } from '../../services/data.service';

@Component({
  selector: 'app-candidate-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './candidate-modal.component.html',
  styleUrls: ['./candidate-modal.component.css']
})
export class CandidateModalComponent implements OnInit {
  @Input() isVisible = false;
  @Input() isEditMode = false;
  @Input() candidate: any = null;
  @Input() elections: Election[] = [];
  @Input() preselectedElectionId: number | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<{ candidate: CreateCandidateRequest | UpdateCandidateRequest, isEdit: boolean }>();

  candidateForm = {
    name: '',
    electionId: null as number | null,
    partyId: null as number | null,
    wardId: null as number | null,
    biography: '',
    manifestoSummary: ''
  };

  isSubmitting = false;

  ngOnInit() {
    if (this.isEditMode && this.candidate) {
      this.candidateForm = {
        name: this.candidate.name || '',
        electionId: this.candidate.electionId || null,
        partyId: this.candidate.partyId || null,
        wardId: this.candidate.wardId || null,
        biography: this.candidate.candidateDetails?.biography || '',
        manifestoSummary: this.candidate.candidateDetails?.manifestoSummary || ''
      };
    }
  }

  ngOnChanges() {
    if (this.isEditMode && this.candidate) {
      this.candidateForm = {
        name: this.candidate.name || '',
        electionId: this.candidate.electionId || null,
        partyId: this.candidate.partyId || null,
        wardId: this.candidate.wardId || null,
        biography: this.candidate.candidateDetails?.biography || '',
        manifestoSummary: this.candidate.candidateDetails?.manifestoSummary || ''
      };
    } else if (!this.isEditMode) {
      this.resetForm();
      // Pre-select election if provided
      if (this.preselectedElectionId) {
        this.candidateForm.electionId = this.preselectedElectionId;
      }
    }
  }

  closeModal() {
    this.isSubmitting = false;
    this.close.emit();
  }

  onSubmit() {
    if (this.isSubmitting) return;

    // Basic validation
    if (!this.candidateForm.name.trim()) {
      alert('Please enter candidate name');
      return;
    }

    if (!this.candidateForm.electionId) {
      alert('Please select an election');
      return;
    }

    this.isSubmitting = true;

    const candidateData = {
      name: this.candidateForm.name.trim(),
      electionId: this.candidateForm.electionId,
      partyId: this.candidateForm.partyId || undefined,
      wardId: this.candidateForm.wardId || undefined,
      biography: this.candidateForm.biography.trim() || undefined,
      manifestoSummary: this.candidateForm.manifestoSummary.trim() || undefined
    };

    this.save.emit({ candidate: candidateData, isEdit: this.isEditMode });
  }

  resetForm() {
    this.candidateForm = {
      name: '',
      electionId: this.preselectedElectionId || null,
      partyId: null,
      wardId: null,
      biography: '',
      manifestoSummary: ''
    };
  }

  resetLoadingState() {
    this.isSubmitting = false;
  }
}
