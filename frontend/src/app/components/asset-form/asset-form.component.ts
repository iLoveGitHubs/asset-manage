import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/category.model';
import { AssetStatus } from '../../models/asset.model';

@Component({
  selector: 'app-asset-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './asset-form.component.html',
  styleUrl: './asset-form.component.css'
})
export class AssetFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private assetService = inject(AssetService);
  private categoryService = inject(CategoryService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  form!: FormGroup;
  categories: Category[] = [];
  isEdit = false;
  assetId: number | null = null;
  loading = false;
  submitting = false;
  error: string | null = null;

  readonly statusOptions: { value: AssetStatus; label: string }[] = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'INACTIVE', label: 'Inactive' },
    { value: 'MAINTENANCE', label: 'Maintenance' },
    { value: 'ASSIGNED', label: 'Assigned' }
  ];

  ngOnInit(): void {
    this.initForm();
    this.loadCategories();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.assetId = Number(idParam);
      this.loadAsset(this.assetId);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(200)]],
      code: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/^[A-Z0-9-]+$/)]],
      status: ['ACTIVE', [Validators.required]],
      purchaseDate: ['', [Validators.required]],
      purchaseValue: [0, [Validators.required, Validators.min(0)]],
      currentValue: [0, [Validators.required, Validators.min(0)]],
      categoryId: ['', [Validators.required]],
      usefulLifeYears: [5, [Validators.required, Validators.min(1), Validators.max(50)]]
    });
  }

  private loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (cats) => this.categories = cats,
      error: () => this.error = 'Failed to load categories.'
    });
  }

  private loadAsset(id: number): void {
    this.loading = true;
    this.assetService.getById(id).subscribe({
      next: (asset) => {
        this.form.patchValue({
          name: asset.name,
          code: asset.code,
          status: asset.status,
          purchaseDate: asset.purchaseDate,
          purchaseValue: asset.purchaseValue,
          currentValue: asset.currentValue,
          categoryId: asset.categoryId,
          usefulLifeYears: asset.usefulLifeYears
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load asset data.';
        this.loading = false;
      }
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.error = null;
    const value = this.form.value;

    const payload = {
      name: value.name,
      code: value.code,
      status: value.status,
      purchaseDate: value.purchaseDate,
      purchaseValue: Number(value.purchaseValue),
      currentValue: Number(value.currentValue),
      categoryId: Number(value.categoryId),
      usefulLifeYears: Number(value.usefulLifeYears)
    };

    if (this.isEdit && this.assetId !== null) {
      this.assetService.update(this.assetId, payload).subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/assets']);
        },
        error: () => {
          this.error = 'Failed to update asset.';
          this.submitting = false;
        }
      });
    } else {
      this.assetService.create(payload).subscribe({
        next: () => {
          this.submitting = false;
          this.router.navigate(['/assets']);
        },
        error: () => {
          this.error = 'Failed to create asset.';
          this.submitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/assets']);
  }
}
