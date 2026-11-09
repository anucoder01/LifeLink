import React, { useEffect, useState } from 'react';
import GoogleMapView from './GoogleMapView';

export default function LiveMap({ center, emergencyTitle, donors = [] }) {
  const [mapCenter, setMapCenter] = useState(center || [12.9716, 77.5946]); // Default to Bangalore

  useEffect(() => {
    if (center) setMapCenter(center);
  }, [center]);

  const markers = [
    { position: mapCenter, title: emergencyTitle || 'Emergency Location', label: emergencyTitle || 'Emergency Location', color: '#e11d48' },
    ...donors.map(donor => ({
      position: [donor.lat, donor.lng],
      title: donor.name,
      label: `${donor.name} · Status: ${donor.status}`,
      color: '#16a34a'
    }))
  ];
  const circles = [
    { center: mapCenter, radius: 5000, color: '#3b82f6' },
    { center: mapCenter, radius: 15000, color: '#f59e0b' }
  ];
  return (
    <div style={{ height: '400px', width: '100%', borderRadius: '12px', overflow: 'hidden', border: '1px solid var(--color-border)' }}>
      <GoogleMapView center={mapCenter} zoom={13} markers={markers} circles={circles} />
    </div>
  );
}
