import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Wrench, Shield, Compass, User as UserIcon, LogOut, PlusCircle } from 'lucide-react';

export default function Navbar() {
  const { user, activeRole, isOwner, isBorrower, logout, switchRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const hasDualRole = isOwner && isBorrower;

  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/" className="brand">
          <div className="brand-icon">
            <Wrench size={20} color="#ffffff" />
          </div>
          <div>
            <div className="brand-title">TOOLBARTER</div>
            <div className="brand-subtitle">Borrow Nearby. Build Together.</div>
          </div>
        </Link>

        <div className="nav-links">
          <Link to="/discover" className={`nav-link ${location.pathname === '/discover' ? 'active' : ''}`}>
            <Compass size={16} />
            <span>Radar Map</span>
          </Link>

          {user && (
            <>
              {activeRole === 'BORROWER' ? (
                <>
                  <Link to="/borrower/dashboard" className={`nav-link ${location.pathname.startsWith('/borrower') ? 'active' : ''}`}>
                    Borrower Console
                  </Link>
                  <Link to="/requests" className={`nav-link ${location.pathname === '/requests' ? 'active' : ''}`}>
                    My Requests
                  </Link>
                </>
              ) : (
                <>
                  <Link to="/owner/dashboard" className={`nav-link ${location.pathname.startsWith('/owner') ? 'active' : ''}`}>
                    Owner Console
                  </Link>
                  <Link to="/manage-items" className={`nav-link ${location.pathname === '/manage-items' ? 'active' : ''}`}>
                    My Tools
                  </Link>
                </>
              )}
            </>
          )}
        </div>

        <div className="nav-actions">
          {user ? (
            <div className="user-nav-group">
              {/* Dual Role Switcher */}
              {hasDualRole && (
                <div className="role-switch-pill">
                  <button
                    className={`role-tab ${activeRole === 'BORROWER' ? 'active' : ''}`}
                    onClick={() => {
                      switchRole('BORROWER');
                      navigate('/borrower/dashboard');
                    }}
                    title="Switch to Borrower Mode"
                  >
                    Borrower
                  </button>
                  <button
                    className={`role-tab ${activeRole === 'OWNER' ? 'active' : ''}`}
                    onClick={() => {
                      switchRole('OWNER');
                      navigate('/owner/dashboard');
                    }}
                    title="Switch to Owner Mode"
                  >
                    Owner
                  </button>
                </div>
              )}

              {/* Quick Action: List an Item */}
              {isOwner && (
                <Link to="/items/new" className="btn btn-sm btn-primary">
                  <PlusCircle size={15} />
                  <span>List Tool</span>
                </Link>
              )}

              {/* Profile & Trust Score Badge */}
              <Link to="/profile" className="user-profile-badge" title="View Trust Profile">
                <div className="avatar-dot">
                  <UserIcon size={14} />
                </div>
                <div className="user-badge-text">
                  <span className="user-name">{(user.name || user.fullName || 'User').split(' ')[0]}</span>
                  <span className="user-trust-tag">
                    <Shield size={11} className="inline-icon" /> {user.trustScore ?? user.trustPoints ?? 0} pts
                  </span>
                </div>
              </Link>

              {/* Logout Button */}
              <button onClick={handleLogout} className="btn-icon-logout" title="Sign Out">
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <div className="guest-nav-group">
              <Link to="/login" className="btn btn-outline btn-sm">
                Sign In
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Get Started
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
