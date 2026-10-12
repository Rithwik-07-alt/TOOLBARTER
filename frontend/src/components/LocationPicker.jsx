import React, { useEffect, useRef } from 'react';
import L from 'leaflet';
import { MapPin, Navigation } from 'lucide-react';

export default function LocationPicker({
  latitude,
  longitude,
  onChange,
  label = 'Select Location',
  height = '240px',
}) {
  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerRef = useRef(null);

  const lat = latitude || 17.4401;
  const lng = longitude || 78.3826;

  useEffect(() => {
    if (!mapContainerRef.current) return;

    const map = L.map(mapContainerRef.current, {
      center: [lat, lng],
      zoom: 14,
      attributionControl: false,
    });
    mapInstanceRef.current = map;

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);

    const pickerIcon = L.divIcon({
      className: 'location-picker-marker',
      html: `<div class="picker-pin">📍</div>`,
      iconSize: [30, 30],
      iconAnchor: [15, 30],
    });

    const marker = L.marker([lat, lng], {
      icon: pickerIcon,
      draggable: true,
    }).addTo(map);
    markerRef.current = marker;

    marker.on('dragend', (e) => {
      const position = e.target.getLatLng();
      onChange({
        latitude: Math.round(position.lat * 100000) / 100000,
        longitude: Math.round(position.lng * 100000) / 100000,
      });
    });

    map.on('click', (e) => {
      marker.setLatLng(e.latlng);
      onChange({
        latitude: Math.round(e.latlng.lat * 100000) / 100000,
        longitude: Math.round(e.latlng.lng * 100000) / 100000,
      });
    });

    const timer1 = setTimeout(() => {
      map.invalidateSize();
    }, 100);
    const timer2 = setTimeout(() => {
      map.invalidateSize();
    }, 400);

    let resizeObserver;
    if (window.ResizeObserver && mapContainerRef.current) {
      resizeObserver = new ResizeObserver(() => {
        map.invalidateSize();
      });
      resizeObserver.observe(mapContainerRef.current);
    }

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      resizeObserver?.disconnect();
      map.remove();
    };
  }, []);

  useEffect(() => {
    if (markerRef.current && mapInstanceRef.current && latitude && longitude) {
      markerRef.current.setLatLng([latitude, longitude]);
      mapInstanceRef.current.panTo([latitude, longitude]);
    }
  }, [latitude, longitude]);

  const handleUseBrowserLocation = () => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const newLat = Math.round(pos.coords.latitude * 100000) / 100000;
          const newLng = Math.round(pos.coords.longitude * 100000) / 100000;
          onChange({ latitude: newLat, longitude: newLng });
        },
        (err) => {
          alert('Could not retrieve browser geolocation: ' + err.message);
        }
      );
    } else {
      alert('Geolocation is not supported by your browser.');
    }
  };

  return (
    <div className="location-picker-group">
      <div className="location-picker-header">
        <label className="form-label">
          <MapPin size={14} className="inline-icon" /> {label}
        </label>
        <button
          type="button"
          onClick={handleUseBrowserLocation}
          className="btn-locate-me"
          title="Detect GPS location"
        >
          <Navigation size={12} /> Detect Location
        </button>
      </div>

      <div
        ref={mapContainerRef}
        className="location-picker-map"
        style={{ height: height || '250px', minHeight: height || '250px', width: '100%', borderRadius: 'var(--radius-md)' }}
      />

      <div className="coords-input-row">
        <div className="coords-input-col">
          <label className="coords-sublabel">Latitude</label>
          <input
            type="number"
            step="0.00001"
            className="form-input form-input-sm"
            value={latitude || ''}
            onChange={(e) =>
              onChange({ latitude: parseFloat(e.target.value) || 0, longitude })
            }
            placeholder="Enter latitude"
            required
          />
        </div>
        <div className="coords-input-col">
          <label className="coords-sublabel">Longitude</label>
          <input
            type="number"
            step="0.00001"
            className="form-input form-input-sm"
            value={longitude || ''}
            onChange={(e) =>
              onChange({ latitude, longitude: parseFloat(e.target.value) || 0 })
            }
            placeholder="Enter longitude"
            required
          />
        </div>
      </div>
      <p className="coords-hint">
        Click anywhere on the map or drag the pin to set your exact coordinates.
      </p>
    </div>
  );
}
