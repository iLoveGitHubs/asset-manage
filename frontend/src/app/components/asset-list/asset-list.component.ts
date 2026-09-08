import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AssetService } from '../../services/asset.service';
import { Asset, AssetPage, AssetStatus } from '../../models/asset.model';

@Component({
  selector: 'app-asset-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './asset-list.component.html',
  styleUrl: './asset-list.component.css'
})
export class AssetListComponent implements OnInit {
  private assetService = inject(AssetService);

  assets: Asset[] = [];
  totalElements = 0;
  totalPages = 0;
  page = 0;
  size = 10;
  loading = false;
  error: string | null = null;

  searchTerm = '';
  searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  readonly statusLabels: Record<AssetStatus, string> = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    MAINTENANCE: 'Maintenance',
    ASSIGNED: 'Assigned'
  };

  ngOnInit(): void {
    this.loadAssets();
  }

  loadAssets(): void {
    this.loading = true;
    this.error = null;

    const request$ = this.searchTerm.trim().length > 0
      ? this.assetService.search(this.searchTerm.trim(), this.page, this.size)
      : this.assetService.getAll(this.page, this.size);

    request$.subscribe({
      next: (data: AssetPage) => {
        this.assets = data.content;
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load assets. Please try again.';
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }
    this.searchDebounceTimer = setTimeout(() => {
      this.page = 0;
      this.loadAssets();
    }, 350);
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadAssets();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadAssets();
    }
  }

  goToPage(index: number): void {
    if (index >= 0 && index < this.totalPages && index !== this.page) {
      this.page = index;
      this.loadAssets();
    }
  }

  deleteAsset(asset: Asset): void {
    if (!confirm(`Are you sure you want to delete asset "${asset.name}" (${asset.code})?`)) {
      return;
    }
    this.assetService.delete(asset.id).subscribe({
      next: () => {
        this.loadAssets();
      },
      error: () => {
        this.error = `Failed to delete asset "${asset.name}".`;
      }
    });
  }

  getPagesArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
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
