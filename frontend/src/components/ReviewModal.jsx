import React, { useState } from 'react';
import { Star, AlertCircle, X, ShieldCheck } from 'lucide-react';
import { apiClient } from '../api/apiClient';

export default function ReviewModal({ request, isOpen, onClose, onSuccess }) {
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen || !request) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      setLoading(true);
      const res = await apiClient.submitReview(request.id, {
        rating,
        comment: comment.trim(),
      });
      setLoading(false);
      onSuccess(res);
    } catch (err) {
      setLoading(false);
      setError(err.message || 'Failed to submit review.');
    }
  };

  const getRatingLabel = (r) => {
    switch (r) {
      case 5: return '5 Stars - Outstanding (+2 Trust Points)';
      case 4: return '4 Stars - Good (+1 Trust Point)';
      case 3: return '3 Stars - Average (0 Trust Points)';
      case 2: return '2 Stars - Below Average (-2 Trust Points)';
      case 1: return '1 Star - Poor Experience (-4 Trust Points)';
      default: return '';
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content modal-card">
        <div className="modal-header">
          <div className="modal-title-row">
            <ShieldCheck size={18} className="modal-icon text-emerald" />
            <h3 className="modal-title">Leave Exchange Review</h3>
          </div>
          <button onClick={onClose} className="btn-close-modal">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-body">
          <p className="modal-desc">
            Rate your completed tool exchange for <strong>{request.itemTitle}</strong>.
            Reviews update community trust scores and badges on the blockchain ledger.
          </p>

          {error && (
            <div className="alert alert-error">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          <div className="rating-select-container">
            <div className="star-row">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  type="button"
                  className="star-btn"
                  onMouseEnter={() => setHoverRating(star)}
                  onMouseLeave={() => setHoverRating(0)}
                  onClick={() => setRating(star)}
                >
                  <Star
                    size={28}
                    fill={(hoverRating || rating) >= star ? '#eab308' : 'none'}
                    color={(hoverRating || rating) >= star ? '#eab308' : '#64748b'}
                  />
                </button>
              ))}
            </div>
            <div className="rating-label-display">
              {getRatingLabel(hoverRating || rating)}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Review Comment (Optional)</label>
            <textarea
              className="form-textarea"
              rows={3}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="How was the tool condition, punctuality, and handover communication?"
            />
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-outline" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Submitting Review...' : 'Submit & Update Trust Score'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
