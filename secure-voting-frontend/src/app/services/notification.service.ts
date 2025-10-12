import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private electionUpdatedSubject = new Subject<void>();
  
  // Observable for components to subscribe to
  electionUpdated$ = this.electionUpdatedSubject.asObservable();
  
  // Method to notify that elections have been updated
  notifyElectionUpdated(): void {
    this.electionUpdatedSubject.next();
  }
}













