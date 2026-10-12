import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';
import ReviewModal from '../components/ReviewModal';
import {
  Wrench,
  Package,
  Clock,
  Shield,
  CheckCircle2,
  XCircle,
  Truck,
  RotateCcw,
  Star,
  PlusCircle,
  RefreshCw,
  AlertCircle,
} from 'lucide-react';

export default function OwnerDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [selectedReviewRequest, setSelectedReviewRequest] = useState(null);
  const [message, setMessage] = useState(null);

  const loadOwnerData = async () => {
    if (!user) return;
    try {
      setLoading(true);
      const [statsData, reqsData] = await Promise.all([
        apiClient.getUserStatistics(user.id).catch(() => null),
        apiClient.getOwnerRequests().catch(() => []),
      ]);
      setStats(statsData);
      setRequests(reqsData);
    } catch (err) {
      console.error('Failed to load owner dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOwnerData();
  }, [user]);

  const handleApprove = async (id) => {
    try {
      setActionLoading(id);
      await apiClient.approveRequest(id);
      setMessage({ type: 'success', text: 'Borrow request approved! The time slot is now locked.' });
      loadOwnerData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to approve request.' });
    } finally {
      setActionLoading(null);
    }
  };

  const handleDecline = async (id) => {
    if (!window.confirm('Are you sure you want to decline this request?')) return;
    try {
      setActionLoading(id);
      await apiClient.declineRequest(id);
      setMessage({ type: 'success', text: 'Borrow request declined and slot released.' });
      loadOwnerData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to decline request.' });
    } finally {
      setActionLoading(null);
    }
  };

  const handleConfirmPickup = async (id) => {
    try {
      setActionLoading(id);
      await apiClient.confirmPickup(id);
      setMessage({ type: 'success', text: 'Handover confirmed! Tool is now ON_LOAN.' });
      loadOwnerData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to confirm pickup.' });
    } finally {
      setActionLoading(null);
    }
  };

  const handleConfirmReturn = async (id) => {
    try {
      setActionLoading(id);
      await apiClient.confirmReturn(id);
      setMessage({ type: 'success', text: 'Return confirmed! Tool is relisted, and +5 trust points awarded!' });
      loadOwnerData();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to confirm return.' });
    } finally {
      setActionLoading(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return <span className="status-pill pill-pending">Action Required</span>;
      case 'APPROVED':
        return <span className="status-pill pill-approved">Approved &bull; Awaiting Handover</span>;
      case 'PICKED_UP':
        return <span className="status-pill pill-active">Tool on Loan</span>;
      case 'RETURNED':
        return <span className="status-pill pill-returned">Safely Returned</span>;
      case 'DECLINED':
        return <span className="status-pill pill-declined">Declined</span>;
      case 'CANCELLED':
        return <span className="status-pill pill-cancelled">Cancelled by Borrower</span>;
      default:
        return <span className="status-pill">{status}</span>;
    }
  };

  return (
    <div className="dashboard-container">
      {/* Header */}
      <div className="dashboard-header-row">
        <div>
          <h1 className="dashboard-title">Owner Console</h1>
          <p className="dashboard-subtitle">Manage incoming loan requests, confirm handovers, and build lender trust</p>
        </div>
        <div className="dashboard-actions">
          <Link to="/items/new" className="btn btn-primary btn-sm">
            <PlusCircle size={15} /> List New Tool
          </Link>
          <Link to="/manage-items" className="btn btn-secondary btn-sm">
            <Wrench size={15} /> My Tools
          </Link>
          <button onClick={loadOwnerData} className="btn btn-outline btn-sm" title="Refresh data">
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
            <Wrench size={20} />
          </div>
          <div>
            <div className="metric-val">{stats?.itemsListed || 0}</div>
            <div className="metric-lbl">Tools Listed</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-wrap icon-amber">
            <Clock size={20} />
          </div>
          <div>
            <div className="metric-val">
              {requests.filter((r) => r.status === 'PENDING').length}
            </div>
            <div className="metric-lbl">Pending Requests</div>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-icon-wrap icon-rose">
            <Package size={20} />
          </div>
          <div>
            <div className="metric-val">{stats?.activeLoans || 0}</div>
            <div className="metric-lbl">Active Loans</div>
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
      </div>

      {/* Incoming Requests Section */}
      <div className="dashboard-section-card">
        <div className="section-header-inline">
          <h3 className="section-heading">Incoming Borrow Requests & Active Handover Contracts</h3>
        </div>

        {loading ? (
          <div className="text-center py-6 text-muted">
            <div className="spinner mx-auto" />
            <p className="mt-2">Retrieving owner ledger...</p>
          </div>
        ) : requests.length === 0 ? (
          <div className="empty-state-box">
            <Clock size={32} className="text-muted mb-2" />
            <h4>No Incoming Borrow Requests</h4>
            <p>Your tools are listed on the radar map. Incoming requests from neighbours will appear here.</p>
            <Link to="/manage-items" className="btn btn-outline btn-sm mt-3">
              View Your Tools & Slots
            </Link>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Tool Requested</th>
                  <th>Borrower</th>
                  <th>Requested Window</th>
                  <th>Status</th>
                  <th>Action Controls</th>
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
                        <strong>{req.borrowerName}</strong>
                        <div className="table-subtext">★ {req.borrowerTrustScore} pts ({req.borrowerTrustLevelDisplay})</div>
                        {req.borrowerPhone && <div className="table-subtext">📞 {req.borrowerPhone}</div>}
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
                        {/* Pending: Approve / Decline */}
                        {req.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => handleApprove(req.id)}
                              disabled={actionLoading === req.id}
                              className="btn btn-primary btn-xs"
                            >
                              <CheckCircle2 size={13} /> Approve
                            </button>
                            <button
                              onClick={() => handleDecline(req.id)}
                              disabled={actionLoading === req.id}
                              className="btn btn-outline btn-xs"
                            >
                              <XCircle size={13} /> Decline
                            </button>
                          </>
                        )}

                        {/* Approved: Confirm Pickup Handover */}
                        {req.status === 'APPROVED' && (
                          <button
                            onClick={() => handleConfirmPickup(req.id)}
                            disabled={actionLoading === req.id}
                            className="btn btn-secondary btn-xs"
                          >
                            <Truck size={13} /> Handover Tool
                          </button>
                        )}

                        {/* Picked up: Confirm Return */}
                        {req.status === 'PICKED_UP' && (
                          <button
                            onClick={() => handleConfirmReturn(req.id)}
                            disabled={actionLoading === req.id}
                            className="btn btn-primary btn-xs btn-emerald"
                          >
                            <RotateCcw size={13} /> Confirm Return (+5 pts)
                          </button>
                        )}

                        {/* Returned: Review borrower */}
                        {req.canReview && (
                          <button
                            onClick={() => setSelectedReviewRequest(req)}
                            className="btn btn-outline btn-xs"
                          >
                            <Star size={13} /> Review Borrower
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

      {/* Review Modal */}
      {selectedReviewRequest && (
        <ReviewModal
          request={selectedReviewRequest}
          isOpen={!!selectedReviewRequest}
          onClose={() => setSelectedReviewRequest(null)}
          onSuccess={() => {
            setSelectedReviewRequest(null);
            setMessage({ type: 'success', text: 'Borrower review saved! Trust rating ledger updated.' });
            loadOwnerData();
          }}
        />
      )}
    </div>
  );
}
