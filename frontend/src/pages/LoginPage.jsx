import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AlertCircle, Wrench } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/discover';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      setLoading(true);
      const user = await login(email, password);
      setLoading(false);
      if (user.roles?.includes('ROLE_OWNER')) {
        navigate('/owner/dashboard');
      } else {
        navigate('/borrower/dashboard');
      }
    } catch (err) {
      setLoading(false);
      setError(err.message || 'Login failed. Please check your credentials.');
    }
  };

  return (
    <div className="auth-page-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="brand-icon-sm mx-auto">
            <Wrench size={18} color="#ffffff" />
          </div>
          <h2 className="auth-title">Welcome Back to TOOLBARTER</h2>
          <p className="auth-subtitle">Sign in to borrow tools or manage your garage listings</p>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
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
            <label className="form-label">Password</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer-link">
          Don't have an account yet? <Link to="/register">Create Account</Link>
        </div>
      </div>
    </div>
  );
}
