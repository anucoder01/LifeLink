import React from 'react';
import { AlertCircle, Clock, MapPin, Share2 } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Requests() {
  const [isLocating, setIsLocating] = React.useState(false);

  const handleNewSOS = () => {
    setIsLocating(true);
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser. Please enable location services.');
      setIsLocating(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setIsLocating(false);
        const { latitude, longitude } = position.coords;
        console.log("Captured real coordinates for SOS:", { latitude, longitude });
        alert(`New SOS Request Initiated!\nYour exact location (Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}) will be sent to nearby donors immediately.`);
      },
      (error) => {
        setIsLocating(false);
        if (error.code === error.PERMISSION_DENIED) {
          alert('Location access denied! LifeLink requires your exact location to find nearby blood donors. Please turn on your device location and allow permissions in the browser.');
        } else {
          alert('Failed to get location. Please ensure location services are enabled.');
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  };

  const requests = [
    { id: 1, type: 'O-', hospital: 'City General Hospital', distance: '3.2 km', time: '10 mins ago', urgency: 'CRITICAL', status: 'PENDING' },
    { id: 2, type: 'B+', hospital: 'Apollo Spectra', distance: '12 km', time: '1 hour ago', urgency: 'HIGH', status: 'IN_PROGRESS' },
  ];

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <AlertCircle color="var(--color-primary)" />
            Emergency Requests
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Urgent blood requests near your location.</p>
        </div>
        <PrimaryButton disabled={isLocating} onClick={handleNewSOS}>
          {isLocating ? 'Locating...' : '+ New SOS Request'}
        </PrimaryButton>
      </div>

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
            
            <div style={{ display: 'flex', gap: '1rem', borderTop: '1px solid var(--color-border)', paddingTop: '1.5rem' }}>
              <PrimaryButton style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>Accept Request</PrimaryButton>
              <button style={{ 
                flex: 1, 
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
              <button style={{ 
                width: '48px', 
                background: 'rgba(255,255,255,0.05)', 
                border: '1px solid var(--color-border)', 
                color: 'var(--color-text)', 
                borderRadius: '8px', 
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                cursor: 'pointer',
                transition: 'all 0.2s ease'
              }}>
                <Share2 size={20} />
              </button>
            </div>
          </GlassCard>
        ))}
      </div>
    </div>
  );
}
