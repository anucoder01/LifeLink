import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix for default marker icons in react-leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});

L.Marker.prototype.options.icon = DefaultIcon;

const donorIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const emergencyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

export default function LiveMap({ center, emergencyTitle, donors = [] }) {
  const [mapCenter, setMapCenter] = useState(center || [12.9716, 77.5946]); // Default to Bangalore

  useEffect(() => {
    if (center) setMapCenter(center);
  }, [center]);

  return (
    <div style={{ height: '400px', width: '100%', borderRadius: '12px', overflow: 'hidden', border: '1px solid var(--color-border)' }}>
      <MapContainer center={mapCenter} zoom={13} style={{ height: '100%', width: '100%', zIndex: 0 }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />
        
        {/* Emergency Origin */}
        <Marker position={mapCenter} icon={emergencyIcon}>
          <Popup>
            <strong>{emergencyTitle || 'Emergency Location'}</strong>
          </Popup>
        </Marker>

        {/* 5km Radius */}
        <Circle center={mapCenter} radius={5000} pathOptions={{ color: 'var(--color-primary)', fillColor: 'var(--color-primary)', fillOpacity: 0.1, weight: 1 }} />
        {/* 15km Radius */}
        <Circle center={mapCenter} radius={15000} pathOptions={{ color: 'var(--color-warning)', fillColor: 'var(--color-warning)', fillOpacity: 0.05, weight: 1 }} />
        
        {/* Donors */}
        {donors.map(donor => (
          <Marker key={donor.id} position={[donor.lat, donor.lng]} icon={donorIcon}>
            <Popup>
              <strong>{donor.name}</strong><br/>
              Status: {donor.status}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}
