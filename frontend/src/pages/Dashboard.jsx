import React, { useEffect, useState } from 'react';
import { Heart, Link as LinkIcon, MapPin, Navigation, Phone, Shield, User } from 'lucide-react';
import PhoneInput from 'react-phone-number-input';
import 'react-phone-number-input/style.css';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import GamificationCard from '../components/GamificationCard';
import RecoveryCompanion from '../components/RecoveryCompanion';
import LiveMapModal from '../components/LiveMapModal';
import LeaderboardModal from '../components/LeaderboardModal';
import { api } from '../services/api';

export default function Dashboard() {
  const [profile, setProfile] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [notificationPrefs, setNotificationPrefs] = useState(null);
  const [contactName, setContactName] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [liveMapOpen, setLiveMapOpen] = useState(false);
  const [leaderboardOpen, setLeaderboardOpen] = useState(false);

  const loadProfile = async () => {
    const { data } = await api.get('/donors/me');
    setProfile(data);
    localStorage.setItem('donorName', data.name);
  };
  const loadContacts = async () => {
    const { data } = await api.get('/donors/me/vouches?size=3');
    setContacts((data.content || []).map(item => ({ id: item.id, name: item.contactName, phone: item.contactPhoneMasked })));
  };
  const loadNotificationPrefs = async () => {
    const { data } = await api.get('/donors/me/notifications');
    setNotificationPrefs(data);
  };
  useEffect(() => {
    Promise.all([loadProfile(), loadContacts(), loadNotificationPrefs()]).catch(() => setError('Donor profile is unavailable. Sign in with a donor account and retry.'));
  }, []);

  const toggleAvailability = async () => {
    if (!profile) return;
    const active = !profile.isActive;
    setBusy(true);
    try { const { data } = await api.put(`/donors/me/active?active=${active}`); setProfile(data); setError(''); }
    catch { setError('Could not update donor availability.'); }
    finally { setBusy(false); }
  };

  const updateLocation = () => {
    if (!navigator.geolocation) { setError('This browser does not support location updates.'); return; }
    setBusy(true);
    navigator.geolocation.getCurrentPosition(async ({ coords }) => {
      try {
        const { data } = await api.put('/donors/me/location', { latitude: coords.latitude, longitude: coords.longitude });
        setProfile(data); setError('');
      } catch { setError('Could not save location.'); }
      finally { setBusy(false); }
    }, () => { setBusy(false); setError('Location permission was not granted.'); }, { enableHighAccuracy: true, timeout: 15000 });
  };

  const addContact = async event => {
    event.preventDefault(); setBusy(true);
    try {
      await api.post('/donors/me/vouches', { contactName, contactPhone });
      setContactName(''); setContactPhone(''); await loadContacts(); setError('');
    } catch { setError('Could not add contact. Check the phone number and your three-contact limit.'); }
    finally { setBusy(false); }
  };
  const removeContact = async id => {
    try { await api.delete(`/donors/me/vouches/${id}`); await loadContacts(); setError(''); }
    catch { setError('Could not remove this contact.'); }
  };
  const updateNotificationPref = async patch => {
    try {
      const { data } = await api.put('/donors/me/notifications', patch);
      setNotificationPrefs(data); setError('');
    } catch { setError('Notification preferences could not be saved.'); }
  };

  return <main className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
    <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 16, flexWrap: 'wrap', marginBottom: 24 }}>
      <div><h2><User color="var(--color-primary)" /> Donor dashboard</h2><p>Manage your availability, location, and trusted contacts.</p></div>
      <div>
        <span>{profile ? (profile.isActive ? 'Available for alerts' : 'Unavailable for alerts') : 'Loading status…'}</span>{' '}
        <PrimaryButton disabled={!profile || busy} onClick={toggleAvailability}>{profile?.isActive ? 'Go offline' : 'Go online'}</PrimaryButton>
      </div>
    </header>
    {error && <p role="alert">{error}</p>}
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(280px,1fr))', gap: 16 }}>
      <GlassCard>
        <h3><Heart color="var(--color-primary)" /> Donor profile</h3>
        <p><strong>{profile?.name || localStorage.getItem('donorName') || 'Donor'}</strong></p>
        <p>Blood type: {profile?.bloodType || 'Loading…'}</p>
        <p>Last donation: {profile?.lastDonationDate || 'Not recorded'}</p>
        <button onClick={() => setLiveMapOpen(true)}><Navigation size={16} /> View donor locations for a request</button>
      </GlassCard>
      <GlassCard>
        <h3><MapPin color="var(--color-primary)" /> Location</h3>
        <p>{profile?.latitude != null && profile?.longitude != null
          ? `${profile.latitude.toFixed(4)}, ${profile.longitude.toFixed(4)}`
          : 'No location saved'}</p>
        <PrimaryButton disabled={busy} onClick={updateLocation}>Update my location</PrimaryButton>
      </GlassCard>
    </div>

    <GlassCard style={{ marginTop: 16 }}>
      <h3><LinkIcon color="var(--color-primary)" /> Blood Chain contacts</h3>
      <p>Nominate up to three trusted contacts for an emergency invite if donor matching finds no eligible donors.</p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(240px,1fr))', gap: 10 }}>
        {contacts.map(contact => <div key={contact.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
          <span><strong>{contact.name}</strong> · <Phone size={14} /> {contact.phone}</span>
          <button onClick={() => removeContact(contact.id)}>Remove</button>
        </div>)}
        {!contacts.length && <p>No trusted contacts added.</p>}
      </div>
      <form onSubmit={addContact} style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'end', marginTop: 16 }}>
        <label>Contact name<input required maxLength="100" value={contactName} onChange={e => setContactName(e.target.value)} /></label>
        <label>Phone<PhoneInput international defaultCountry="IN" value={contactPhone} onChange={value => setContactPhone(value || '')} required /></label>
        <PrimaryButton disabled={busy || contacts.length >= 3}>Add trusted contact</PrimaryButton>
      </form>
      <p><Shield size={14} /> Phone numbers are masked in the contact list.</p>
    </GlassCard>

    <GlassCard style={{ marginTop: 16 }}>
      <h3>Notification preferences</h3>
      <p>SMS and WhatsApp are off until you opt in. Reply STOP to an alert to disable messages; reply START to opt back in.</p>
      <label style={{ display: 'block' }}><input type="checkbox" checked={notificationPrefs?.smsEnabled || false} onChange={event => updateNotificationPref({ smsEnabled: event.target.checked })} /> Allow SMS alerts</label>
      <label style={{ display: 'block' }}><input type="checkbox" checked={notificationPrefs?.whatsappEnabled || false} onChange={event => updateNotificationPref({ whatsappEnabled: event.target.checked })} /> Allow WhatsApp alerts</label>
      {notificationPrefs?.smsOptedOut && <p role="status">Messages are stopped. Reply START by SMS to re-enable them.</p>}
    </GlassCard>

    <div style={{ marginTop: 16 }}><GamificationCard onOpenLeaderboard={() => setLeaderboardOpen(true)} /></div>
    <RecoveryCompanion />
    <LiveMapModal isOpen={liveMapOpen} onClose={() => setLiveMapOpen(false)} />
    <LeaderboardModal isOpen={leaderboardOpen} onClose={() => setLeaderboardOpen(false)} />
  </main>;
}
