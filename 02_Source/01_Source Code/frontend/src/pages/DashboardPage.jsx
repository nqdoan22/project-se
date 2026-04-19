import { useState, useEffect, useCallback } from 'react';
import { reportApi } from '../services/api';

// Display names for material type enum values
const MATERIAL_TYPE_DISPLAY = {
  'API': 'API',
  'EXCIPIENT': 'Excipient',
  'DIETARY_SUPPLEMENT': 'Dietary Supplement',
  'CONTAINER': 'Container',
  'CLOSURE': 'Closure',
  'PROCESS_CHEMICAL': 'Process Chemical',
  'TESTING_MATERIAL': 'Testing Material'
};

const getDisplayName = (type) => MATERIAL_TYPE_DISPLAY[type] || type;

function MetricCard({ label, value, color, sub }) {
  return (
    <div className="metric-card">
      <div className="metric-label">{label}</div>
      <div className="metric-value" style={color ? { color } : {}}>{value ?? '—'}</div>
      {sub && <div className="metric-sub">{sub}</div>}
    </div>
  );
}


export default function DashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [dashLoading, setDashLoading] = useState(true);
  const [dashError, setDashError] = useState('');

  const [nearExpiry, setNearExpiry] = useState([]);
  const [nearLoading, setNearLoading] = useState(true);
  const [nearError, setNearError] = useState('');
  const [expiryDays, setExpiryDays] = useState(30);

  const [inventory, setInventory] = useState([]);
  const [invLoading, setInvLoading] = useState(true);
  const [invError, setInvError] = useState('');

  const [qcFrom, setQcFrom] = useState(() => {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    return d.toISOString().split('T')[0];
  });
  const [qcTo, setQcTo] = useState(() => new Date().toISOString().split('T')[0]);
  const [qcReport, setQcReport] = useState(null);
  const [qcLoading, setQcLoading] = useState(false);
  const [qcError, setQcError] = useState('');

  // Load dashboard
  useEffect(() => {
    setDashLoading(true);
    reportApi.getDashboard()
      .then((r) => setDashboard(r.data))
      .catch((e) => setDashError(e.response?.data?.message ?? 'Không thể tải dashboard'))
      .finally(() => setDashLoading(false));
  }, []);

  // Load near expiry
  const loadNearExpiry = useCallback(async () => {
    setNearLoading(true);
    setNearError('');
    try {
      const res = await reportApi.getNearExpiry({ days: expiryDays });
      setNearExpiry(res.data.content ?? res.data ?? []);
    } catch (e) {
      setNearError(e.response?.data?.message ?? 'Không thể tải danh sách sắp hết hạn');
    } finally {
      setNearLoading(false);
    }
  }, [expiryDays]);

  useEffect(() => { loadNearExpiry(); }, [loadNearExpiry]);

  // Load inventory
  useEffect(() => {
    setInvLoading(true);
    reportApi.getInventory()
      .then((r) => setInventory(r.data))
      .catch((e) => setInvError(e.response?.data?.message ?? 'Không thể tải báo cáo tồn kho'))
      .finally(() => setInvLoading(false));
  }, []);

  // Load QC report
  const loadQCReport = useCallback(async () => {
    if (!qcFrom || !qcTo) return;
    setQcLoading(true);
    setQcError('');
    try {
      const res = await reportApi.getQCReport({ from: qcFrom, to: qcTo });
      setQcReport(res.data);
    } catch (e) {
      setQcError(e.response?.data?.message ?? 'Không thể tải báo cáo QC');
    } finally {
      setQcLoading(false);
    }
  }, [qcFrom, qcTo]);

  useEffect(() => { loadQCReport(); }, [loadQCReport]);

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—';

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>📊 Báo cáo & Dashboard</h1>
          <p>Tổng quan hệ thống quản lý tồn kho</p>
        </div>
      </div>

      <div className="page-body">
        {/* Dashboard Summary */}
        <div className="section-title">Tổng quan hệ thống</div>
        {dashLoading ? (
          <div className="loading-center"><div className="spinner" /></div>
        ) : dashError ? (
          <div className="alert alert-error" style={{ marginBottom: 16 }}>⚠ {dashError}</div>
        ) : dashboard && (
          <>
            <div className="metric-grid">
              <MetricCard label="Tổng vật tư" value={dashboard.totalMaterials} />
              <MetricCard label="Lô đang hoạt động" value={dashboard.totalActiveLots} color="var(--primary)" />
              <MetricCard label="Sắp hết hạn" value={dashboard.nearExpiryLots} color="var(--warning)" sub="trong 30 ngày" />
              <MetricCard label="Lô sản xuất đang chạy" value={dashboard.activeBatches} color="var(--success)" />
              <MetricCard label="QC thất bại (30 ngày)" value={dashboard.failedQCLast30Days} color="var(--danger)" />
            </div>

            {dashboard.byStatus && (
              <>
                <div className="section-title">Lô hàng theo trạng thái</div>
                <div className="metric-grid">
                  <MetricCard label="Quarantine" value={dashboard.byStatus.Quarantine ?? 0} color="var(--warning)" />
                  <MetricCard label="Accepted" value={dashboard.byStatus.Accepted ?? 0} color="var(--success)" />
                  <MetricCard label="Rejected" value={dashboard.byStatus.Rejected ?? 0} color="var(--danger)" />
                  <MetricCard label="Depleted" value={dashboard.byStatus.Depleted ?? 0} color="var(--muted)" />
                </div>
              </>
            )}

            {dashboard.asOf && (
              <p className="text-muted" style={{ marginBottom: 24 }}>
                Cập nhật lúc: {new Date(dashboard.asOf).toLocaleString('vi-VN')}
              </p>
            )}
          </>
        )}

        {/* Near Expiry Lots */}
        <div className="section-title">Lô hàng sắp hết hạn</div>
        <div className="card" style={{ marginBottom: 32 }}>
          <div className="card-header">
            <span className="card-title">Danh sách lô sắp hết hạn</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <label style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>Trong vòng:</label>
              <input
                type="number"
                className="form-control"
                style={{ width: 80 }}
                min={1}
                value={expiryDays}
                onChange={(e) => setExpiryDays(parseInt(e.target.value) || 30)}
              />
              <span style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>ngày</span>
            </div>
          </div>
          {nearLoading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : nearError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{nearError}</div></div>
          ) : nearExpiry.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">✅</div>
              <p>Không có lô nào sắp hết hạn trong {expiryDays} ngày tới.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Vật tư</th>
                    <th>Part Number</th>
                    <th>Số lượng</th>
                    <th>Trạng thái</th>
                    <th>Vị trí kho</th>
                    <th>Ngày hết hạn</th>
                    <th>Còn lại</th>
                  </tr>
                </thead>
                <tbody>
                  {nearExpiry.map((item) => (
                    <tr key={item.lotId}>
                      <td className="td-primary">{item.materialName}</td>
                      <td className="td-mono">{item.partNumber}</td>
                      <td>{item.quantity} <span className="text-muted">{item.unitOfMeasure}</span></td>
                      <td>{item.status}</td>
                      <td>{item.storageLocation || <span className="text-muted">—</span>}</td>
                      <td className="text-muted">{fmtDate(item.expirationDate)}</td>
                      <td>
                        <span style={{
                          fontWeight: 600,
                          color: item.daysUntilExpiry <= 7 ? 'var(--danger)' : 'var(--warning)'
                        }}>
                          {item.daysUntilExpiry} ngày
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Inventory Snapshot */}
        <div className="section-title">Snapshot tồn kho theo vật tư</div>
        <div className="card" style={{ marginBottom: 32 }}>
          {invLoading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : invError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{invError}</div></div>
          ) : inventory.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📦</div>
              <p>Không có dữ liệu tồn kho.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Part Number</th>
                    <th>Tên vật tư</th>
                    <th>Loại</th>
                    <th>Quarantine</th>
                    <th>Accepted</th>
                    <th>Rejected</th>
                    <th>Depleted</th>
                    <th>Tổng có sẵn</th>
                    <th>Đơn vị</th>
                  </tr>
                </thead>
                <tbody>
                  {inventory.map((item) => (
                    <tr key={item.materialId}>
                      <td className="td-mono">{item.partNumber}</td>
                      <td className="td-primary">{item.materialName}</td>
                      <td><span className="type-badge">{getDisplayName(item.materialType)}</span></td>
                      <td>
                        <span style={{ color: 'var(--warning)' }}>
                          {item.lots?.Quarantine?.count ?? 0} lô / {item.lots?.Quarantine?.totalQuantity ?? 0}
                        </span>
                      </td>
                      <td>
                        <span style={{ color: 'var(--success)' }}>
                          {item.lots?.Accepted?.count ?? 0} lô / {item.lots?.Accepted?.totalQuantity ?? 0}
                        </span>
                      </td>
                      <td>
                        <span style={{ color: 'var(--danger)' }}>
                          {item.lots?.Rejected?.count ?? 0} lô / {item.lots?.Rejected?.totalQuantity ?? 0}
                        </span>
                      </td>
                      <td className="text-muted">
                        {item.lots?.Depleted?.count ?? 0} lô / {item.lots?.Depleted?.totalQuantity ?? 0}
                      </td>
                      <td>
                        <strong style={{ color: 'var(--success)' }}>{item.totalAvailable}</strong>
                      </td>
                      <td className="text-muted">{item.unit}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* QC Report */}
        <div className="section-title">Báo cáo kiểm nghiệm (QC)</div>
        <div className="card">
          <div className="card-header">
            <span className="card-title">Kết quả kiểm nghiệm theo khoảng thời gian</span>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
              <label style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>Từ:</label>
              <input
                type="date"
                className="form-control"
                style={{ width: 140 }}
                value={qcFrom}
                onChange={(e) => setQcFrom(e.target.value)}
                id="qc-from"
              />
              <label style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>Đến:</label>
              <input
                type="date"
                className="form-control"
                style={{ width: 140 }}
                value={qcTo}
                onChange={(e) => setQcTo(e.target.value)}
                id="qc-to"
              />
            </div>
          </div>
          <div style={{ padding: 20 }}>
            {qcLoading ? (
              <div className="loading-center"><div className="spinner" /></div>
            ) : qcError ? (
              <div className="alert alert-error">⚠ {qcError}</div>
            ) : qcReport ? (
              <>
                <div className="stats-row" style={{ marginBottom: 20 }}>
                  <div className="stat-card">
                    <span className="stat-label">Tổng kiểm nghiệm</span>
                    <span className="stat-value">{qcReport.summary?.totalTests ?? 0}</span>
                  </div>
                  <div className="stat-card">
                    <span className="stat-label" style={{ color: 'var(--success)' }}>Pass</span>
                    <span className="stat-value" style={{ color: 'var(--success)' }}>{qcReport.summary?.passed ?? 0}</span>
                  </div>
                  <div className="stat-card">
                    <span className="stat-label" style={{ color: 'var(--danger)' }}>Fail</span>
                    <span className="stat-value" style={{ color: 'var(--danger)' }}>{qcReport.summary?.failed ?? 0}</span>
                  </div>
                  <div className="stat-card">
                    <span className="stat-label" style={{ color: 'var(--warning)' }}>Pending</span>
                    <span className="stat-value" style={{ color: 'var(--warning)' }}>{qcReport.summary?.pending ?? 0}</span>
                  </div>
                  <div className="stat-card">
                    <span className="stat-label">Tỷ lệ đạt</span>
                    <span className="stat-value" style={{ color: 'var(--primary)' }}>
                      {qcReport.summary?.passRate != null
                        ? `${(qcReport.summary.passRate * 100).toFixed(1)}%`
                        : '—'}
                    </span>
                  </div>
                </div>

                {qcReport.byMaterial && qcReport.byMaterial.length > 0 && (
                  <>
                    <div className="detail-panel-title">Chi tiết theo vật tư</div>
                    <div className="table-wrapper">
                      <table>
                        <thead>
                          <tr>
                            {Object.keys(qcReport.byMaterial[0]).map((k) => (
                              <th key={k}>{k}</th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {qcReport.byMaterial.map((row, i) => (
                            <tr key={i}>
                              {Object.values(row).map((v, j) => (
                                <td key={j}>{v != null ? String(v) : '—'}</td>
                              ))}
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </>
                )}
              </>
            ) : (
              <div className="empty-state">
                <div className="empty-icon">📊</div>
                <p>Chọn khoảng thời gian để xem báo cáo kiểm nghiệm.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
