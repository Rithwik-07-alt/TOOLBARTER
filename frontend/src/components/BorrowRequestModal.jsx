import React, { useState } from 'react';
import { Calendar, Clock, AlertCircle, CheckCircle2, X } from 'lucide-react';
import { apiClient } from '../api/apiClient';

export default function BorrowRequestModal({ item, slots = [], isOpen, onClose, onSuccess }) {
  const [selectedSlotId, setSelectedSlotId] = useState('');
  const [startTs, setStartTs] = useState('');
  const [endTs, setEndTs] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSlotChange = (e) => {
    const slotId = e.target.value;
    setSelectedSlotId(slotId);
    const chosen = slots.find((s) => s.id === parseInt(slotId));
    if (chosen) {
      // Pre-fill datetime-local format: YYYY-MM-DDTHH:mm
      setStartTs(chosen.slotStart.substring(0, 16));
      setEndTs(chosen.slotEnd.substring(0, 16));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!selectedSlotId) {
      setError('Please select an availability slot.');
      return;
    }
    if (!startTs || !endTs) {
      setError('Please specify start and end dates/times.');
      return;
    }

    try {
      setLoading(true);
      // Spring Boot expects ISO format: YYYY-MM-DDTHH:mm:ss
      const formattedStart = startTs.length === 16 ? `${startTs}:00` : startTs;
      const formattedEnd = endTs.length === 16 ? `${endTs}:00` : endTs;

      const response = await apiClient.createBorrowRequest(item.id, {
        slotId: parseInt(selectedSlotId),
        startTs: formattedStart,
        endTs: formattedEnd,
      });

      setLoading(false);
      onSuccess(response);
    } catch (err) {
      setLoading(false);
      setError(err.message || 'Failed to submit borrow request.');
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content modal-card">
        <div className="modal-header">
          <div className="modal-title-row">
            <Calendar size={18} className="modal-icon" />
            <h3 className="modal-title">Request Borrow: {item.title}</h3>
          </div>
          <button onClick={onClose} className="btn-close-modal">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-body">
          {error && (
            <div className="alert alert-error">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Available Time Slots</label>
            {slots.length === 0 ? (
              <p className="empty-subtext">No open availability slots currently defined for this tool.</p>
            ) : (
              <select
                className="form-select"
                value={selectedSlotId}
                onChange={handleSlotChange}
                required
              >
                <option value="">-- Choose a bookable slot --</option>
                {slots.map((s) => (
                  <option key={s.id} value={s.id}>
                    {new Date(s.slotStart).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })} &rarr;{' '}
                    {new Date(s.slotEnd).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                  </option>
                ))}
              </select>
            )}
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">Requested Start</label>
              <input
                type="datetime-local"
                className="form-input"
                value={startTs}
                onChange={(e) => setStartTs(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Requested End</label>
              <input
                type="datetime-local"
                className="form-input"
                value={endTs}
                onChange={(e) => setEndTs(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="borrow-limits-notice">
            <Clock size={13} className="inline-icon" />
            <span>
              Item Duration Limits: Min {item.minDurationHours} hr(s) &bull; Max {item.maxDurationHours} hr(s).
            </span>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-outline" disabled={loading}>
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || slots.length === 0}
            >
              {loading ? 'Validating Schedule...' : 'Submit Request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
