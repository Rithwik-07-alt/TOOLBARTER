import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import SlotModal from '../components/SlotModal';
import {
  Wrench, PlusCircle, CalendarPlus, Trash2, Edit,
  Clock, CheckCircle2, AlertCircle, RefreshCw, ChevronDown, ChevronUp,
  Package, MapPin, Layers, BarChart2,
} from 'lucide-react';

const STATUS_CONFIG = {
  LISTED:       { label: 'Listed',       cls: 'pill-listed',       dot: '#059669' },
  UNAVAILABLE:  { label: 'Unavailable',  cls: 'pill-unavailable',  dot: '#94a3b8' },
  PENDING:      { label: 'Pending',      cls: 'pill-pending',      dot: '#b45309' },
};

function StatusPill({ status }) {
  const cfg = STATUS_CONFIG[status] || { label: status, cls: 'pill-cancelled', dot: '#94a3b8' };
  return (
    <span className={`status-pill ${cfg.cls}`}>
      <span className="status-dot" style={{ background: cfg.dot }} />
      {cfg.label}
    </span>
  );
}

export default function ManageItemsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedSlotItem, setSelectedSlotItem] = useState(null);
  const [expandedItemId, setExpandedItemId] = useState(null);
  const [itemSlots, setItemSlots] = useState({});
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const loadMyItems = async () => {
    try {
      setLoading(true);
      const data = await apiClient.getMyItems();
      setItems(data || []);
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to load tools.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadMyItems(); }, []);

  // Auto-dismiss message after 4s
  useEffect(() => {
    if (!message) return;
    const t = setTimeout(() => setMessage(null), 4000);
    return () => clearTimeout(t);
  }, [message]);

  const handleUnlist = async (id, title) => {
    if (!window.confirm(`Unlist "${title}"? This cannot be undone.`)) return;
    try {
      await apiClient.deleteItem(id);
      setMessage({ type: 'success', text: `"${title}" unlisted successfully.` });
      loadMyItems();
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to unlist item.' });
    }
  };

  const toggleExpandSlots = async (itemId) => {
    if (expandedItemId === itemId) { setExpandedItemId(null); return; }
    setExpandedItemId(itemId);
    if (!itemSlots[itemId]) {
      setSlotsLoading(true);
      try {
        const slots = await apiClient.getSlots(itemId);
        setItemSlots((prev) => ({ ...prev, [itemId]: slots || [] }));
      } catch {
        // quiet catch
      } finally {
        setSlotsLoading(false);
      }
    }
  };

  const handleDeleteSlot = async (itemId, slotId) => {
    if (!window.confirm('Delete this slot?')) return;
    try {
      await apiClient.deleteSlot(slotId);
      setMessage({ type: 'success', text: 'Slot removed.' });
      const updated = await apiClient.getSlots(itemId);
      setItemSlots((prev) => ({ ...prev, [itemId]: updated || [] }));
    } catch (err) {
      setMessage({ type: 'error', text: err.message || 'Failed to delete slot.' });
    }
  };

  const stats = {
    total: items.length,
    active: items.filter(i => i.status === 'LISTED').length,
    slots: items.reduce((s, i) => s + (i.openSlotsCount || 0), 0),
  };

  return (
    <div className="mgmt-page">
      {/* Page Header */}
      <div className="mgmt-header">
        <div className="mgmt-header-text">
          <h1 className="mgmt-title">My Garage Inventory</h1>
          <p className="mgmt-subtitle">Manage listings, availability windows and loan status</p>
        </div>
        <div className="mgmt-header-actions">
          <button onClick={loadMyItems} className="btn btn-outline btn-sm" disabled={loading}>
            <RefreshCw size={14} className={loading ? 'spin-anim' : ''} />
            Refresh
          </button>
          <Link to="/items/new" className="btn btn-primary btn-sm">
            <PlusCircle size={15} /> Add Tool
          </Link>
        </div>
      </div>

      {/* Stats Strip */}
      {!loading && items.length > 0 && (
        <div className="mgmt-stats-strip">
          <div className="mgmt-stat">
            <Package size={16} className="mgmt-stat-icon" />
            <span className="mgmt-stat-num">{stats.total}</span>
            <span className="mgmt-stat-lbl">Total Tools</span>
          </div>
          <div className="mgmt-stat">
            <BarChart2 size={16} className="mgmt-stat-icon" />
            <span className="mgmt-stat-num">{stats.active}</span>
            <span className="mgmt-stat-lbl">Active</span>
          </div>
          <div className="mgmt-stat">
            <Clock size={16} className="mgmt-stat-icon" />
            <span className="mgmt-stat-num">{stats.slots}</span>
            <span className="mgmt-stat-lbl">Open Slots</span>
          </div>
        </div>
      )}

      {/* Toast Message */}
      {message && (
        <div className={`mgmt-toast mgmt-toast-${message.type}`}>
          {message.type === 'success' ? <CheckCircle2 size={15} /> : <AlertCircle size={15} />}
          <span>{message.text}</span>
          <button className="mgmt-toast-close" onClick={() => setMessage(null)}>×</button>
        </div>
      )}

      {/* Content */}
      {loading ? (
        <div className="mgmt-loading">
          <div className="spinner" />
          <p>Loading your tools...</p>
        </div>
      ) : items.length === 0 ? (
        <div className="mgmt-empty">
          <Wrench size={48} className="mgmt-empty-icon" />
          <h3>No Tools Listed Yet</h3>
          <p>Share your drills, ladders, pressure washers and garden gear with neighbours.</p>
          <Link to="/items/new" className="btn btn-primary mt-4">
            <PlusCircle size={16} /> List Your First Tool
          </Link>
        </div>
      ) : (
        <div className="mgmt-list">
          {items.map((item) => (
            <div key={item.id} className="mgmt-card">
              {/* Card Main Row */}
              <div className="mgmt-card-main">
                {/* Photo / Placeholder */}
                <div className="mgmt-card-thumb">
                  {item.photoUrl
                    ? <img src={item.photoUrl} alt={item.title} className="mgmt-card-img" onError={(e) => { e.target.style.display = 'none'; }} />
                    : <div className="mgmt-card-img-placeholder"><Wrench size={28} /></div>
                  }
                </div>

                {/* Info */}
                <div className="mgmt-card-info">
                  <div className="mgmt-card-badges">
                    <span className="mgmt-cat-tag">{item.categoryDisplay || item.category}</span>
                    <StatusPill status={item.status} />
                    {item.isOneTimeUse && <span className="mgmt-one-time-tag">One-time</span>}
                  </div>
                  <h3 className="mgmt-card-title">
                    <Link to={`/items/${item.id}`}>{item.title}</Link>
                  </h3>
                  <p className="mgmt-card-desc">{item.description}</p>
                  <div className="mgmt-card-meta">
                    <span><Layers size={12} /> {item.conditionGradeDisplay || item.condition}</span>
                    <span><MapPin size={12} /> {item.pickupRadiusKm || item.pickupRadius || 5} km radius</span>
                    <span><Clock size={12} /> {item.minDurationHours || 1}h – {item.maxDurationHours || 72}h</span>
                    <span><CalendarPlus size={12} /> {item.openSlotsCount || 0} slots</span>
                  </div>
                </div>

                {/* Actions */}
                <div className="mgmt-card-actions">
                  <button
                    onClick={() => setSelectedSlotItem(item)}
                    className="btn btn-outline btn-sm"
                    title="Add availability slot"
                  >
                    <CalendarPlus size={14} /> Add Slot
                  </button>
                  <button
                    onClick={() => toggleExpandSlots(item.id)}
                    className={`btn btn-sm ${expandedItemId === item.id ? 'btn-primary' : 'btn-secondary'}`}
                    title="View slots"
                  >
                    <Clock size={14} />
                    Slots
                    {expandedItemId === item.id ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                  </button>
                  <Link to={`/items/${item.id}/edit`} className="btn btn-outline btn-sm" title="Edit listing">
                    <Edit size={14} /> Edit
                  </Link>
                  <button
                    onClick={() => handleUnlist(item.id, item.title)}
                    className="btn btn-danger-outline btn-sm"
                    title="Unlist tool"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>

              {/* Slots Drawer */}
              {expandedItemId === item.id && (
                <div className="mgmt-slots-drawer">
                  <div className="mgmt-slots-header">
                    <Clock size={14} />
                    <span>Availability Windows</span>
                  </div>
                  {slotsLoading ? (
                    <p className="mgmt-slots-empty">Loading slots...</p>
                  ) : !itemSlots[item.id] || itemSlots[item.id].length === 0 ? (
                    <div className="mgmt-slots-empty">
                      No slots added yet. Click <strong>Add Slot</strong> above to schedule availability.
                    </div>
                  ) : (
                    <div className="mgmt-slots-grid">
                      {itemSlots[item.id].map((slot) => (
                        <div key={slot.id} className={`mgmt-slot-chip ${slot.isBooked ? 'mgmt-slot-booked' : ''}`}>
                          <div className="mgmt-slot-times">
                            <span className="mgmt-slot-date">
                              {new Date(slot.slotStart).toLocaleDateString([], { month: 'short', day: 'numeric' })}
                            </span>
                            <span className="mgmt-slot-time">
                              {new Date(slot.slotStart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} →{' '}
                              {new Date(slot.slotEnd).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          {slot.isBooked
                            ? <span className="mgmt-slot-reserved">Reserved</span>
                            : <button onClick={() => handleDeleteSlot(item.id, slot.id)} className="mgmt-slot-del" title="Remove slot">×</button>
                          }
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Slot Modal */}
      {selectedSlotItem && (
        <SlotModal
          itemId={selectedSlotItem.id}
          itemTitle={selectedSlotItem.title}
          isOpen={!!selectedSlotItem}
          onClose={() => setSelectedSlotItem(null)}
          onSuccess={() => {
            setSelectedSlotItem(null);
            setMessage({ type: 'success', text: 'Availability slot created!' });
            loadMyItems();
          }}
        />
      )}
    </div>
  );
}
