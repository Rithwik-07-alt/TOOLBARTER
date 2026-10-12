import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/apiClient';
import ItemCard from '../components/ItemCard';
import { Compass, UserPlus, PlusCircle, ArrowRight, ShieldCheck, CheckCircle2 } from 'lucide-react';

export default function LandingPage() {
  const [impact, setImpact] = useState({
    itemsListed: 0,
    completedExchanges: 0,
    distanceSavedKm: 0,
    averageTrustScore: 0,
  });
  const [featuredItems, setFeaturedItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    async function loadData() {
      try {
        const [statsData, itemsData] = await Promise.all([
          apiClient.getNeighbourhoodImpact().catch(() => ({
            itemsListed: 0,
            completedExchanges: 0,
            distanceSavedKm: 0,
            averageTrustScore: 0,
          })),
          apiClient.getItems().catch(() => []),
        ]);
        setImpact(statsData);
        setFeaturedItems(itemsData.slice(0, 4));
      } catch (err) {
        console.error('Failed to load landing page data:', err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  return (
    <div className="landing-page">
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-badge-wrap">
          <div className="hero-badge">
            <span className="status-indicator" />
            <span>Hyper-Local Peer-to-Peer Tool Barter</span>
          </div>
        </div>

        <h1 className="hero-title">
          Borrow Nearby. <span className="hero-highlight">Build Together.</span>
        </h1>

        <p className="hero-description">
          Find the tool you need a street away, borrow it for exactly the hours you need,
          and lend out what’s gathering dust in your garage.
        </p>

        <div className="hero-buttons">
          <Link to="/discover" className="btn btn-primary btn-lg">
            <Compass size={18} />
            <span>Explore Nearby Tools</span>
          </Link>
          <Link to="/register?role=BORROWER" className="btn btn-secondary btn-lg">
            <UserPlus size={18} />
            <span>Join as Borrower</span>
          </Link>
          <Link to="/items/new" className="btn btn-outline btn-lg">
            <PlusCircle size={18} />
            <span>List an Item</span>
          </Link>
        </div>
      </section>

      {/* Neighbourhood Impact Section (Live MySQL Totals) */}
      <section id="impact" className="section section-impact">
        <div className="section-header">
          <div className="section-subtitle">Real Proximity Supply</div>
          <h2 className="section-title">Neighbourhood Impact</h2>
          <p className="section-desc">
            Derived dynamically from live database records and verified completed exchanges.
          </p>
        </div>

        <div className="impact-grid">
          <div className="impact-card">
            <div className="impact-num">{impact.itemsListed}</div>
            <div className="impact-label">Tools Listed</div>
            <div className="impact-sub">Available across local streets</div>
          </div>

          <div className="impact-card">
            <div className="impact-num">{impact.completedExchanges}</div>
            <div className="impact-label">Completed Exchanges</div>
            <div className="impact-sub">Returned with zero disputes</div>
          </div>

          <div className="impact-card">
            <div className="impact-num">{impact.distanceSavedKm} km</div>
            <div className="impact-label">Travel Distance Saved</div>
            <div className="impact-sub">Calculated via Haversine great-circle</div>
          </div>

          <div className="impact-card">
            <div className="impact-num">{impact.averageTrustScore} pts</div>
            <div className="impact-label">Average Community Trust</div>
            <div className="impact-sub">Rule-based reputation ledger</div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="section">
        <div className="section-header">
          <div className="section-subtitle">Frictionless Lifecycle</div>
          <h2 className="section-title">How TOOLBARTER Works</h2>
          <p className="section-desc">
            A complete 5-stage verifiable loop connecting idle tools with neighbours who need them.
          </p>
        </div>

        <div className="steps-grid">
          <div className="step-card">
            <div className="step-num">01</div>
            <h3 className="step-title">List Item</h3>
            <p className="step-desc">
              Owners list garage items with home coordinates, category, condition notes, and bookable availability slots.
            </p>
          </div>

          <div className="step-card">
            <div className="step-num">02</div>
            <h3 className="step-title">Discover</h3>
            <p className="step-desc">
              Borrowers explore a distance-ranked radar map powered by Java Haversine calculations and MySQL spatial coordinates.
            </p>
          </div>

          <div className="step-card">
            <div className="step-num">03</div>
            <h3 className="step-title">Request</h3>
            <p className="step-desc">
              Select an open slot. Backend checks for scheduling conflicts and locks the window against double-booking.
            </p>
          </div>

          <div className="step-card">
            <div className="step-num">04</div>
            <h3 className="step-title">Borrow</h3>
            <p className="step-desc">
              Pickup handover is confirmed by the owner, shifting the item to ON_LOAN status with verifiable timestamps.
            </p>
          </div>

          <div className="step-card">
            <div className="step-num">05</div>
            <h3 className="step-title">Return & Review</h3>
            <p className="step-desc">
              Both parties submit trust-based ratings and reviews. Trust scores (+5 completion, star bonuses) update dynamically.
            </p>
          </div>
        </div>
      </section>

      {/* Featured Items Preview */}
      <section id="featured" className="section">
        <div className="section-header">
          <div className="section-subtitle">Real Live Database Inventory</div>
          <h2 className="section-title">Featured Nearby Tools</h2>
          <p className="section-desc">
            Live tools registered by neighbours in the surrounding radius.
          </p>
        </div>

        {featuredItems.length === 0 && !loading ? (
          <div className="empty-state-box">
            <p>No tools listed yet. Be the first neighbour to list a tool!</p>
            <Link to="/items/new" className="btn btn-primary btn-sm">List a Tool</Link>
          </div>
        ) : (
          <div className="items-grid">
            {featuredItems.map((item) => (
              <ItemCard key={item.id} item={item} />
            ))}
          </div>
        )}

        <div style={{ textAlign: 'center', marginTop: '2.5rem' }}>
          <Link to="/discover" className="btn btn-secondary">
            <span>Explore All Tools on Radar Map</span>
            <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      {/* Architecture & Engineering Section */}
      <section id="architecture" className="section section-tech">
        <div className="section-header">
          <div className="section-subtitle">Academic Implementation Architecture</div>
          <h2 className="section-title">System Architecture Blueprint</h2>
        </div>

        <div className="arch-flow-grid">
          <div className="arch-card">
            <div className="arch-step-tag">Frontend Tier</div>
            <h4 className="arch-title">React.js & Canvas Radar</h4>
            <p className="arch-desc">
              Single-page application with Leaflet.js and HTML5 Canvas radar sweep rendered outside React's virtual-DOM for 60fps responsiveness.
            </p>
          </div>

          <div className="arch-card">
            <div className="arch-step-tag">REST API & Security</div>
            <h4 className="arch-title">Spring Boot 3.x & JWT</h4>
            <p className="arch-desc">
              Stateless JWT authentication filter, BCrypt password hashing, Bean Validation, and ownership-guarded HTTP controllers.
            </p>
          </div>

          <div className="arch-card">
            <div className="arch-step-tag">Domain Service Layer</div>
            <h4 className="arch-title">Haversine & Scheduling Rules</h4>
            <p className="arch-desc">
              Java services calculate great-circle distances, enforce temporal overlap locks, and recalculate trust levels idempotently.
            </p>
          </div>

          <div className="arch-card">
            <div className="arch-step-tag">Persistence Tier</div>
            <h4 className="arch-title">MySQL 8.0+ Spatial Engine</h4>
            <p className="arch-desc">
              Normalized tables for users, items, availability slots, requests, and reviews with POINT SRID 4326 spatial indexing.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}
