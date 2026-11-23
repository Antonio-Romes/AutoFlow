import { Routes } from '@angular/router';
import { LoginPageComponent } from './features/auth/pages/login-page.component';

export const routes: Routes = [
  { path: '', component: LoginPageComponent },
  { path: '**', redirectTo: '' }
];
