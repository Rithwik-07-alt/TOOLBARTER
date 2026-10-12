import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LocationPicker from '../components/LocationPicker';
import { apiClient } from '../api/apiClient';
import {
  Wrench, Tag, MapPin, ChevronRight, ChevronLeft,
  AlertCircle, Check, Image as ImageIcon, Sliders, Info
} from 'lucide-react';

const CATEGORIES = [
  { value: 'POWER_TOOLS',          label: 'Power Tools',          icon: '🔧' },
  { value: 'LADDERS_ACCESS',       label: 'Ladders & Access',     icon: '🪜' },
  { value: 'GARDEN_YARD',          label: 'Garden & Yard',        icon: '🌿' },
  { value: 'CLEANING_EQUIPMENT',   label: 'Cleaning Equipment',   icon: '🧹' },
  { value: 'PAINTING_DECORATING',  label: 'Painting & Decorating',icon: '🖌️' },
  { value: 'EVENT_PARTY_GEAR',     label: 'Event & Party Gear',   icon: '🎉' },
  { value: 'OTHER',                label: 'Other',                icon: '📦' },
];

const CONDITIONS = [
  { value: 'NEW',      label: 'New',       desc: 'Never used, in original packaging' },
  { value: 'LIKE_NEW', label: 'Like New',  desc: 'Minimal use, flawless condition' },
  { value: 'GOOD',     label: 'Good',      desc: 'Used with care, fully functional' },
  { value: 'FAIR',     label: 'Fair',      desc: 'Noticeable wear, works reliably' },
  { value: 'POOR',     label: 'Poor',      desc: 'Heavy wear, functions with quirks' },
];

const STEPS = [
  { id: 'basics',   label: '1. Basics',   icon: Wrench },
  { id: 'details',  label: '2. Details',  icon: Tag },
  { id: 'location', label: '3. Location', icon: MapPin },
];

export default function ItemFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const isEdit = Boolean(id);

  const [step, setStep] = useState(0);
  const [form, setForm] = useState({
    title: '',
    description: '',
    category: 'POWER_TOOLS',
    condition: 'GOOD',
    conditionNotes: '',
    pickupRadius: 5,
    isOneTimeUse: false,
    photoUrl: '',
    latitude: user?.latitude || user?.homeLatitude || 17.4401,
    longitude: user?.longitude || user?.homeLongitude || 78.3826,
  });
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(isEdit);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    if (isEdit) {
      setFetchLoading(true);
      apiClient.getItemById(id)
        .then(res => {
          const item = res?.data || res;
          setForm({
            title:          item.title          || '',
            description:    item.description    || '',
            category:       item.category       || 'POWER_TOOLS',
            condition:      item.condition      || 'GOOD',
            conditionNotes: item.conditionNotes || '',
            pickupRadius:   item.pickupRadiusKm || item.pickupRadius || 5,
            isOneTimeUse:   item.isOneTimeUse   || false,
            photoUrl:       item.photoUrl       || '',
            latitude:       item.latitude       || 17.4401,
            longitude:      item.longitude      || 78.3826,
          });
        })
        .catch(() => setError('Failed to load tool details.'))
        .finally(() => setFetchLoading(false));
    }
  }, [id, isEdit, user, navigate]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleLocationChange = (coords) => {
    const lat = coords?.latitude ?? coords?.lat;
    const lng = coords?.longitude ?? coords?.lng;
    setForm(prev => ({ ...prev, latitude: lat, longitude: lng }));
  };

  const validateStep = (s) => {
    if (s === 0) {
      if (!form.title.trim()) { setError('Please enter a tool title.'); return false; }
      if (!form.description.trim()) { setError('Please enter a description.'); return false; }
    }
    setError('');
    return true;
  };

  const handleNext = () => {
    if (validateStep(step)) {
      setStep(prev => Math.min(prev + 1, STEPS.length - 1));
    }
  };

  const handlePrev = () => {
    setError('');
    setStep(prev => Math.max(prev - 1, 0));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateStep(0)) {
      setStep(0);
      return;
    }
    setError('');
    setLoading(true);
    try {
      const payload = {
        ...form,
        pickupRadius: Number(form.pickupRadius),
        latitude:     Number(form.latitude),
        longitude:    Number(form.longitude),
      };
      if (isEdit) {
        await apiClient.updateItem(id, payload);
      } else {
        await apiClient.createItem(payload);
      }
      navigate('/manage-items');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to save tool listing.');
    } finally {
      setLoading(false);
    }
  };

  if (fetchLoading) {
    return (
      <div className="ifp-loading">
        <div className="spinner" />
        <p>Loading tool details...</p>
      </div>
    );
  }

  return (
    <div className="ifp-page">
      <div className="ifp-container">
        {/* Header */}
        <div className="ifp-header">
          <div className="ifp-header-icon">{isEdit ? '✏️' : '🛠️'}</div>
          <div>
            <h1 className="ifp-title">{isEdit ? 'Edit Tool Listing' : 'List a New Tool'}</h1>
            <p className="ifp-subtitle">
              {isEdit ? 'Modify your tool specifications and availability' : 'Make your unused tools available to your local neighborhood'}
            </p>
          </div>
        </div>

        {/* Wizard Step Indicator */}
        <div className="ifp-steps">
          {STEPS.map(({ id: sid, label, icon: Icon }, i) => (
            <div
              key={sid}
              className={`ifp-step ${i === step ? 'ifp-step-active' : i < step ? 'ifp-step-done' : 'ifp-step-todo'}`}
              onClick={() => {
                if (i < step || validateStep(step)) setStep(i);
              }}
            >
              <div className="ifp-step-circle">
                {i < step ? <Check size={14} /> : <Icon size={14} />}
              </div>
              <span className="ifp-step-label">{label}</span>
              {i < STEPS.length - 1 && <div className="ifp-step-line" />}
            </div>
          ))}
        </div>

        {/* Error Alert */}
        {error && (
          <div className="ifp-error">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        {/* Form Body */}
        <form onSubmit={handleSubmit}>
          <div className="ifp-card">

            {/* STEP 1: BASICS */}
            {step === 0 && (
              <div className="ifp-step-content">
                <div className="ifp-section-header">
                  <h2>Tool Basics</h2>
                  <p>Give your listing a clear title, description, and category</p>
                </div>

                <div className="form-group">
                  <label className="form-label">
                    Tool Title <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-input"
                    name="title"
                    value={form.title}
                    onChange={handleChange}
                    placeholder="Enter tool title"
                    required
                    autoFocus
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">
                    Description <span className="text-danger">*</span>
                  </label>
                  <textarea
                    className="form-textarea"
                    name="description"
                    value={form.description}
                    onChange={handleChange}
                    rows={4}
                    placeholder="Enter description, included accessories, and safety instructions..."
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Category</label>
                  <div className="ifp-category-grid">
                    {CATEGORIES.map(cat => (
                      <button
                        key={cat.value}
                        type="button"
                        className={`ifp-cat-btn ${form.category === cat.value ? 'ifp-cat-btn-active' : ''}`}
                        onClick={() => setForm(prev => ({ ...prev, category: cat.value }))}
                      >
                        <span className="ifp-cat-icon">{cat.icon}</span>
                        <span className="ifp-cat-label">{cat.label}</span>
                      </button>
                    ))}
                  </div>
                </div>

                <div className="ifp-checkbox-row">
                  <input
                    type="checkbox"
                    id="oneTimeUse"
                    name="isOneTimeUse"
                    checked={form.isOneTimeUse}
                    onChange={handleChange}
                    className="ifp-checkbox"
                  />
                  <label htmlFor="oneTimeUse" className="ifp-checkbox-label">
                    <span className="ifp-checkbox-icon">🔖</span>
                    One-time loan / consumable item <span className="text-muted">(special loan rules apply)</span>
                  </label>
                </div>
              </div>
            )}

            {/* STEP 2: DETAILS */}
            {step === 1 && (
              <div className="ifp-step-content">
                <div className="ifp-section-header">
                  <h2>Condition & Pickup Area</h2>
                  <p>Accurate condition helps build trust and maintain a great rating</p>
                </div>

                <div className="form-group">
                  <label className="form-label">Tool Condition</label>
                  <div className="ifp-condition-list">
                    {CONDITIONS.map(cond => (
                      <label
                        key={cond.value}
                        className={`ifp-cond-option ${form.condition === cond.value ? 'ifp-cond-active' : ''}`}
                      >
                        <input
                          type="radio"
                          name="condition"
                          value={cond.value}
                          checked={form.condition === cond.value}
                          onChange={handleChange}
                          className="ifp-radio"
                        />
                        <div className="ifp-cond-text">
                          <span className="ifp-cond-label">{cond.label}</span>
                          <span className="ifp-cond-desc">{cond.desc}</span>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">
                    Condition Notes <span className="text-muted">(optional)</span>
                  </label>
                  <input
                    type="text"
                    className="form-input"
                    name="conditionNotes"
                    value={form.conditionNotes}
                    onChange={handleChange}
                    placeholder="Enter condition notes (e.g. minor scratches, included accessories)..."
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">
                    Photo URL <span className="text-muted">(optional)</span>
                  </label>
                  <input
                    type="url"
                    className="form-input"
                    name="photoUrl"
                    value={form.photoUrl}
                    onChange={handleChange}
                    placeholder="Enter image URL"
                  />
                  {form.photoUrl && (
                    <div className="ifp-photo-preview">
                      <img
                        src={form.photoUrl}
                        alt="Tool preview"
                        onError={(e) => { e.target.style.display = 'none'; }}
                      />
                    </div>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label">
                    Borrower Search Radius: <span className="ifp-radius-badge">{form.pickupRadius} km</span>
                  </label>
                  <div className="ifp-slider-wrap">
                    <span className="ifp-slider-min">1 km</span>
                    <input
                      type="range"
                      name="pickupRadius"
                      min="1"
                      max="50"
                      value={form.pickupRadius}
                      onChange={handleChange}
                      className="radius-slider"
                    />
                    <span className="ifp-slider-max">50 km</span>
                  </div>
                  <p className="form-hint">Only borrowers within {form.pickupRadius} km will find this tool on their radar.</p>
                </div>
              </div>
            )}

            {/* STEP 3: LOCATION */}
            {step === 2 && (
              <div className="ifp-step-content">
                <div className="ifp-section-header">
                  <h2>Pickup Location</h2>
                  <p>Click or drag the marker to pinpoint where borrowers can collect this tool</p>
                </div>

                <div className="ifp-map-wrap">
                  <LocationPicker
                    latitude={form.latitude}
                    longitude={form.longitude}
                    onChange={handleLocationChange}
                  />
                </div>

                <div className="ifp-coord-display">
                  <div className="ifp-coord-chip">
                    <MapPin size={13} />
                    <span>Latitude: <strong>{Number(form.latitude).toFixed(4)}</strong></span>
                  </div>
                  <div className="ifp-coord-chip">
                    <MapPin size={13} />
                    <span>Longitude: <strong>{Number(form.longitude).toFixed(4)}</strong></span>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Navigation Controls */}
          <div className="ifp-nav">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={step === 0 ? () => navigate('/manage-items') : handlePrev}
            >
              <ChevronLeft size={16} />
              {step === 0 ? 'Cancel' : 'Previous Step'}
            </button>

            {step < STEPS.length - 1 ? (
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleNext}
              >
                Next Step <ChevronRight size={16} />
              </button>
            ) : (
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? (
                  <span className="spinner" />
                ) : (
                  <>
                    {isEdit ? 'Save Changes' : 'Publish Tool Listing'}
                    <Check size={16} />
                  </>
                )}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
}
