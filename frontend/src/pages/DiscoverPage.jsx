import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import { useAuth } from '../context/AuthContext';
import RadarMap from '../components/RadarMap';
import ItemCard from '../components/ItemCard';
import {
  Search, MapPin, Grid3X3, Map, LayoutTemplate,
  RefreshCw, X, SlidersHorizontal, Zap, ChevronRight
} from 'lucide-react';

const CATEGORIES = [
  { id: '', label: 'All', icon: '🔍' },
  { id: 'POWER_TOOLS', label: 'Power Tools', icon: '🔧' },
  { id: 'LADDERS_ACCESS', label: 'Ladders', icon: '🪜' },
  { id: 'GARDEN_YARD', label: 'Garden', icon: '🌿' },
  { id: 'CLEANING_EQUIPMENT', label: 'Cleaning', icon: '🧹' },
  { id: 'PAINTING_DECORATING', label: 'Painting', icon: '🖌️' },
  { id: 'EVENT_PARTY_GEAR', label: 'Event Gear', icon: '🎉' },
  { id: 'OTHER', label: 'Other', icon: '📦' },
];

const VIEW_MODES = [
  { id: 'split', label: 'Split', Icon: LayoutTemplate, title: 'Map + Grid' },
  { id: 'radar', label: 'Map', Icon: Map, title: 'Full Map' },
  { id: 'grid', label: 'Grid', Icon: Grid3X3, title: 'Grid Only' },
];

function SkeletonCard() {
  return (
    <div className="skeleton-card">
      <div className="skeleton-img" />
      <div className="skeleton-body">
        <div className="skeleton-line w-3-4" />
        <div className="skeleton-line w-1-2" />
        <div className="skeleton-line w-full" />
      </div>
    </div>
  );
}

export default function DiscoverPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [radiusKm, setRadiusKm] = useState(10);
  const [viewMode, setViewMode] = useState('split');
  const [userCenter, setUserCenter] = useState([
    user?.latitude || 17.4401,
    user?.longitude || 78.3826,
  ]);

  const fetchItems = useCallback(async () => {
    try {
      setLoading(true);
      const params = { lat: userCenter[0], lng: userCenter[1], radius: radiusKm };
      if (keyword.trim()) params.keyword = keyword.trim();
      if (selectedCategory) params.category = selectedCategory;
      const data = await apiClient.getItems(params);
      setItems(data || []);
    } catch (err) {
      console.error('Failed to load items:', err);
    } finally {
      setLoading(false);
    }
  }, [userCenter, radiusKm, selectedCategory, keyword]);

  useEffect(() => {
    if (user?.latitude && user?.longitude) {
      setUserCenter([user.latitude, user.longitude]);
    }
  }, [user]);

  useEffect(() => {
    fetchItems();
  }, [userCenter, radiusKm, selectedCategory]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchItems();
  };

  const clearSearch = () => {
    setKeyword('');
    // fetch after state update
    setTimeout(fetchItems, 0);
  };

  return (
    <div className="discover-page">
      {/* Sticky Top Controls */}
      <div className="discover-topbar">
        <div className="discover-topbar-inner">
          {/* Search */}
          <form onSubmit={handleSearchSubmit} className="discover-search-form">
            <div className="discover-search-wrap">
              <Search size={16} className="discover-search-icon" />
              <input
                type="text"
                className="discover-search-input"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Search pressure washer, drill, ladder..."
              />
              {keyword && (
                <button type="button" className="discover-search-clear" onClick={clearSearch}>
                  <X size={14} />
                </button>
              )}
            </div>
            <button type="submit" className="btn btn-primary btn-sm">Search</button>
          </form>

          {/* Right Controls */}
          <div className="discover-controls-right">
            {/* Radius */}
            <div className="radius-pill">
              <MapPin size={13} />
              <span>{radiusKm} km</span>
              <input
                type="range" min="1" max="30" step="1" value={radiusKm}
                onChange={(e) => setRadiusKm(Number(e.target.value))}
                className="radius-inline-slider"
              />
            </div>

            {/* View Toggle */}
            <div className="view-mode-seg">
              {VIEW_MODES.map(({ id, label, Icon, title }) => (
                <button
                  key={id}
                  className={`seg-btn ${viewMode === id ? 'seg-btn-active' : ''}`}
                  onClick={() => setViewMode(id)}
                  title={title}
                >
                  <Icon size={14} />
                  <span className="seg-label">{label}</span>
                </button>
              ))}
            </div>

            <button onClick={fetchItems} className="btn-icon-round" title="Refresh">
              <RefreshCw size={14} className={loading ? 'spin-anim' : ''} />
            </button>
          </div>
        </div>

        {/* Category Pills */}
        <div className="discover-cats">
          {CATEGORIES.map((cat) => (
            <button
              key={cat.id}
              className={`cat-pill ${selectedCategory === cat.id ? 'cat-pill-active' : ''}`}
              onClick={() => setSelectedCategory(cat.id)}
            >
              <span>{cat.icon}</span>
              <span>{cat.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Main Content */}
      <div className={`discover-body layout-${viewMode}`}>
        {/* Map Panel */}
        {(viewMode === 'split' || viewMode === 'radar') && (
          <div className="discover-map-col">
            <RadarMap
              items={items}
              center={userCenter}
              radiusKm={radiusKm}
              onItemClick={(item) => navigate(`/items/${item.id}`)}
            />
          </div>
        )}

        {/* Items Panel */}
        {(viewMode === 'split' || viewMode === 'grid') && (
          <div className="discover-panel">
            <div className="discover-panel-header">
              <div className="discover-results-badge">
                <Zap size={13} />
                <strong>{items.length}</strong> {items.length === 1 ? 'tool' : 'tools'} within {radiusKm} km
              </div>
            </div>

            {loading ? (
              <div className="discover-skeletons">
                {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : items.length === 0 ? (
              <div className="discover-empty">
                <div className="discover-empty-icon">🔍</div>
                <h3>No tools found nearby</h3>
                <p>Try expanding your radius or changing the category filter.</p>
                <button
                  className="btn btn-outline btn-sm mt-3"
                  onClick={() => { setRadiusKm(25); setSelectedCategory(''); }}
                >
                  Expand to 25 km <ChevronRight size={14} />
                </button>
              </div>
            ) : (
              <div className="discover-grid">
                {items.map((item) => (
                  <ItemCard key={item.id} item={item} />
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
