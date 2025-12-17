import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
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
export class CandidateModalComponent implements OnInit, OnChanges {
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
    manifestoSummary: '',
    // Candidate Details fields
    email: '',
    phoneNumber: '',
    gender: '',
    age: null as number | null,
    address: '',
    aadharCardLink: '',
    candidateImageLink: ''
  };

  isSubmitting = false;
  private isInitialized = false;
  private isLoadingData = false;

  ngOnInit() {
    this.loadCandidateData();
    this.isInitialized = true;
  }

  ngOnChanges(changes: SimpleChanges) {
    // Only process changes after component is initialized
    if (!this.isInitialized) {
      return;
    }

    const isVisibleChanged = changes['isVisible'];
    const candidateChanged = changes['candidate'];
    const isEditModeChanged = changes['isEditMode'];
    
    // Only reload when modal becomes visible (opened) - not when it closes
    if (isVisibleChanged && this.isVisible && isVisibleChanged.previousValue !== this.isVisible) {
      // Reset submitting state when opening modal
      this.isSubmitting = false;
      // Use a longer delay to ensure all inputs are set and any alerts are dismissed
      setTimeout(() => {
        this.loadCandidateData();
      }, 150);
    } 
    // Only reload if candidate or edit mode changes while modal is visible AND it's a meaningful change
    else if (this.isVisible && !this.isSubmitting) {
      // Check if candidate actually changed (not just reference equality)
      if (candidateChanged && candidateChanged.previousValue !== candidateChanged.currentValue) {
        const prevCandidate = candidateChanged.previousValue;
        const currCandidate = candidateChanged.currentValue;
        // Only reload if candidate ID changed or candidate was null before
        if (!prevCandidate || !currCandidate || 
            prevCandidate.candidateId !== currCandidate.candidateId) {
          setTimeout(() => {
            this.loadCandidateData();
          }, 150);
        }
      }
      // Check if edit mode actually changed
      else if (isEditModeChanged && 
               isEditModeChanged.previousValue !== isEditModeChanged.currentValue) {
        setTimeout(() => {
          this.loadCandidateData();
        }, 150);
      }
    }
  }

  private loadCandidateData() {
    // Prevent multiple simultaneous loads
    if (this.isLoadingData) {
      return;
    }
    
    this.isLoadingData = true;
    
    if (this.isEditMode && this.candidate) {
      // Ensure candidateDetails exists (might be null initially)
      const candidateDetails = this.candidate.candidateDetails || {};
      
      // Helper function to clean placeholder values
      const cleanValue = (value: any, placeholders: string[] = []): string => {
        if (!value) return '';
        const strValue = String(value).trim();
        if (strValue === '' || strValue === 'null' || strValue === 'undefined') return '';
        // Check against common placeholders
        const allPlaceholders = [
          'not provided',
          'not-provided',
          'default@example.com',
          '0000000000',
          'Not Specified',
          'Address not provided',
          'Biography not provided',
          'Manifesto not provided',
          'https://drive.google.com/not-provided',
          ...placeholders
        ];
        const lowerValue = strValue.toLowerCase();
        for (const placeholder of allPlaceholders) {
          if (lowerValue === placeholder.toLowerCase() || lowerValue.includes(placeholder.toLowerCase())) {
            return '';
          }
        }
        return strValue;
      };
      
      // Get biography and manifestoSummary, handling null/undefined/empty strings
      const biography = candidateDetails.biography;
      const manifestoSummary = candidateDetails.manifestoSummary;
      
      // Filter out placeholder-like strings
      const cleanBiography = cleanValue(biography, ['Biography not provided']);
      const cleanManifesto = cleanValue(manifestoSummary, ['Manifesto not provided']);
      
      // Clean age - return null if it's a default/placeholder value
      let cleanAge: number | null = null;
      if (candidateDetails.age && candidateDetails.age > 0 && candidateDetails.age <= 100) {
        cleanAge = candidateDetails.age;
      }
      
      this.candidateForm = {
        name: this.candidate.name || '',
        electionId: this.candidate.electionId || null,
        partyId: this.candidate.partyId || null,
        wardId: this.candidate.wardId || null,
        biography: cleanBiography,
        manifestoSummary: cleanManifesto,
        // Load candidate details fields - clean placeholder values
        email: cleanValue(candidateDetails.email, ['default@example.com']),
        phoneNumber: cleanValue(candidateDetails.phoneNumber, ['0000000000']),
        gender: cleanValue(candidateDetails.gender, ['Not Specified']),
        age: cleanAge,
        address: cleanValue(candidateDetails.address, ['Address not provided']),
        aadharCardLink: cleanValue(candidateDetails.aadharCardLink, ['https://drive.google.com/not-provided']),
        candidateImageLink: cleanValue(candidateDetails.candidateImageLink, ['https://drive.google.com/not-provided'])
      };
    } else if (!this.isEditMode) {
      this.resetForm();
      // Pre-select election if provided
      if (this.preselectedElectionId) {
        this.candidateForm.electionId = this.preselectedElectionId;
      }
    }
    
    // Reset loading flag after a short delay to allow change detection to complete
    setTimeout(() => {
      this.isLoadingData = false;
    }, 200);
  }

  closeModal() {
    if (this.isSubmitting) {
      return; // Prevent closing while submitting
    }
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

    // Validate candidate details fields if in edit mode (they should exist)
    if (this.isEditMode) {
      if (!this.candidateForm.email?.trim()) {
        alert('Please enter email address');
        return;
      }
      if (!this.candidateForm.phoneNumber?.trim()) {
        alert('Please enter phone number');
        return;
      }
      if (!this.candidateForm.gender) {
        alert('Please select gender');
        return;
      }
      if (!this.candidateForm.age || this.candidateForm.age < 18 || this.candidateForm.age > 100) {
        alert('Please enter a valid age (18-100)');
        return;
      }
      if (!this.candidateForm.address?.trim()) {
        alert('Please enter address');
        return;
      }
      if (!this.candidateForm.aadharCardLink?.trim()) {
        alert('Please enter Aadhar Card link');
        return;
      }
    }

    this.isSubmitting = true;

    // Helper to convert values - for edit mode, send all fields (including empty strings for optional fields)
    // This ensures the backend knows which fields to update
    const toValue = (value: string | number | null | undefined, isRequired: boolean = false): string | number | null | undefined => {
      if (value === null || value === undefined) {
        // For required fields in edit mode, we should have validated them already
        // For optional fields, send null to indicate they should be cleared
        return isRequired ? undefined : null;
      }
      if (typeof value === 'string') {
        const trimmed = value.trim();
        // For required fields, don't send empty strings (should have been validated)
        // For optional fields, send empty string so backend knows to clear them
        if (trimmed === '') {
          return isRequired ? undefined : '';
        }
        return trimmed;
      }
      return value;
    };

    const candidateData: any = {
      name: this.candidateForm.name.trim(),
      electionId: this.candidateForm.electionId
    };

    // Include optional fields
    if (this.candidateForm.partyId !== null && this.candidateForm.partyId !== undefined) {
      candidateData.partyId = this.candidateForm.partyId;
    }
    if (this.candidateForm.wardId !== null && this.candidateForm.wardId !== undefined) {
      candidateData.wardId = this.candidateForm.wardId;
    }

    // For edit mode, always send all candidateDetails fields so backend knows to update them
    if (this.isEditMode) {
      // Required fields - always send (validation ensures they have values)
      // Send the actual values - validation ensures these are not empty
      candidateData.email = this.candidateForm.email?.trim() || '';
      candidateData.phoneNumber = this.candidateForm.phoneNumber?.trim() || '';
      candidateData.gender = this.candidateForm.gender || '';
      candidateData.age = this.candidateForm.age || 0;
      candidateData.address = this.candidateForm.address?.trim() || '';
      candidateData.aadharCardLink = this.candidateForm.aadharCardLink?.trim() || '';
      
      // Optional fields - send even if empty to allow clearing
      const bioValue = this.candidateForm.biography?.trim();
      candidateData.biography = bioValue === '' ? null : bioValue;
      const manifestoValue = this.candidateForm.manifestoSummary?.trim();
      candidateData.manifestoSummary = manifestoValue === '' ? null : manifestoValue;
      const imageValue = this.candidateForm.candidateImageLink?.trim();
      candidateData.candidateImageLink = imageValue === '' ? null : imageValue;
    } else {
      // For create mode, only send if provided
      if (this.candidateForm.biography?.trim()) {
        candidateData.biography = this.candidateForm.biography.trim();
      }
      if (this.candidateForm.manifestoSummary?.trim()) {
        candidateData.manifestoSummary = this.candidateForm.manifestoSummary.trim();
      }
      if (this.candidateForm.candidateImageLink?.trim()) {
        candidateData.candidateImageLink = this.candidateForm.candidateImageLink.trim();
      }
    }

    this.save.emit({ candidate: candidateData, isEdit: this.isEditMode });
  }

  resetForm() {
    this.candidateForm = {
      name: '',
      electionId: this.preselectedElectionId || null,
      partyId: null,
      wardId: null,
      biography: '',
      manifestoSummary: '',
      email: '',
      phoneNumber: '',
      gender: '',
      age: null,
      address: '',
      aadharCardLink: '',
      candidateImageLink: ''
    };
  }

  resetLoadingState() {
    this.isSubmitting = false;
  }
}
