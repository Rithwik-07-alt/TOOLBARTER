import React, { useState } from 'react';
import { CalendarPlus, AlertCircle, X } from 'lucide-react';
import { apiClient } from '../api/apiClient';

export default function SlotModal({ itemId, itemTitle, isOpen, onClose, onSuccess }) {
  const [slotStart, setSlotStart] = useState('');
  const [slotEnd, setSlotEnd] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!slotStart || !slotEnd) {
      setError('Please provide start and end dates/times.');
      return;
    }

    try {
      setLoading(true);
      const formattedStart = slotStart.length === 16 ? `${slotStart}:00` : slotStart;
      const formattedEnd = slotEnd.length === 16 ? `${slotEnd}:00` : slotEnd;

      const slot = await apiClient.createSlot(itemId, {
        slotStart: formattedStart,
        slotEnd: formattedEnd,
      });

      setLoading(false);
      onSuccess(slot);
    } catch (err) {
      setLoading(false);
      setError(err.message || 'Failed to create availability slot.');
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content modal-card">
        <div className="modal-header">
          <div className="modal-title-row">
            <CalendarPlus size={18} className="modal-icon" />
            <h3 className="modal-title">Add Availability Slot</h3>
          </div>
          <button onClick={onClose} className="btn-close-modal">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-body">
          <p className="modal-desc">
            Define a bookable window for <strong>{itemTitle}</strong>. Borrowers can reserve any duration inside this slot.
          </p>

          {error && (
            <div className="alert alert-error">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">Slot Start Time</label>
              <input
                type="datetime-local"
                className="form-input"
                value={slotStart}
                onChange={(e) => setSlotStart(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Slot End Time</label>
              <input
                type="datetime-local"
                className="form-input"
                value={slotEnd}
                onChange={(e) => setSlotEnd(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-outline" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating Slot...' : 'Save Availability Slot'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
