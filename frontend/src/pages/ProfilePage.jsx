import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';

const TRUST_COLORS = {
  NEW_NEIGHBOUR: '#6b7280',
  REGULAR_BORROWER: '#3b82f6',
  RELIABLE_LENDER: '#8b5cf6',
  TRUSTED_NEIGHBOUR: '#f59e0b',
  STREET_CHAMPION: '#ef4444',
};

const TRUST_LABELS = {
  NEW_NEIGHBOUR: 'New Neighbour',
  REGULAR_BORROWER: 'Regular Borrower',
  RELIABLE_LENDER: 'Reliable Lender',
  TRUSTED_NEIGHBOUR: 'Trusted Neighbour',
  STREET_CHAMPION: 'Street Champion',
};

function StarRow({ rating }) {
  return (
    <span>
      {[1, 2, 3, 4, 5].map(i => (
        <span key={i} className={i <= rating ? 'star-filled' : 'star-empty'}>★</span>
      ))}
    </span>
  );
}

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('profile');
  const [stats, setStats] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [statsLoading, setStatsLoading] = useState(true);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({ fullName: '', phoneNumber: '', homeLatitude: '', homeLongitude: '' });
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState('');
  const [editSuccess, setEditSuccess] = useState('');

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    setStatsLoading(true);
    apiClient.getUserStatistics(user.id)
      .then(res => setStats(res.data))
      .catch(() => {})
      .finally(() => setStatsLoading(false));
    setEditForm({
      fullName: user.fullName || '',
      phoneNumber: user.phoneNumber || '',
      homeLatitude: user.homeLatitude || '',
      homeLongitude: user.homeLongitude || '',
    });
  }, [user, navigate]);

  const loadReviews = () => {
    if (reviewsLoading || reviews.length > 0) return;
    setReviewsLoading(true);
    apiClient.getUserReviews(user.id)
      .then(res => setReviews(res.data))
      .catch(() => {})
      .finally(() => setReviewsLoading(false));
  };

  const handleTabChange = t => {
    setTab(t);
    if (t === 'reviews') loadReviews();
  };

  const handleEditChange = e => {
    setEditForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleEditSubmit = async e => {
    e.preventDefault();
    setEditLoading(true);
    setEditError('');
    setEditSuccess('');
    try {
      await apiClient.updateUserProfile(user.id, editForm);
      await refreshUser();
      setEditSuccess('Profile updated successfully!');
      setEditing(false);
    } catch (err) {
      setEditError(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setEditLoading(false);
    }
  };

  if (!user) return null;

  const displayName = user.name || user.fullName || 'User';
  const initials = displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  const userLevel = user.trustLevel || 'NEW_NEIGHBOUR';
  const trustColor = TRUST_COLORS[userLevel] || '#6b7280';
  const trustLabel = TRUST_LABELS[userLevel] || userLevel;
  const userPoints = user.trustScore ?? user.trustPoints ?? 0;

  return (
    <div className="profile-page">
      <div className="profile-hero">
        <div className="profile-avatar" style={{ background: trustColor }}>{initials}</div>
        <div className="profile-info">
          <h1 className="profile-name">{displayName}</h1>
          <p className="profile-email">{user.email}</p>
          <div className="trust-badge" style={{ borderColor: trustColor }}>
            <span className="trust-badge-level" style={{ color: trustColor }}>⭐ {trustLabel}</span>
            <span className="trust-badge-points">{userPoints} pts</span>
          </div>
        </div>
      </div>

      <div className="tab-bar">
        <button className={`tab-btn${tab === 'profile' ? ' tab-btn-active' : ''}`} onClick={() => handleTabChange('profile')}>👤 Profile & Stats</button>
        <button className={`tab-btn${tab === 'reviews' ? ' tab-btn-active' : ''}`} onClick={() => handleTabChange('reviews')}>⭐ My Reviews</button>
      </div>

      {tab === 'profile' && (
        <div className="profile-tab-content">
          {statsLoading ? <div className="spinner" /> : stats && (
            <div className="stats-grid">
              <div className="stat-card"><div className="stat-value">{stats.totalItemsListed}</div><div className="stat-label">Tools Listed</div></div>
              <div className="stat-card"><div className="stat-value">{stats.activeItems}</div><div className="stat-label">Active Tools</div></div>
              <div className="stat-card"><div className="stat-value">{stats.completedBorrowsAsOwner}</div><div className="stat-label">Lends Completed</div></div>
              <div className="stat-card"><div className="stat-value">{stats.completedBorrowsAsBorrower}</div><div className="stat-label">Borrows Completed</div></div>
              <div className="stat-card"><div className="stat-value">{stats.averageRatingAsOwner?.toFixed(1) || '—'}</div><div className="stat-label">Avg Rating (Lender)</div></div>
              <div className="stat-card"><div className="stat-value">{stats.averageRatingAsBorrower?.toFixed(1) || '—'}</div><div className="stat-label">Avg Rating (Borrower)</div></div>
            </div>
          )}

          {editSuccess && <div className="alert alert-success">{editSuccess}</div>}

          {!editing ? (
            <div className="profile-actions">
              <button className="btn btn-secondary" onClick={() => setEditing(true)}>✏️ Edit Profile</button>
            </div>
          ) : (
            <div className="edit-profile-form">
              <h3>Edit Profile</h3>
              {editError && <div className="alert alert-error">{editError}</div>}
              <form onSubmit={handleEditSubmit}>
                <div className="form-group">
                  <label className="form-label">Full Name</label>
                  <input className="form-input" name="fullName" value={editForm.fullName} onChange={handleEditChange} placeholder="Enter your full name" required />
                </div>
                <div className="form-group">
                  <label className="form-label">Phone Number</label>
                  <input className="form-input" name="phoneNumber" value={editForm.phoneNumber} onChange={handleEditChange} placeholder="Enter your phone number" />
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Home Latitude</label>
                    <input className="form-input" name="homeLatitude" type="number" step="0.0001" value={editForm.homeLatitude} onChange={handleEditChange} placeholder="Enter home latitude" />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Home Longitude</label>
                    <input className="form-input" name="homeLongitude" type="number" step="0.0001" value={editForm.homeLongitude} onChange={handleEditChange} placeholder="Enter home longitude" />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="button" className="btn btn-secondary" onClick={() => setEditing(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={editLoading}>
                    {editLoading ? <span className="spinner" /> : 'Save Changes'}
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {tab === 'reviews' && (
        <div className="reviews-tab-content">
          {reviewsLoading ? <div className="spinner" /> : reviews.length === 0 ? (
            <div className="empty-state">No reviews yet. Complete some borrows to earn reviews!</div>
          ) : (
            <div className="reviews-list">
              {reviews.map(r => (
                <div key={r.id} className="review-card">
                  <div className="review-header">
                    <div className="review-stars"><StarRow rating={r.rating} /></div>
                    <span className="review-meta">{r.reviewerName} · {new Date(r.createdAt).toLocaleDateString()}</span>
                  </div>
                  {r.comment && <p className="review-comment">{r.comment}</p>}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
