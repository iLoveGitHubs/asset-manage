import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { CategoryService } from '../../services/category.service';
import { Asset, AssetStatus, DepreciationInfo } from '../../models/asset.model';
import { Category } from '../../models/category.model';

@Component({
  selector: 'app-asset-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './asset-detail.component.html',
  styleUrl: './asset-detail.component.css'
})
export class AssetDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private assetService = inject(AssetService);
  private categoryService = inject(CategoryService);

  asset: Asset | null = null;
  category: Category | null = null;
  depreciation: DepreciationInfo | null = null;
  loading = false;
  error: string | null = null;

  readonly statusLabels: Record<AssetStatus, string> = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    MAINTENANCE: 'Maintenance',
    ASSIGNED: 'Assigned'
  };

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadAsset(id);
      this.loadDepreciation(id);
    }
  }

  private loadAsset(id: number): void {
    this.loading = true;
    this.assetService.getById(id).subscribe({
      next: (asset) => {
        this.asset = asset;
        this.loading = false;
        this.loadCategory(asset.categoryId);
      },
      error: () => {
        this.error = 'Failed to load asset.';
        this.loading = false;
      }
    });
  }

  private loadCategory(categoryId: number): void {
    this.categoryService.getAll().subscribe({
      next: (cats) => {
        this.category = cats.find((c) => c.id === categoryId) ?? null;
      },
      error: () => {
        this.category = null;
      }
    });
  }

  private loadDepreciation(id: number): void {
    this.assetService.getDepreciation(id).subscribe({
      next: (dep) => this.depreciation = dep,
      error: () => {
        this.depreciation = null;
      }
    });
  }

  statusClass(status: AssetStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'badge-active';
      case 'ASSIGNED':
        return 'badge-assigned';
      case 'MAINTENANCE':
        return 'badge-maintenance';
      default:
        return 'badge-inactive';
    }
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(value);
  }
}
