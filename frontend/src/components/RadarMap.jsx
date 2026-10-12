import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import L from 'leaflet';

export default function RadarMap({ items = [], center = [17.4401, 78.3826], radiusKm = 10, onItemClick }) {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const canvasLayerRef = useRef(null);
  const markersLayerRef = useRef(null);
  const animFrameIdRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!mapContainerRef.current) return;

    // 1. Initialize Leaflet Map
    const map = L.map(mapContainerRef.current, {
      center: center,
      zoom: 13,
      zoomControl: true,
      attributionControl: false,
    });
    mapInstanceRef.current = map;

    // Standard OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);

    // Marker group
    const markersGroup = L.layerGroup().addTo(map);
    markersLayerRef.current = markersGroup;

    // User center marker
    const userIcon = L.divIcon({
      className: 'user-radar-center-marker',
      html: `
        <div class="user-pulse-ring"></div>
        <div class="user-center-dot"></div>
      `,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
    });
    L.marker(center, { icon: userIcon })
      .bindPopup('<b>Your Location</b><br>Radar origin')
      .addTo(markersGroup);

    // Search Radius Circle
    L.circle(center, {
      radius: radiusKm * 1000,
      color: '#3b82f6',
      fillColor: '#3b82f6',
      fillOpacity: 0.05,
      weight: 1.5,
      dashArray: '4, 8',
    }).addTo(markersGroup);

    // 2. Custom Canvas Radar Sweep Layer
    const canvas = document.createElement('canvas');
    canvas.className = 'radar-sweep-canvas';
    canvas.style.position = 'absolute';
    canvas.style.top = '0';
    canvas.style.left = '0';
    canvas.style.pointerEvents = 'none';
    canvas.style.zIndex = '400';
    map.getPanes().overlayPane.appendChild(canvas);
    canvasLayerRef.current = canvas;

    let angle = 0;
    const ctx = canvas.getContext('2d');

    function resizeCanvas() {
      const size = map.getSize();
      canvas.width = size.x;
      canvas.height = size.y;
    }
    resizeCanvas();
    map.on('resize', resizeCanvas);
    map.on('move', resizeCanvas);

    function drawRadar() {
      if (!ctx || !mapInstanceRef.current) return;
      const size = map.getSize();
      ctx.clearRect(0, 0, size.x, size.y);

      // Convert center lat/lng to container pixel point
      const originPoint = map.latLngToContainerPoint(center);

      // Convert radiusKm to pixel radius
      const edgeLatLng = L.latLng(center[0], center[1] + (radiusKm / 111.32));
      const edgePoint = map.latLngToContainerPoint(edgeLatLng);
      const pixelRadius = Math.max(30, Math.abs(edgePoint.x - originPoint.x));

      // Draw radar sweep beam
      ctx.save();
      ctx.beginPath();
      ctx.moveTo(originPoint.x, originPoint.y);
      ctx.arc(originPoint.x, originPoint.y, pixelRadius, angle - 0.25, angle);
      ctx.closePath();

      const gradient = ctx.createRadialGradient(
        originPoint.x, originPoint.y, 0,
        originPoint.x, originPoint.y, pixelRadius
      );
      gradient.addColorStop(0, 'rgba(59, 130, 246, 0.35)');
      gradient.addColorStop(1, 'rgba(59, 130, 246, 0.0)');
      ctx.fillStyle = gradient;
      ctx.fill();

      // Sweep front line
      ctx.beginPath();
      ctx.moveTo(originPoint.x, originPoint.y);
      ctx.lineTo(
        originPoint.x + pixelRadius * Math.cos(angle),
        originPoint.y + pixelRadius * Math.sin(angle)
      );
      ctx.strokeStyle = 'rgba(96, 165, 250, 0.8)';
      ctx.lineWidth = 1.5;
      ctx.stroke();

      ctx.restore();

      angle += 0.02;
      if (angle > Math.PI * 2) angle = 0;

      animFrameIdRef.current = requestAnimationFrame(drawRadar);
    }

    drawRadar();

    // Invalidate map size to prevent tile misalignment or blank gaps
    const timer1 = setTimeout(() => {
      map.invalidateSize();
      resizeCanvas();
    }, 100);
    const timer2 = setTimeout(() => {
      map.invalidateSize();
      resizeCanvas();
    }, 400);

    let resizeObserver;
    if (window.ResizeObserver && mapContainerRef.current) {
      resizeObserver = new ResizeObserver(() => {
        map.invalidateSize();
        resizeCanvas();
      });
      resizeObserver.observe(mapContainerRef.current);
    }

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      resizeObserver?.disconnect();
      if (animFrameIdRef.current) {
        cancelAnimationFrame(animFrameIdRef.current);
      }
      if (map) {
        map.remove();
      }
    };
  }, [center[0], center[1], radiusKm]);

  // Update item markers when items change
  useEffect(() => {
    if (!mapInstanceRef.current || !markersLayerRef.current) return;
    const markersGroup = markersLayerRef.current;

    // Clear previous item markers (keep center & circle)
    const layers = markersGroup.getLayers();
    // Keep first 2 layers (origin marker and circle)
    layers.slice(2).forEach(l => markersGroup.removeLayer(l));

    // Re-create items
    items.forEach((item) => {
      if (!item.latitude || !item.longitude) return;

      const itemIcon = L.divIcon({
        className: 'radar-item-marker',
        html: `
          <div class="marker-pin ${item.oneTimeUse ? 'one-time-pin' : ''}">
            <span class="marker-dot"></span>
          </div>
        `,
        iconSize: [28, 28],
        iconAnchor: [14, 28],
      });

      const marker = L.marker([item.latitude, item.longitude], { icon: itemIcon });

      const popupContent = document.createElement('div');
      popupContent.className = 'radar-popup-card';
      popupContent.innerHTML = `
        <div class="popup-category">${item.categoryDisplay || item.category || 'Tool'}</div>
        <div class="popup-title">${item.title}</div>
        <div class="popup-meta">
          <span>📍 ${item.distanceKm != null ? item.distanceKm + ' km away' : 'Nearby'}</span>
          ${item.ownerTrustScore != null ? `<span>★ ${item.ownerTrustScore} pts (${item.ownerTrustLevelDisplay || 'Trust'})</span>` : ''}
        </div>
        <div class="popup-slot">Next available: ${item.nextAvailableSlot || 'Contact owner'}</div>
        <button class="popup-btn" id="btn-view-${item.id}">View Tool</button>
      `;

      popupContent.querySelector(`#btn-view-${item.id}`)?.addEventListener('click', () => {
        if (onItemClick) {
          onItemClick(item);
        } else {
          navigate(`/items/${item.id}`);
        }
      });

      marker.bindPopup(popupContent);
      marker.addTo(markersGroup);
    });
  }, [items, navigate, onItemClick]);

  return (
    <div className="radar-map-wrapper">
      <div ref={mapContainerRef} className="radar-leaflet-container" />
      <div className="radar-hud-overlay">
        <div className="hud-badge">
          <span className="hud-live-dot" />
          <span>RADAR ACTIVE &bull; {items.length} {items.length === 1 ? 'tool' : 'tools'}</span>
        </div>
        <div className="hud-radius-indicator">Radius: {radiusKm} km</div>
      </div>
    </div>
  );
}
