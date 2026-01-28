import { Routes } from '@angular/router';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/signin', pathMatch: 'full' },
  { path: 'signin', loadComponent: () => import('./components/signin/signin.component').then(m => m.SigninComponent) },
  { path: 'signup', loadComponent: () => import('./components/signup/signup.component').then(m => m.SignupComponent) },
  { path: 'voter-registration', loadComponent: () => import('./components/voter-registration/voter-registration.component').then(m => m.VoterRegistrationComponent) },
  { path: 'candidate-nomination', loadComponent: () => import('./components/candidate-nomination/candidate-nomination.component').then(m => m.CandidateNominationComponent) },
  { path: 'dashboard', loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'admin', loadComponent: () => import('./components/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
  { path: 'vote', loadComponent: () => import('./components/voting/voting.component').then(m => m.VotingComponent) },
  { path: 'vote/:electionId', loadComponent: () => import('./components/voting/voting.component').then(m => m.VotingComponent) },
  { path: 'create-election', loadComponent: () => import('./components/create-election/create-election.component').then(m => m.CreateElectionComponent) },
  { path: '**', redirectTo: '/signin' }
];
