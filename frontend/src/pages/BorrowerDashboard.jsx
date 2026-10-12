import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';
import ReviewModal from '../components/ReviewModal';
import ItemCard from '../components/ItemCard';
import {
  PackageCheck,
  Clock,
  Compass,
  Shield,
  Heart,
  XCircle,
  Star,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';

export default function BorrowerDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [requests, setRequests] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedReviewRequest, setSelectedReviewRequest] = useState(null);
  const [message, setMessage] = useState(null);

  const loadDashboardData = async () => {
    if (!user) return;
    try {
      setLoading(true);
      const [statsData, reqsData, favsData] = await Promise.all([
        apiClient.getUserStatistics(user.id).catch(() => null),
        apiClient.getBorrowerRequests().catch(() => []),
        apiClient.getFavorites().catch(() => []),
      ]);
      setStats(statsData);
      setRequests(reqsData);
      setFavorites(favsData);
    } catch (err) {
      console.error('Failed to load borrower dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [user]);

  const handleCancelRequest = async (requestId) => {
    if (!window.confirm('Are you sure you want to cancel this borrow request?')) return;
    try {
      await apiClient.cancelRequest(requestId);
      setMessage({ type: 'success', text: 'Borrow request cancelled successfully.' });
      loadDashboardData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to cancel request.' });
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return <span className="status-pill pill-pending">Awaiting Owner Approval</span>;
      case 'APPROVED':
        return <span className="status-pill pill-approved">Approved &bull; Ready for Pickup</span>;
      case 'PICKED_UP':
        return <span className="status-pill pill-active">Tool Handed Over (In Use)</span>;
      case 'RETURNED':
        return <span className="status-pill pill-returned">Completed & Returned</span>;
      case 'DECLINED':
        return <span className="status-pill pill-declined">Declined by Owner</span>;
      case 'CANCELLED':
        return <span className="status-pill pill-cancelled">Cancelled</span>;
      default:
        return <span className="status-pill">{status}</span>;
    }
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <div className="dashboard-header-row">
        <div>
          <h1 className="dashboard-title">Borrower Console</h1>
          <p className="dashboard-subtitle">Track your active tool requests, handovers, and trust reputation</p>
        </div>
        <div className="dashboard-actions">
          <Link to="/discover" className="btn btn-primary btn-sm">
            <Compass size={15} /> Find Tools on Radar
          </Link>
          <button onClick={loadDashboardData} className="btn btn-outline btn-sm" title="Refresh data">
            <RefreshCw size={14} className={loading ? 'spinning' : ''} />
          </button>
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type} mb-4`}>
          {message.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
          <span>{message.text}</span>
        </div>
      )}

      {/* Metrics Row */}
      <div className="metrics-row">
        <div className="metric-card">
          <div className="metric-icon-wrap icon-blue">
            <PackageCheck size={20} />
          </div>
          <div>
            <div className="metric-val">{stats?.itemsBorrowed || 0}</div>
            <div className="metric-lbl">Items Borrowed</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-wrap icon-amber">
            <Clock size={20} />
          </div>
          <div>
            <div className="metric-val">{stats?.activeRequests || 0}</div>
            <div className="metric-lbl">Active Requests</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-wrap icon-purple">
            <Shield size={20} />
          </div>
          <div>
            <div className="metric-val">{user?.trustScore || 0} pts</div>
            <div className="metric-lbl">{user?.trustLevelDisplay || 'New Neighbour'}</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-wrap icon-rose">
            <Heart size={20} />
          </div>
          <div>
            <div className="metric-val">{favorites.length}</div>
            <div className="metric-lbl">Saved Tools</div>
          </div>
        </div>
      </div>

      {/* Requests Section */}
      <div className="dashboard-section-card">
        <div className="section-header-inline">
          <h3 className="section-heading">My Borrow Requests & History ({requests.length})</h3>
        </div>

        {loading ? (
          <div className="text-center py-6 text-muted">
            <div className="spinner mx-auto" />
            <p className="mt-2">Loading requests ledger...</p>
          </div>
        ) : requests.length === 0 ? (
          <div className="empty-state-box">
            <Clock size={32} className="text-muted mb-2" />
            <h4>No Borrow Requests Yet</h4>
            <p>Ready to build? Discover garage tools nearby and request an open time slot.</p>
            <Link to="/discover" className="btn btn-primary btn-sm mt-3">
              Explore Proximity Radar
            </Link>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Tool</th>
                  <th>Owner</th>
                  <th>Reserved Time Window</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((req) => (
                  <tr key={req.id}>
                    <td>
                      <div className="table-item-cell">
                        <Link to={`/items/${req.itemId}`} className="table-item-title">
                          {req.itemTitle}
                        </Link>
                        <span className="table-item-cat">{req.itemCategoryDisplay}</span>
                      </div>
                    </td>
                    <td>
                      <div>
                        <strong>{req.ownerName}</strong>
                        {req.ownerPhone && (
                          <div className="table-subtext">📞 {req.ownerPhone}</div>
                        )}
                      </div>
                    </td>
                    <td>
                      <div className="table-time-range">
                        <span>{new Date(req.startTs).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</span>
                        <span className="time-arrow">&rarr;</span>
                        <span>{new Date(req.endTs).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</span>
                      </div>
                    </td>
                    <td>{getStatusBadge(req.status)}</td>
                    <td>
                      <div className="table-actions-cell">
                        {/* Cancel Pending */}
                        {req.status === 'PENDING' && (
                          <button
                            onClick={() => handleCancelRequest(req.id)}
                            className="btn btn-outline btn-xs"
                            title="Cancel Request"
                          >
                            <XCircle size={13} /> Cancel
                          </button>
                        )}

                        {/* Leave Review when returned */}
                        {req.canReview && (
                          <button
                            onClick={() => setSelectedReviewRequest(req)}
                            className="btn btn-primary btn-xs"
                          >
                            <Star size={13} /> Review Owner
                          </button>
                        )}

                        {req.hasReviewed && (
                          <span className="badge-reviewed">
                            <CheckCircle2 size={12} /> Reviewed
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Saved / Favorites Section */}
      {favorites.length > 0 && (
        <div className="dashboard-section-card mt-6">
          <h3 className="section-heading mb-4">Saved / Favorite Tools ({favorites.length})</h3>
          <div className="items-grid">
            {favorites.map((item) => (
              <ItemCard key={item.id} item={item} onFavoriteToggle={loadDashboardData} />
            ))}
          </div>
        </div>
      )}

      {/* Review Modal */}
      {selectedReviewRequest && (
        <ReviewModal
          request={selectedReviewRequest}
          isOpen={!!selectedReviewRequest}
          onClose={() => setSelectedReviewRequest(null)}
          onSuccess={() => {
            setSelectedReviewRequest(null);
            setMessage({ type: 'success', text: 'Review recorded! Community trust updated.' });
            loadDashboardData();
          }}
        />
      )}
    </div>
  );
}
