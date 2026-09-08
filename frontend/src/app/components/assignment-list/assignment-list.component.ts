import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AssignmentService } from '../../services/assignment.service';
import { Assignment, AssignmentStatus } from '../../models/assignment.model';

@Component({
  selector: 'app-assignment-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './assignment-list.component.html'
})
export class AssignmentListComponent implements OnInit {
  private assignmentService = inject(AssignmentService);

  assignments: Assignment[] = [];
  loading = false;
  error: string | null = null;

  readonly statusLabels: Record<AssignmentStatus, string> = {
    ASSIGNED: 'Assigned',
    RETURNED: 'Returned'
  };

  ngOnInit(): void {
    this.loadAssignments();
  }

  private loadAssignments(): void {
    this.loading = true;
    this.error = null;
    this.assignmentService.getAll().subscribe({
      next: (data) => {
        this.assignments = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load assignments.';
        this.loading = false;
      }
    });
  }

  returnAssignment(assignment: Assignment): void {
    if (!confirm(`Return asset "${assignment.assetName}" from ${assignment.assignedTo}?`)) {
      return;
    }
    this.assignmentService.return(assignment.id).subscribe({
      next: () => this.loadAssignments(),
      error: () => this.error = 'Failed to return asset.'
    });
  }

  statusClass(status: AssignmentStatus): string {
    return status === 'ASSIGNED' ? 'badge-assigned' : 'badge-returned';
  }
}
