import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private electionUpdatedSubject = new Subject<void>();
  private chartFilterChangedSubject = new Subject<string | null>();
  
  // Observable for components to subscribe to
  electionUpdated$ = this.electionUpdatedSubject.asObservable();
  
  // Observable for chart filter synchronization
  chartFilterChanged$ = this.chartFilterChangedSubject.asObservable();
  
  // Method to notify that elections have been updated
  notifyElectionUpdated(): void {
    this.electionUpdatedSubject.next();
  }
  
  // Method to notify that chart filter has changed
  notifyChartFilterChanged(electionId: string | null): void {
    this.chartFilterChangedSubject.next(electionId);
  }
}

















