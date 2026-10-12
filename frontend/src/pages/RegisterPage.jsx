import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LocationPicker from '../components/LocationPicker';
import { UserPlus, AlertCircle, Wrench, Shield } from 'lucide-react';

export default function RegisterPage() {
  const [searchParams] = useSearchParams();
  const initialRole = searchParams.get('role') || 'BOTH';

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [roleType, setRoleType] = useState(initialRole);
  const [coords, setCoords] = useState({ latitude: 17.4401, longitude: 78.3826 });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    let roles = [];
    if (roleType === 'OWNER') {
      roles = ['ROLE_OWNER'];
    } else if (roleType === 'BORROWER') {
      roles = ['ROLE_BORROWER'];
    } else {
      roles = ['ROLE_OWNER', 'ROLE_BORROWER'];
    }

    try {
      setLoading(true);
      await register({
        name,
        email,
        password,
        phone,
        latitude: coords.latitude,
        longitude: coords.longitude,
        roles,
      });
      setLoading(false);
      if (roles.includes('ROLE_OWNER')) {
        navigate('/owner/dashboard');
      } else {
        navigate('/borrower/dashboard');
      }
    } catch (err) {
      setLoading(false);
      setError(err.message || 'Registration failed. Please check your details.');
    }
  };

  return (
    <div className="auth-page-container">
      <div className="auth-card auth-card-wide">
        <div className="auth-header">
          <div className="brand-icon-sm mx-auto">
            <Wrench size={18} color="#ffffff" />
          </div>
          <h2 className="auth-title">Join TOOLBARTER Community</h2>
          <p className="auth-subtitle">Borrow and lend garage tools with your verified neighbours</p>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                className="form-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your full name"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input
                type="tel"
                className="form-input"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="Enter your phone number"
                required
              />
            </div>
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email address"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password (min 6 characters)</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                minLength={6}
                required
              />
            </div>
          </div>

          {/* Role Selection */}
          <div className="form-group">
            <label className="form-label">Account Capabilities</label>
            <div className="role-options-grid">
              <label className={`role-option-card ${roleType === 'BOTH' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="roleType"
                  value="BOTH"
                  checked={roleType === 'BOTH'}
                  onChange={(e) => setRoleType(e.target.value)}
                />
                <div className="role-card-title">Dual Account (Recommended)</div>
                <div className="role-card-desc">Lend your own tools and borrow from neighbours seamlessly.</div>
              </label>

              <label className={`role-option-card ${roleType === 'BORROWER' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="roleType"
                  value="BORROWER"
                  checked={roleType === 'BORROWER'}
                  onChange={(e) => setRoleType(e.target.value)}
                />
                <div className="role-card-title">Borrower Only</div>
                <div className="role-card-desc">Search radar, reserve time-windows, and borrow tools.</div>
              </label>

              <label className={`role-option-card ${roleType === 'OWNER' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="roleType"
                  value="OWNER"
                  checked={roleType === 'OWNER'}
                  onChange={(e) => setRoleType(e.target.value)}
                />
                <div className="role-card-title">Owner / Lister</div>
                <div className="role-card-desc">List garage inventory, set slots, approve requests.</div>
              </label>
            </div>
          </div>

          {/* Home Geolocation Picker */}
          <div className="form-group">
            <LocationPicker
              label="Home Location (Geocoded Point SRID 4326)"
              latitude={coords.latitude}
              longitude={coords.longitude}
              onChange={setCoords}
            />
          </div>

          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Creating Account & Generating JWT...' : 'Complete Registration'}
          </button>
        </form>

        <div className="auth-footer-link">
          Already have an account? <Link to="/login">Sign In here</Link>
        </div>
      </div>
    </div>
  );
}
