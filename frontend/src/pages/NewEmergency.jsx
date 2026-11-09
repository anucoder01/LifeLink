import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function NewEmergency() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ bloodType: 'O-', componentType: 'WHOLE_BLOOD', urgency: 'CRITICAL', unitsRequired: 1, facilityName: '', verificationReference: '' });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [created, setCreated] = useState(null);
  const change = event => setForm({ ...form, [event.target.name]: event.target.value });

  const submit = async event => {
    event.preventDefault();
    if (!navigator.geolocation) { setError('Location is required to match nearby donors.'); return; }
    setBusy(true); setError('');
    navigator.geolocation.getCurrentPosition(async ({ coords }) => {
      try {
        const { data } = await api.post('/requests', { ...form, unitsRequired: Number(form.unitsRequired), latitude: coords.latitude, longitude: coords.longitude });
        setCreated(data);
      } catch (err) {
        setError(err.response?.data?.message || 'Request could not be submitted. Sign in and try again.');
      } finally { setBusy(false); }
    }, () => { setBusy(false); setError('Location permission is needed to submit a nearby donor request.'); }, { enableHighAccuracy: true, timeout: 15000 });
  };

  if (created) return <main className="container" style={{ padding: '2rem 1.5rem' }}>
    <GlassCard><h2>Request submitted</h2><p>Request ID: {created.id}</p>
      <p>The request is awaiting authorized verification. Donor matching starts after approval.</p>
      <PrimaryButton onClick={() => navigate('/my-requests')}>View my requests</PrimaryButton>
    </GlassCard>
  </main>;

  return <main className="container" style={{ padding: '2rem 1.5rem', maxWidth: 680 }}>
    <h2>Request blood for an emergency</h2>
    <p>Requests are checked by an authorized coordinator before donors are contacted.</p>
    <GlassCard><form onSubmit={submit} style={{ display: 'grid', gap: 16 }}>
      <label>Blood type<select name="bloodType" value={form.bloodType} onChange={change}>{['A+','A-','B+','B-','AB+','AB-','O+','O-'].map(type => <option key={type}>{type}</option>)}</select></label>
      <label>Component<select name="componentType" value={form.componentType} onChange={change}>{['WHOLE_BLOOD','PLATELETS','PLASMA'].map(type => <option key={type}>{type}</option>)}</select></label>
      <label>Units required<input name="unitsRequired" type="number" min="1" max="10" value={form.unitsRequired} onChange={change} required /></label>
      <label>Urgency<select name="urgency" value={form.urgency} onChange={change}>{['CRITICAL','HIGH','MEDIUM'].map(level => <option key={level}>{level}</option>)}</select></label>
      <label>Hospital or care facility<input name="facilityName" maxLength="150" value={form.facilityName} onChange={change} required /></label>
      <label>Verification contact or reference (optional)<input name="verificationReference" maxLength="500" value={form.verificationReference} onChange={change} /></label>
      {error && <p role="alert">{error}</p>}
      <PrimaryButton disabled={busy} type="submit">{busy ? 'Submitting…' : 'Submit for verification'}</PrimaryButton>
    </form></GlassCard>
  </main>;
}
