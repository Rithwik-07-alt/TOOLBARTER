import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';
import BorrowRequestModal from '../components/BorrowRequestModal';
import SlotModal from '../components/SlotModal';
import {
  Wrench,
  MapPin,
  Shield,
  Calendar,
  Clock,
  User,
  Star,
  Sparkles,
  ArrowLeft,
  PlusCircle,
  Edit,
  CheckCircle2,
} from 'lucide-react';

export default function ItemDetailsPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [slots, setSlots] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [isRequestModalOpen, setIsRequestModalOpen] = useState(false);
  const [isSlotModalOpen, setIsSlotModalOpen] = useState(false);
  const [successToast, setSuccessToast] = useState(null);

  const fetchItemData = async () => {
    try {
      setLoading(true);
      const userCoords = user ? { lat: user.latitude, lng: user.longitude } : {};
      const [itemData, slotsData] = await Promise.all([
        apiClient.getItemById(id, userCoords),
        apiClient.getSlots(id, true),
      ]);
      setItem(itemData);
      setSlots(slotsData);

      if (itemData.ownerId) {
        const ownerReviews = await apiClient.getUserReviews(itemData.ownerId).catch(() => []);
        setReviews(ownerReviews);
      }
    } catch (err) {
      console.error('Failed to load item:', err);
      setError(err.message || 'Tool listing not found.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItemData();
  }, [id, user]);

  const handleRequestSuccess = (newRequest) => {
    setIsRequestModalOpen(false);
    setSuccessToast('Borrow request successfully submitted! Awaiting owner confirmation.');
    fetchItemData();
  };

  const handleSlotSuccess = () => {
    setIsSlotModalOpen(false);
    setSuccessToast('Availability slot added successfully.');
    fetchItemData();
  };

  if (loading) {
    return (
      <div className="container py-8 text-center">
        <div className="spinner mx-auto" />
        <p className="mt-3 text-muted">Retrieving tool and availability schedule...</p>
      </div>
    );
  }

  if (error || !item) {
    return (
      <div className="container py-8">
        <div className="empty-state-box">
          <Wrench size={40} className="text-muted mb-2" />
          <h2>Tool Not Found</h2>
          <p>{error || 'This tool listing may have been unlisted by the owner.'}</p>
          <Link to="/discover" className="btn btn-primary btn-sm mt-3">
            Back to Radar Map
          </Link>
        </div>
      </div>
    );
  }

  const isOwner = user && user.id === item.ownerId;

  return (
    <div className="item-details-container">
      {/* Back button & Action Header */}
      <div className="details-nav-row">
        <Link to="/discover" className="btn-back">
          <ArrowLeft size={16} /> Back to Radar Map
        </Link>
        {isOwner && (
          <div className="owner-quick-actions">
            <button onClick={() => setIsSlotModalOpen(true)} className="btn btn-outline btn-sm">
              <PlusCircle size={14} /> Add Availability Slot
            </button>
            <Link to={`/items/${item.id}/edit`} className="btn btn-secondary btn-sm">
              <Edit size={14} /> Edit Tool Listing
            </Link>
          </div>
        )}
      </div>

      {successToast && (
        <div className="alert alert-success mb-4">
          <CheckCircle2 size={16} />
          <span>{successToast}</span>
        </div>
      )}

      <div className="details-grid">
        {/* Left Column: Tool Specs & Schedule */}
        <div className="details-main-col">
          <div className="details-header-card">
            <div className="details-badge-row">
              <span className="item-category-tag">{item.categoryDisplay}</span>
              <span className="condition-badge">{item.conditionGradeDisplay}</span>
              {item.oneTimeUse && (
                <span className="one-time-badge">
                  <Sparkles size={11} className="inline-icon" /> One-Time Use
                </span>
              )}
            </div>

            <h1 className="item-page-title">{item.title}</h1>

            <div className="details-meta-bar">
              <span className="meta-item">
                <MapPin size={15} className="inline-icon" />
                {item.distanceKm != null ? `${item.distanceKm} km away` : 'Nearby'}
              </span>
              <span className="meta-item">
                <Clock size={15} className="inline-icon" />
                Duration Limits: {item.minDurationHours}h &bull; {item.maxDurationHours}h
              </span>
              <span className="meta-item">
                <Shield size={15} className="inline-icon" />
                Pickup Radius: {item.pickupRadiusKm} km
              </span>
            </div>
          </div>

          {/* Description & Condition Notes */}
          <div className="details-section-card">
            <h3 className="section-heading">Description & Accessories</h3>
            <p className="item-body-desc">{item.description}</p>

            {item.conditionNotes && (
              <div className="condition-notes-box">
                <strong>Owner Condition Notes:</strong>
                <p>{item.conditionNotes}</p>
              </div>
            )}
          </div>

          {/* Availability Slots */}
          <div className="details-section-card">
            <div className="section-header-inline">
              <h3 className="section-heading">Open Availability Windows</h3>
              {isOwner && (
                <button onClick={() => setIsSlotModalOpen(true)} className="btn-link-action">
                  + Add Window
                </button>
              )}
            </div>

            {slots.length === 0 ? (
              <div className="empty-slots-box">
                <Calendar size={24} className="text-muted" />
                <p>No open slots currently available for reservation.</p>
                {isOwner && (
                  <button onClick={() => setIsSlotModalOpen(true)} className="btn btn-outline btn-sm mt-2">
                    Add Availability Slot
                  </button>
                )}
              </div>
            ) : (
              <div className="slots-list-grid">
                {slots.map((s) => (
                  <div key={s.id} className="slot-badge-card">
                    <div className="slot-date-time">
                      <Calendar size={15} className="slot-icon" />
                      <div>
                        <div className="slot-start">
                          {new Date(s.slotStart).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                        </div>
                        <div className="slot-end text-muted">
                          until {new Date(s.slotEnd).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                        </div>
                      </div>
                    </div>
                    {!isOwner && (
                      <button
                        onClick={() => {
                          if (!user) {
                            navigate('/login', { state: { from: { pathname: `/items/${item.id}` } } });
                          } else {
                            setIsRequestModalOpen(true);
                          }
                        }}
                        className="btn btn-primary btn-sm"
                      >
                        Reserve
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Reviews Received by Owner */}
          <div className="details-section-card">
            <h3 className="section-heading">Neighbourhood Feedback & Reviews ({reviews.length})</h3>
            {reviews.length === 0 ? (
              <p className="text-muted">No reviews recorded yet for this owner. Complete an exchange to leave a review!</p>
            ) : (
              <div className="reviews-feed">
                {reviews.map((r) => (
                  <div key={r.id} className="review-feed-item">
                    <div className="review-item-header">
                      <div className="reviewer-info">
                        <div className="avatar-dot-sm">
                          <User size={12} />
                        </div>
                        <span className="reviewer-name">{r.reviewerName}</span>
                      </div>
                      <div className="star-rating-pill">
                        {[...Array(r.rating)].map((_, i) => (
                          <Star key={i} size={13} fill="#eab308" color="#eab308" />
                        ))}
                      </div>
                    </div>
                    {r.comment && <p className="review-comment-text">{r.comment}</p>}
                    <span className="review-timestamp">
                      {new Date(r.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Owner Card & Request Action */}
        <div className="details-side-col">
          <div className="owner-profile-card">
            <div className="owner-card-header">
              <div className="owner-avatar-lg">
                <User size={24} />
              </div>
              <div>
                <h4 className="owner-title-name">{item.ownerName}</h4>
                <div className="owner-badge-tag">{item.ownerTrustLevelDisplay}</div>
              </div>
            </div>

            <div className="owner-trust-metric-box">
              <div className="trust-metric-num">{item.ownerTrustScore}</div>
              <div className="trust-metric-label">Verified Trust Points</div>
              <div className="trust-metric-sub">Earned via undisputed returns & 5-star ratings</div>
            </div>

            <div className="owner-safety-notes">
              <div className="safety-point">
                <CheckCircle2 size={15} className="text-emerald" />
                <span>Double-booking locked via backend concurrency controls</span>
              </div>
              <div className="safety-point">
                <CheckCircle2 size={15} className="text-emerald" />
                <span>Exchange timestamps recorded on MySQL spatial ledger</span>
              </div>
            </div>

            <div className="owner-action-box">
              {isOwner ? (
                <div className="owner-self-notice">
                  You are the owner of this tool listing.
                </div>
              ) : (
                <button
                  onClick={() => {
                    if (!user) {
                      navigate('/login', { state: { from: { pathname: `/items/${item.id}` } } });
                    } else {
                      setIsRequestModalOpen(true);
                    }
                  }}
                  className="btn btn-primary btn-block btn-lg"
                  disabled={slots.length === 0}
                >
                  {slots.length === 0 ? 'No Open Slots' : 'Request to Borrow'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Modals */}
      <BorrowRequestModal
        item={item}
        slots={slots}
        isOpen={isRequestModalOpen}
        onClose={() => setIsRequestModalOpen(false)}
        onSuccess={handleRequestSuccess}
      />

      <SlotModal
        itemId={item.id}
        itemTitle={item.title}
        isOpen={isSlotModalOpen}
        onClose={() => setIsSlotModalOpen(false)}
        onSuccess={handleSlotSuccess}
      />
    </div>
  );
}
