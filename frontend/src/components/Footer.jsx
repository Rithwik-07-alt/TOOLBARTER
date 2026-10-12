import React from 'react';
import { Wrench, ShieldCheck, MapPin, Code2 } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-brand">
          <div className="brand-logo-row">
            <div className="brand-icon-sm">
              <Wrench size={16} color="#ffffff" />
            </div>
            <span className="footer-brand-title">TOOLBARTER</span>
          </div>
          <p className="footer-tagline">
            Hyper-Local "One-Time-Use" Tool Barter Platform. Borrow nearby. Build together.
          </p>
        </div>

        <div className="footer-features">
          <div className="footer-feat-item">
            <MapPin size={16} className="feat-icon" />
            <span>Haversine Proximity Geodiscovery</span>
          </div>
          <div className="footer-feat-item">
            <ShieldCheck size={16} className="feat-icon" />
            <span>Verified Trust Ledger & Reviews</span>
          </div>
          <div className="footer-feat-item">
            <Code2 size={16} className="feat-icon" />
            <span>Temporal Double-Booking Prevention</span>
          </div>
        </div>

        <div className="footer-academic">
          <div className="academic-badge">B.Tech CSE Project</div>
          <div className="student-name">Ch. Rithwik &bull; Roll No: 24EG105P07</div>
          <div className="department">Department of Computer Science & Engineering</div>
        </div>
      </div>
      <div className="footer-bottom">
        &copy; {new Date().getFullYear()} TOOLBARTER. Built with React.js, Spring Boot 3.x, and MySQL Spatial.
      </div>
    </footer>
  );
}
