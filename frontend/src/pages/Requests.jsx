import React from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, Clock, MapPin, Share2, Car } from 'lucide-react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

// Fix Leaflet's default icon path issues
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Create custom icons for SOS and Donors
const emergencyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const donorIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

export default function Requests() {
  const navigate = useNavigate();

  const handleNewSOS = () => {
    navigate('/new-sos');
  };

  const requests = [
    { id: 1, type: 'O-', hospital: 'City General Hospital', distance: '3.2 km', time: '10 mins ago', urgency: 'CRITICAL', status: 'PENDING', lat: 12.9716, lng: 77.5946 },
    { id: 2, type: 'B+', hospital: 'Apollo Spectra', distance: '12 km', time: '1 hour ago', urgency: 'HIGH', status: 'IN_PROGRESS', lat: 12.9352, lng: 77.6245 },
  ];

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <AlertCircle color="var(--color-primary)" />
            Live Emergency Network
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Urgent blood requests mapped in real-time near you.</p>
        </div>
        <PrimaryButton onClick={handleNewSOS}>
          + New SOS Request
        </PrimaryButton>
      </div>

      <GlassCard style={{ padding: '0', overflow: 'hidden', marginBottom: '2rem', height: '400px', border: '1px solid var(--color-border)' }}>
        <MapContainer center={[12.95, 77.6]} zoom={12} style={{ height: '100%', width: '100%', background: '#111827' }}>
          <TileLayer
            url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          
          {requests.map(req => (
            <React.Fragment key={req.id}>
              <Marker position={[req.lat, req.lng]} icon={emergencyIcon}>
                <Popup>
                  <strong>{req.hospital}</strong><br />
                  Urgent: {req.type} Needed
                </Popup>
              </Marker>
              <Circle center={[req.lat, req.lng]} radius={3000} pathOptions={{ color: 'var(--color-primary)', fillColor: 'var(--color-primary)', fillOpacity: 0.1 }} />
            </React.Fragment>
          ))}

          {/* Dummy Donor Locations */}
          <Marker position={[12.96, 77.58]} icon={donorIcon}>
            <Popup>You (Verified Donor)</Popup>
          </Marker>
          <Marker position={[12.94, 77.63]} icon={donorIcon} />
          <Marker position={[12.98, 77.61]} icon={donorIcon} />
        </MapContainer>
      </GlassCard>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {requests.map(req => (
          <GlassCard key={req.id} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', borderLeft: req.urgency === 'CRITICAL' ? '4px solid var(--color-primary)' : '4px solid var(--color-warning)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
                <div style={{ 
                  width: '64px', height: '64px', 
                  borderRadius: '12px', 
                  background: req.urgency === 'CRITICAL' ? 'var(--color-primary)' : 'var(--color-warning)',
                  color: 'white',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', 
                  fontSize: '1.5rem', fontWeight: 'bold',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.2)'
                }}>
                  {req.type}
                </div>
                <div>
                  <h3 style={{ margin: 0 }}>Urgent {req.type} Blood Needed</h3>
                  <p style={{ margin: 0, color: 'var(--color-text-muted)', display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.25rem' }}>
                    <MapPin size={16} /> {req.hospital} • {req.distance}
                  </p>
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ 
                  padding: '0.25rem 0.75rem', 
                  borderRadius: '99px', 
                  fontSize: '0.75rem', 
                  fontWeight: '600',
                  background: req.urgency === 'CRITICAL' ? 'rgba(220,38,38,0.2)' : 'rgba(245,158,11,0.2)',
                  color: req.urgency === 'CRITICAL' ? 'var(--color-primary)' : 'var(--color-warning)'
                }}>
                  {req.urgency}
                </span>
                <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-muted)', display: 'flex', alignItems: 'center', gap: '0.25rem', marginTop: '0.5rem', justifyContent: 'flex-end' }}>
                  <Clock size={14} /> {req.time}
                </p>
              </div>
            </div>
            
            <div style={{ display: 'flex', gap: '1rem', borderTop: '1px solid var(--color-border)', paddingTop: '1.5rem', flexWrap: 'wrap' }}>
              <PrimaryButton 
                onClick={() => {
                  alert('You have accepted this request! Opening secure chat with coordinator...');
                  navigate('/chat');
                }} 
                style={{ flex: '1 1 150px', display: 'flex', justifyContent: 'center' }}>
                Accept Request
              </PrimaryButton>
              
              <PrimaryButton 
                variant="secondary"
                onClick={() => {
                  alert('Request accepted & driver requested! A volunteer driver will contact you shortly.');
                  navigate('/dashboard');
                }} 
                style={{ flex: '1 1 200px', display: 'flex', justifyContent: 'center', gap: '0.5rem', background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.3)' }}>
                <Car size={18} /> Accept & Request Driver
              </PrimaryButton>

              <button 
                onClick={() => alert('Request declined. We will notify other nearby donors.')}
                style={{ 
                flex: '0 1 auto',
                padding: '0 1rem', 
                background: 'rgba(255,255,255,0.05)', 
                border: '1px solid var(--color-border)', 
                color: 'var(--color-text)', 
                borderRadius: '8px', 
                fontWeight: '600', 
                cursor: 'pointer',
                transition: 'all 0.2s ease'
              }}>
                Decline
              </button>
            </div>
          </GlassCard>
        ))}
      </div>
    </div>
  );
}
