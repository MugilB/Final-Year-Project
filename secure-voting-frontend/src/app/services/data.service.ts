import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

const API_URL = environment.apiUrl;

export interface Candidate {
  candidateId: number;
  name: string;
  electionId: number;
  partyId?: number;
  wardId?: number;
  status?: string; // Candidate status: PENDING, APPROVED, REJECTED
  candidateDetails?: CandidateDetails;
}

export interface CandidateDetails {
  candidateId: number;
  email?: string;
  phoneNumber?: string;
  gender?: string;
  age?: number;
  address?: string;
  aadharCardLink?: string;
  candidateImageLink?: string; // Profile image link
  biography?: string;
  manifestoSummary?: string;
  reviewNotes?: string;
  reviewedBy?: string;
  reviewedAt?: number;
}

export interface CreateCandidateRequest {
  name: string;
  electionId: number;
  partyId?: number;
  wardId?: number;
  biography?: string;
  manifestoSummary?: string;
}

export interface UpdateCandidateRequest {
  name: string;
  partyId?: number;
  wardId?: number;
  biography?: string;
  manifestoSummary?: string;
}

export interface Election {
  electionId: number;
  name: string;
  startDate: number;
  endDate: number;
  status: string;
  description?: string;
  rules?: string;
  candidates?: Candidate[];
}

export interface CreateElectionRequest {
  name: string;
  description?: string;
  rules?: string;
  startDate: number;
  endDate: number;
  status: string;
}

export interface UpdateElectionRequest {
  name: string;
  description?: string;
  rules?: string;
  startDate: number;
  endDate: number;
  status: string;
}

export interface ElectionDetails {
  electionId: number;
  description?: string;
  rules?: string;
}

export interface VoteRequest {
  electionId: number;
  candidateId: number;
  encryptedVote: string;
}

export interface Block {
  blockHeight: number;
  hash: string;
  previousHash: string;
  electionId?: number;
  electionName?: string;
  voterId: string;
  data: string;
  timestamp: number;
  nonce: number;
}

export interface User {
  voterId: string;
  username?: string; // Keep for backward compatibility
  email: string;
  role: string;
  active: boolean;
  createdAt?: number;
  lastLogin?: number;
  approvalStatus?: number; // 0=rejected, 1=approved, 2=pending
  
  // Voter details (now included in the API response)
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  dateOfBirth?: number;
  gender?: string;
  address?: string;
  wardId?: number;
  bloodGroup?: string;
  aadharCardLink?: string;
  profilePictureLink?: string;
}

export interface CreateUserRequest {
  voterId: string;
  email: string;
  password: string;
  roles: string[];
  // Voter details
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  dateOfBirth?: number;
  gender?: string;
  address?: string;
  wardId?: number;
  bloodGroup?: string;
  aadharCardLink?: string;
  profilePictureLink?: string;
}

export interface UpdateUserRequest {
  voterId?: string;
  email?: string;
  password?: string;
  roles?: string[];
  status?: string;
  // Voter details
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  dateOfBirth?: number;
  gender?: string;
  address?: string;
  wardId?: number;
  bloodGroup?: string;
  aadharCardLink?: string;
  profilePictureLink?: string;
  approvalStatus?: number;
}

export interface UpdateCandidateStatusRequest {
  status: string;
  reviewNotes?: string;
  reviewedBy?: string;
}

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

export interface UpdateVoterStatusRequest {
  status: string;
  reviewNotes?: string;
  reviewedBy?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DataService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  // --- Helper method to get auth headers ---
  private getAuthHeaders(): HttpHeaders {
    return this.authService.getAuthHeaders();
  }

  // --- Admin Methods ---
  getAdminUsers(): Observable<any> {
    return this.http.get(API_URL + '/admin/users', { headers: this.getAuthHeaders() });
  }

  getBlockchain(): Observable<any> {
    return this.http.get(API_URL + '/admin/blockchain', { headers: this.getAuthHeaders() });
  }

  tallyVotes(): Observable<any> {
    return this.http.get(API_URL + '/admin/tally', { headers: this.getAuthHeaders() });
  }

  // --- Election & Voting Methods ---
  getAllElections(): Observable<Election[]> {
    return this.http.get<Election[]>(API_URL + '/elections')
      .pipe(catchError(this.handleError));
  }

  getOpenElections(): Observable<Election[]> {
    return this.http.get<Election[]>(API_URL + '/elections/open')
      .pipe(catchError(this.handleError));
  }

  getEligibleElections(): Observable<Election[]> {
    return this.http.get<Election[]>(API_URL + '/elections/eligible')
      .pipe(catchError(this.handleError));
  }


  getAllElectionsWithCandidates(): Observable<Election[]> {
    return this.http.get<Election[]>(API_URL + '/elections/with-candidates')
      .pipe(catchError(this.handleError));
  }

  getElectionWithCandidates(electionId: number): Observable<Election> {
    return this.http.get<Election>(API_URL + `/elections/${electionId}/with-candidates`)
      .pipe(catchError(this.handleError));
  }

  castVote(voteData: VoteRequest): Observable<any> {
    return this.http.post(API_URL + '/vote', voteData, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  // --- Helper method to check if election is eligible ---
  isElectionEligible(election: Election): boolean {
    const now = Date.now();
    return (election.status === 'ACTIVE' || election.status === 'OPENED') && 
           election.startDate <= now && 
           election.endDate > now;
  }

  // --- Helper method to format election dates ---
  formatElectionDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // --- Helper method to get time remaining ---
  getTimeRemaining(endDate: number): string {
    const now = Date.now();
    const remaining = endDate - now;
    
    if (remaining <= 0) {
      return 'Election ended';
    }
    
    const days = Math.floor(remaining / (1000 * 60 * 60 * 24));
    const hours = Math.floor((remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60));
    
    if (days > 0) {
      return `${days}d ${hours}h ${minutes}m remaining`;
    } else if (hours > 0) {
      return `${hours}h ${minutes}m remaining`;
    } else {
      return `${minutes}m remaining`;
    }
  }

  // --- Blockchain API Methods ---
  
  getAllBlocks(): Observable<Block[]> {
    return this.http.get<Block[]>(`${API_URL}/blocks`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getLatestBlock(): Observable<Block> {
    return this.http.get<Block>(`${API_URL}/blocks/latest`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getTotalBlockCount(): Observable<number> {
    return this.http.get<number>(`${API_URL}/blocks/count`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlocksByElection(electionId: number): Observable<Block[]> {
    return this.http.get<Block[]>(`${API_URL}/blocks/election/${electionId}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlocksByVoter(voterId: string): Observable<Block[]> {
    return this.http.get<Block[]>(`${API_URL}/blocks/voter/${voterId}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlockByHeight(blockHeight: number): Observable<Block> {
    return this.http.get<Block>(`${API_URL}/blocks/height/${blockHeight}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlockByHash(hash: string): Observable<Block> {
    return this.http.get<Block>(`${API_URL}/blocks/hash/${hash}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlocksInRange(startHeight: number, endHeight: number): Observable<Block[]> {
    return this.http.get<Block[]>(`${API_URL}/blocks/range?startHeight=${startHeight}&endHeight=${endHeight}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlockchainStatistics(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/blocks/statistics`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getBlockCountByElection(electionId: number): Observable<number> {
    return this.http.get<number>(`${API_URL}/blocks/election/${electionId}/count`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  // --- User Management API Methods ---
  
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/users`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  createUser(user: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${API_URL}/users`, user, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  updateUser(userId: string, user: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${API_URL}/users/${userId}`, user, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  deleteUser(userId: string): Observable<string> {
    return this.http.delete<string>(`${API_URL}/users/${userId}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  // --- Election Management API Methods ---
  
  createElection(election: CreateElectionRequest): Observable<Election> {
    return this.http.post<Election>(`${API_URL}/elections`, election, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  updateElection(electionId: number, election: UpdateElectionRequest): Observable<Election> {
    return this.http.put<Election>(`${API_URL}/elections/${electionId}`, election, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  deleteElection(electionId: number): Observable<any> {
    return this.http.delete(`${API_URL}/elections/${electionId}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  getElectionDetails(electionId: number): Observable<ElectionDetails> {
    return this.http.get<ElectionDetails>(`${API_URL}/elections/${electionId}/details`).pipe(
      catchError(this.handleError)
    );
  }

  // --- Candidate Management API Methods ---
  
  getAllCandidates(): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(`${API_URL}/candidates`).pipe(
      catchError(this.handleError)
    );
  }

  getCandidatesByElection(electionId: number): Observable<Candidate[]> {
    console.log(`DataService: Fetching candidates for election ${electionId}`);
    return this.http.get<Candidate[]>(`${API_URL}/candidates/election/${electionId}`).pipe(
      catchError((error) => {
        console.error(`DataService: Error fetching candidates for election ${electionId}:`, error);
        return this.handleError(error);
      })
    );
  }

  // Get only approved candidates for a specific election (for user-facing components)
  getApprovedCandidatesByElection(electionId: number): Observable<Candidate[]> {
    console.log(`DataService: Fetching approved candidates for election ${electionId}`);
    return this.http.get<Candidate[]>(`${API_URL}/candidates/election/${electionId}/approved`).pipe(
      catchError((error) => {
        console.error(`DataService: Error fetching approved candidates for election ${electionId}:`, error);
        return this.handleError(error);
      })
    );
  }

  getCandidateById(candidateId: number): Observable<Candidate> {
    return this.http.get<Candidate>(`${API_URL}/candidates/${candidateId}`).pipe(
      catchError(this.handleError)
    );
  }

  getCandidateVoteCounts(electionId: number): Observable<{ [candidateName: string]: number }> {
    console.log(`DataService: Fetching vote counts for election ${electionId}`);
    return this.http.get<{ [candidateName: string]: number }>(`${API_URL}/candidates/election/${electionId}/vote-counts`).pipe(
      catchError((error) => {
        console.error(`DataService: Error fetching vote counts for election ${electionId}:`, error);
        return this.handleError(error);
      })
    );
  }

  createCandidate(candidate: CreateCandidateRequest): Observable<Candidate> {
    return this.http.post<Candidate>(`${API_URL}/candidates`, candidate, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  updateCandidate(candidateId: number, candidate: UpdateCandidateRequest): Observable<Candidate> {
    return this.http.put<Candidate>(`${API_URL}/candidates/${candidateId}`, candidate, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  updateCandidateStatus(candidateId: number, request: UpdateCandidateStatusRequest): Observable<Candidate> {
    return this.http.put<Candidate>(`${API_URL}/candidates/${candidateId}/status`, request, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  deleteCandidate(candidateId: number): Observable<any> {
    return this.http.delete(`${API_URL}/candidates/${candidateId}`, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any): Observable<never> {
    console.error('DataService Error:', error);
    // Preserve the original error object to maintain status codes and other properties
    return throwError(() => error);
  }

  // --- Election Status Management ---
  
  updateElectionStatuses(): Observable<string> {
    return this.http.post(`${API_URL}/elections/update-statuses`, {}, {
      headers: this.getAuthHeaders(),
      responseType: 'text' // Explicitly tell HttpClient to expect a plain text response
    }).pipe(
      catchError(this.handleError)
    );
  }

  testBackendConnection(): Observable<string> {
    return this.http.get(`${API_URL}/elections/test`, {
      responseType: 'text' // Explicitly tell HttpClient to expect a plain text response
    }).pipe(
      catchError(this.handleError)
    );
  }

  decryptVoteFromBlock(blockHeight: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/blocks/${blockHeight}/decrypt-vote`).pipe(
      catchError(this.handleError)
    );
  }

  submitVote(voteData: { electionId: number; candidateId: number }): Observable<any> {
    return this.http.post<any>(`${API_URL}/votes/submit`, voteData, { headers: this.getAuthHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  // Get user's voter ID from username (using the same pattern as backend)
  getUserVoterId(username: string): string {
    return `VOTER-${username.toUpperCase()}`;
  }

  // Check if user has voted in a specific election
  hasUserVotedInElection(voterId: string, electionId: number): Observable<{ hasVoted: boolean; blockHeight?: number; timestamp?: number; electionName?: string }> {
    return this.http.get<{ hasVoted: boolean; blockHeight?: number; timestamp?: number; electionName?: string }>(`${API_URL}/votes/status/${voterId}/${electionId}`).pipe(
      catchError(this.handleError)
    );
  }

  // --- Voter Management ---
  
  getAllVoters(): Observable<Voter[]> {
    return this.http.get<Voter[]>(`${API_URL}/voters/all`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  getVoterByVoterId(voterId: string): Observable<Voter> {
    return this.http.get<Voter>(`${API_URL}/voters/${voterId}`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  updateVoterStatus(voterId: string, request: UpdateVoterStatusRequest): Observable<Voter> {
    return this.http.put<Voter>(`${API_URL}/voters/${voterId}/status`, request, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  getPendingVoters(): Observable<Voter[]> {
    return this.http.get<Voter[]>(`${API_URL}/voters/pending`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }

  getApprovedVoters(): Observable<Voter[]> {
    return this.http.get<Voter[]>(`${API_URL}/voters/approved`, {
      headers: this.getAuthHeaders()
    }).pipe(
      catchError(this.handleError)
    );
  }
}