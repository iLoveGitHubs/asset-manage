import { Routes } from '@angular/router';
import { AssetListComponent } from './components/asset-list/asset-list.component';
import { AssetFormComponent } from './components/asset-form/asset-form.component';
import { AssetDetailComponent } from './components/asset-detail/asset-detail.component';
import { CategoryListComponent } from './components/category-list/category-list.component';
import { AssignmentListComponent } from './components/assignment-list/assignment-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/assets', pathMatch: 'full' },
  { path: 'assets', component: AssetListComponent },
  { path: 'assets/new', component: AssetFormComponent },
  { path: 'assets/edit/:id', component: AssetFormComponent },
  { path: 'assets/:id', component: AssetDetailComponent },
  { path: 'categories', component: CategoryListComponent },
  { path: 'assignments', component: AssignmentListComponent },
  { path: '**', redirectTo: '/assets' }
];
