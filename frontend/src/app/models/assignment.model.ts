export type AssignmentStatus = 'ASSIGNED' | 'RETURNED';

export interface Assignment {
  id: number;
  assetId: number;
  assetName: string;
  assetCode: string;
  assignedTo: string;
  assignedDate: string;
  returnDate: string | null;
  status: AssignmentStatus;
  notes: string;
}
