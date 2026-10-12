import React from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Shield, Clock, Heart, Sparkles } from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';

export default function ItemCard({ item, onFavoriteToggle }) {
  const { user } = useAuth();
  const [isFav, setIsFav] = React.useState(item.isFavorite || false);

  const handleFavClick = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!user) {
      alert('Please sign in to save favorite tools.');
      return;
    }
    try {
      const res = await apiClient.toggleFavorite(item.id);
      setIsFav(res.favorite);
      if (onFavoriteToggle) onFavoriteToggle(item.id, res.favorite);
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
    }
  };

  return (
    <div className="item-card">
      <div className="item-card-header">
        <div className="item-badge-row">
          <span className="item-category-tag">{item.categoryDisplay || item.category}</span>
          {item.oneTimeUse && (
            <span className="one-time-badge">
              <Sparkles size={11} className="inline-icon" /> One-Time Use
            </span>
          )}
        </div>
        <button
          onClick={handleFavClick}
          className={`btn-fav-icon ${isFav ? 'favorited' : ''}`}
          title={isFav ? 'Remove from saved' : 'Save to favorites'}
        >
          <Heart size={16} fill={isFav ? '#ef4444' : 'none'} color={isFav ? '#ef4444' : '#94a3b8'} />
        </button>
      </div>

      <div className="item-card-body">
        <h3 className="item-card-title">{item.title}</h3>
        <p className="item-card-desc">{item.description}</p>

        <div className="item-slot-info">
          <Clock size={13} className="inline-icon" />
          <span>Next Slot: <strong>{item.nextAvailableSlot || 'Contact owner'}</strong></span>
        </div>

        <div className="item-meta-row">
          <span className="meta-distance">
            <MapPin size={13} className="inline-icon" />
            {item.distanceKm != null ? `${item.distanceKm} km away` : 'Nearby'}
          </span>
          <span className="meta-trust">
            <Shield size={13} className="inline-icon" />
            ★ {item.ownerTrustScore ?? 0} ({item.ownerTrustLevelDisplay || 'New Neighbour'})
          </span>
        </div>
      </div>

      <div className="item-card-footer">
        <Link to={`/items/${item.id}`} className="btn btn-primary btn-block btn-sm">
          View & Borrow
        </Link>
      </div>
    </div>
  );
}
