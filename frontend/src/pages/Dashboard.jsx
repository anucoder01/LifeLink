import React, { useState } from 'react';
import { User, MapPin, Activity, Link as LinkIcon, Shield, CheckCircle2, Phone, Heart, Trophy } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import PhoneInput from 'react-phone-number-input';
import 'react-phone-number-input/style.css';

export default function Dashboard() {
  const [isActive, setIsActive] = useState(true);
  const [locationName, setLocationName] = useState('Bengaluru, KA, India');
  const [isUpdatingLocation, setIsUpdatingLocation] = useState(false);
  
  const [vouchedContacts, setVouchedContacts] = useState([
    { id: 1, name: 'Jane Doe (Sister)', phone: '+1 (555) 123-4567' }
  ]);
  const [newContactPhone, setNewContactPhone] = useState();
  const [newContactName, setNewContactName] = useState('');

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

  const handleAddContact = (e) => {
    e.preventDefault();
    if (vouchedContacts.length >= 3) {
      alert("You can only have up to 3 contacts.");
      return;
    }
    setVouchedContacts([...vouchedContacts, { id: Date.now(), name: newContactName, phone: newContactPhone }]);
    setNewContactName('');
    setNewContactPhone('');
  };

  const removeContact = (id) => {
    setVouchedContacts(vouchedContacts.filter(c => c.id !== id));
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
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <span style={{ fontSize: '0.875rem', color: isActive ? 'var(--color-success)' : 'var(--color-text-muted)' }}>
            {isActive ? 'Available for Emergencies' : 'Currently Unavailable'}
          </span>
          <PrimaryButton onClick={() => setIsActive(!isActive)} variant={isActive ? 'secondary' : 'primary'} style={{ padding: '0.5rem 1rem' }}>
            {isActive ? 'Go Offline' : 'Go Online'}
          </PrimaryButton>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
        
        {/* Impact Stats */}
        <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: '1rem', gridColumn: '1 / -1', background: 'linear-gradient(135deg, rgba(220,38,38,0.1) 0%, rgba(0,0,0,0.4) 100%)', border: '1px solid rgba(220,38,38,0.2)' }}>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '0 0 0.5rem 0' }}>
            <Trophy color="var(--color-primary)" />
            Your Impact
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
            <div style={{ padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', textAlign: 'center' }}>
              <Heart size={24} color="var(--color-primary)" style={{ margin: '0 auto 0.5rem' }} />
              <h4 style={{ margin: 0, fontSize: '1.5rem' }}>3</h4>
              <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Lives Saved</p>
            </div>
            <div style={{ padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', textAlign: 'center' }}>
              <Activity size={24} color="#10b981" style={{ margin: '0 auto 0.5rem' }} />
              <h4 style={{ margin: 0, fontSize: '1.5rem' }}>14</h4>
              <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Days Streak</p>
            </div>
            <div style={{ padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', textAlign: 'center' }}>
              <User size={24} color="var(--color-warning)" style={{ margin: '0 auto 0.5rem' }} />
              <h4 style={{ margin: 0, fontSize: '1.5rem' }}>Top 5%</h4>
              <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>City Rank</p>
            </div>
          </div>
        </GlassCard>
        
        {/* Health Profile */}
        <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--color-primary)' }}>
            <Activity size={24} />
            <h3 style={{ margin: 0 }}>Health Profile</h3>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginBottom: '1rem' }}>
            <div style={{ width: '64px', height: '64px', borderRadius: '50%', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem', fontWeight: 'bold' }}>
              O+
            </div>
            <div>
              <h4 style={{ margin: 0, fontSize: '1.25rem' }}>John Doe</h4>
              <p style={{ margin: 0, color: 'var(--color-success)', fontWeight: 'bold', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                <CheckCircle2 size={16} /> Eligible to donate
              </p>
            </div>
          </div>
          <div>
            <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold' }}>Last Donation</p>
            <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Jan 10, 2026 (Whole Blood)</p>
          </div>
          <div style={{ background: 'rgba(255,255,255,0.05)', borderRadius: '99px', height: '8px', overflow: 'hidden' }}>
            <div style={{ background: 'var(--color-primary)', width: '100%', height: '100%' }}></div>
          </div>
          <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textAlign: 'right', margin: 0 }}>
            Cooldown complete.
          </p>
        </GlassCard>

        {/* Location Card */}
        <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--color-primary)' }}>
            <MapPin size={24} />
            <h3 style={{ margin: 0 }}>Current Location</h3>
          </div>
          <div>
            <p style={{ fontSize: '1.25rem', fontWeight: 'bold', margin: '0 0 0.5rem 0' }}>{locationName}</p>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Your location is used for geo-targeted emergency matching. We only track your last updated location.</p>
          </div>
          <PrimaryButton 
            disabled={isUpdatingLocation}
            onClick={handleUpdateLocation}
            style={{ marginTop: 'auto', width: '100%', display: 'flex', justifyContent: 'center' }}>
            {isUpdatingLocation ? 'Locating...' : 'Update Location Now'}
          </PrimaryButton>
        </GlassCard>

        {/* Recent Activity Feed */}
        <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: '1rem', gridColumn: '1 / -1' }}>
          <h3 style={{ margin: '0 0 0.5rem 0' }}>Recent Activity</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start', borderBottom: '1px solid var(--color-border)', paddingBottom: '1rem' }}>
              <div style={{ padding: '0.5rem', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '50%', color: '#10b981' }}>
                <CheckCircle2 size={20} />
              </div>
              <div>
                <p style={{ margin: '0 0 0.25rem 0', fontWeight: '500' }}>Donation Successful</p>
                <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>You completed a whole blood donation at City General Hospital.</p>
                <span style={{ fontSize: '0.75rem', color: 'var(--color-primary)', marginTop: '0.25rem', display: 'block' }}>Jan 10, 2026</span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start', borderBottom: '1px solid var(--color-border)', paddingBottom: '1rem' }}>
              <div style={{ padding: '0.5rem', background: 'rgba(245, 158, 11, 0.1)', borderRadius: '50%', color: 'var(--color-warning)' }}>
                <Shield size={20} />
              </div>
              <div>
                <p style={{ margin: '0 0 0.25rem 0', fontWeight: '500' }}>Earned Bronze Donor Badge</p>
                <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>Awarded for completing 3 lifetime donations!</p>
                <span style={{ fontSize: '0.75rem', color: 'var(--color-primary)', marginTop: '0.25rem', display: 'block' }}>Jan 11, 2026</span>
              </div>
            </div>

          </div>
        </GlassCard>

        {/* Nearby Active Emergencies */}
        <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: '1rem', gridColumn: '1 / -1', background: 'rgba(220,38,38,0.02)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Activity color="var(--color-primary)" /> Local Emergency Radar
            </h3>
            <span style={{ fontSize: '0.75rem', color: 'var(--color-primary)', fontWeight: 'bold' }}>Live Data</span>
          </div>
          
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
            <div style={{ flex: '1 1 250px', background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                <span style={{ background: 'var(--color-primary)', color: 'white', padding: '0.25rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold' }}>O- URGENT</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>2.4 km away</span>
              </div>
              <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem' }}>Apollo Hospital ER</p>
              <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Awaiting compatible donors. Radius expanding...</p>
            </div>
            
            <div style={{ flex: '1 1 250px', background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                <span style={{ background: 'var(--color-warning)', color: 'white', padding: '0.25rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold' }}>A+ CRITICAL</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>4.1 km away</span>
              </div>
              <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem' }}>City General Trauma Center</p>
              <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>Matched donors notified via FCM.</p>
            </div>
          </div>
        </GlassCard>

        {/* Blood Chain Card */}
        <GlassCard style={{ gridColumn: '1 / -1' }}>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
            <LinkIcon color="var(--color-primary)" size={24} /> Blood Chain Network 🔗
          </h3>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
            Nominate up to 3 trusted contacts. If we can't find donors in a 30km radius, we'll send them a one-time SMS invite to register and help.
          </p>
          
          <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
            {/* Contact List */}
            <div style={{ flex: '1 1 300px', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {[0, 1, 2].map((idx) => {
                const contact = vouchedContacts[idx];
                if (contact) {
                  return (
                    <div key={contact.id} style={{ padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <div>
                        <p style={{ margin: 0, fontSize: '1rem', fontWeight: 'bold' }}>{contact.name}</p>
                        <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--color-text-muted)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                          <Phone size={14} /> {contact.phone}
                        </p>
                      </div>
                      <PrimaryButton variant="danger" onClick={() => removeContact(contact.id)} style={{ padding: '0.25rem 0.75rem', fontSize: '0.75rem' }}>Remove</PrimaryButton>
                    </div>
                  );
                } else {
                  return (
                    <div key={`empty-${idx}`} style={{ padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px dashed var(--color-border)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <span style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>+ Empty Vouch Slot</span>
                    </div>
                  );
                }
              })}
              <p style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'flex', alignItems: 'center', gap: '0.25rem', marginTop: '0.5rem' }}>
                <Shield size={12} /> Contact numbers are encrypted and masked.
              </p>
            </div>

            {/* Add Contact Form */}
            <div style={{ flex: '1 1 300px', background: 'rgba(255,255,255,0.02)', padding: '1.5rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)' }}>
              <h4 style={{ margin: '0 0 1rem 0' }}>Add a Trusted Contact</h4>
              <form onSubmit={handleAddContact} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.75rem' }}>Contact Name (e.g. Brother)</label>
                  <input required value={newContactName} onChange={(e) => setNewContactName(e.target.value)} type="text" placeholder="John Smith" className="form-input" style={{ width: '100%', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--color-border)', background: 'transparent', color: 'white' }} />
                </div>
                <div className="phone-input-wrapper">
                  <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.75rem' }}>Phone Number</label>
                  <PhoneInput
                    international
                    defaultCountry="IN"
                    value={newContactPhone}
                    onChange={setNewContactPhone}
                    required
                    className="form-input custom-phone"
                    style={{ width: '100%', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--color-border)', background: 'transparent', color: 'white' }}
                  />
                </div>
                <PrimaryButton disabled={vouchedContacts.length >= 3} type="submit" style={{ marginTop: '0.5rem' }}>+ Vouch for Contact</PrimaryButton>
              </form>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
}
