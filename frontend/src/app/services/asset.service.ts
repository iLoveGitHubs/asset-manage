import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Asset, AssetPage, DepreciationInfo } from '../models/asset.model';

@Injectable({ providedIn: 'root' })
export class AssetService {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/assets';

  getAll(page: number, size: number): Observable<AssetPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AssetPage>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Asset> {
    return this.http.get<Asset>(`${this.baseUrl}/${id}`);
  }

  create(asset: Omit<Asset, 'id'>): Observable<Asset> {
    return this.http.post<Asset>(this.baseUrl, asset);
  }

  update(id: number, asset: Omit<Asset, 'id'>): Observable<Asset> {
    return this.http.put<Asset>(`${this.baseUrl}/${id}`, asset);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  search(query: string, page: number, size: number): Observable<AssetPage> {
    const params = new HttpParams()
      .set('q', query)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AssetPage>(`${this.baseUrl}/search`, { params });
  }

  getDepreciation(id: number): Observable<DepreciationInfo> {
    return this.http.get<DepreciationInfo>(`${this.baseUrl}/${id}/depreciation`);
  }
}
