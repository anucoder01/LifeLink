import React, { useState } from 'react';
import { User, MapPin, Activity, Link as LinkIcon, Edit2, Shield, Search } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Dashboard() {
  const [isActive, setIsActive] = useState(true);
  const [locationName, setLocationName] = useState('Bengaluru, KA, India');
  const [isUpdatingLocation, setIsUpdatingLocation] = useState(false);

  const handleUpdateLocation = () => {
    setIsUpdatingLocation(true);
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser. Please enable location services.');
      setIsUpdatingLocation(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setIsUpdatingLocation(false);
        const { latitude, longitude } = position.coords;
        setLocationName(`Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}`);
        alert(`Location updated to Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}\nThis ensures you only receive alerts near you.`);
      },
      (error) => {
        setIsUpdatingLocation(false);
        if (error.code === error.PERMISSION_DENIED) {
          alert('Location access denied! LifeLink requires location services to match you with nearby emergencies. Please turn on your device location and allow permissions in the browser.');
        } else {
          alert('Failed to get location. Please ensure location services are enabled.');
        }
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <User color="var(--color-primary)" />
            Donor Dashboard
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Manage your profile and emergency readiness.</p>
        </div>
        <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--color-primary)' }}>
            <MapPin size={24} />
            <h3 style={{ margin: 0 }}>Current Location</h3>
          </div>
          <div>
            <p style={{ fontSize: '1.25rem', fontWeight: 'bold', margin: '0 0 0.5rem 0' }}>{locationName}</p>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Used for matching you with nearby SOS alerts.</p>
          </div>
          <button 
            disabled={isUpdatingLocation}
            onClick={handleUpdateLocation}
            style={{ 
              marginTop: 'auto', 
              background: 'rgba(255,255,255,0.05)', 
              border: '1px solid var(--color-border)', 
              color: 'var(--color-text)', 
              padding: '0.5rem', 
              borderRadius: '8px', 
              fontWeight: '600', 
              cursor: isUpdatingLocation ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s ease',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
            {isUpdatingLocation ? 'Locating...' : 'Update Location Now'}
          </button>
        </GlassCard>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <span style={{ fontSize: '0.875rem', color: isActive ? 'var(--color-success)' : 'var(--color-text-muted)' }}>
            {isActive ? 'Available for Emergencies' : 'Currently Unavailable'}
          </span>
          <button 
            onClick={() => setIsActive(!isActive)}
            style={{
              background: isActive ? 'var(--color-success)' : 'transparent',
              border: `1px solid ${isActive ? 'var(--color-success)' : 'var(--color-border)'}`,
              color: 'white',
              padding: '0.5rem 1rem',
              borderRadius: 'var(--radius-full)',
              cursor: 'pointer',
              fontWeight: '600',
              transition: 'all 0.2s ease'
            }}
          >
            {isActive ? 'Active' : 'Inactive'}
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
        {/* Profile Card */}
        <GlassCard>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Activity size={20} /> My Profile</h3>
            <button style={{ background: 'none', border: 'none', color: 'var(--color-text-muted)', cursor: 'pointer' }}><Edit2 size={16} /></button>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginBottom: '1.5rem' }}>
            <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem', fontWeight: 'bold' }}>
              O+
            </div>
            <div>
              <h4 style={{ margin: 0, fontSize: '1.25rem' }}>John Doe</h4>
              <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>+1 (555) 123-4567</p>
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.875rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>Total Donations</span>
              <span style={{ fontWeight: '600' }}>4</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>Last Donation</span>
              <span style={{ fontWeight: '600' }}>Oct 12, 2025</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>Eligibility</span>
              <span style={{ color: 'var(--color-success)', fontWeight: '600' }}>Eligible (Whole Blood)</span>
            </div>
          </div>
        </GlassCard>

        {/* Location Card */}
        <GlassCard>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
            <MapPin size={20} /> Current Location
          </h3>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
            Your location is used for geo-targeted emergency matching. We only track your last updated location.
          </p>
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem', border: '1px solid var(--color-border)' }}>
            <p style={{ margin: 0, fontWeight: '500', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
               📍 Bengaluru, KA, India
            </p>
            <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.25rem' }}>Updated 2 hours ago</p>
          </div>
          <PrimaryButton style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: '0.5rem' }}>
            <MapPin size={18} /> Update Location Now
          </PrimaryButton>
        </GlassCard>

        {/* Blood Chain Card */}
        <GlassCard>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
            <LinkIcon size={20} /> Blood Chain Contacts
          </h3>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
            Nominate up to 3 trusted contacts. If we can't find donors in a 30km radius, we'll send them a one-time SMS invite to register and help.
          </p>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.5rem' }}>
            {[1, 2, 3].map((num, i) => (
              <div key={num} style={{ padding: '0.75rem', borderRadius: 'var(--radius-md)', border: '1px dashed var(--color-border)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                {i === 0 ? (
                  <>
                    <div>
                      <p style={{ margin: 0, fontSize: '0.875rem', fontWeight: '500' }}>Jane Smith</p>
                      <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>+1 (555) 987-6543</p>
                    </div>
                    <button style={{ background: 'none', border: 'none', color: 'var(--color-text-muted)', cursor: 'pointer' }}>Remove</button>
                  </>
                ) : (
                  <span style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>+ Add Contact {num}</span>
                )}
              </div>
            ))}
          </div>
          <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <Shield size={12} /> Contact numbers are encrypted and masked.
          </p>
        </GlassCard>
      </div>
    </div>
  );
}
