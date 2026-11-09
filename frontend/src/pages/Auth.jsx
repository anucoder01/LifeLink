import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import PhoneInput from 'react-phone-number-input';
import 'react-phone-number-input/style.css';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Auth() {
  const location = useLocation();
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(location.state?.isLogin ?? true);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [bloodType, setBloodType] = useState('O+');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const saveSession = response => {
    localStorage.setItem('token', response.token);
    localStorage.setItem('refreshToken', response.refreshToken);
    localStorage.setItem('donorName', name || phone);
    navigate('/dashboard');
  };

  const submit = async event => {
    event.preventDefault(); setBusy(true); setError('');
    try {
      if (isLogin) {
        const { data } = await api.post('/auth/login', { phone, password });
        saveSession(data);
      } else {
        let position = null;
        if (navigator.geolocation) {
          try { position = await new Promise(resolve => navigator.geolocation.getCurrentPosition(resolve, () => resolve(null), { timeout: 8000 })); }
          catch { position = null; }
        }
        await api.post('/auth/register', {
          name, phone, password, role: 'DONOR', bloodType,
          latitude: position?.coords.latitude ?? null,
          longitude: position?.coords.longitude ?? null
        });
        const { data } = await api.post('/auth/login', { phone, password });
        saveSession(data);
      }
    } catch (err) {
      const message = err.response?.data;
      setError(typeof message === 'string' ? message : message?.message || 'Could not sign in. Check your details and try again.');
    } finally { setBusy(false); }
  };

  return <main className="container" style={{ padding: '4rem 1.5rem', maxWidth: 500 }}>
    <h2>{isLogin ? 'Sign in to LifeLink' : 'Register as a donor'}</h2>
    <p>{isLogin ? 'Use your registered phone number and password.' : 'Create a donor account. Location sharing is optional and can be updated later.'}</p>
    <GlassCard><form onSubmit={submit} style={{ display: 'grid', gap: 16 }}>
      {!isLogin && <>
        <label>Full name<input required maxLength="100" value={name} onChange={e => setName(e.target.value)} /></label>
        <label>Blood type<select value={bloodType} onChange={e => setBloodType(e.target.value)}>{['A+','A-','B+','B-','AB+','AB-','O+','O-'].map(type => <option key={type}>{type}</option>)}</select></label>
      </>}
      <label>Phone number<PhoneInput international defaultCountry="IN" value={phone} onChange={value => setPhone(value || '')} required /></label>
      <label>Password<input required type="password" minLength="8" value={password} onChange={e => setPassword(e.target.value)} autoComplete={isLogin ? 'current-password' : 'new-password'} /></label>
      {error && <p role="alert">{error}</p>}
      <PrimaryButton disabled={busy}>{busy ? 'Please wait…' : isLogin ? 'Sign in' : 'Create account'}</PrimaryButton>
    </form></GlassCard>
    <button type="button" onClick={() => { setError(''); setIsLogin(!isLogin); }} style={{ marginTop: 16 }}>
      {isLogin ? 'Create a donor account' : 'I already have an account'}
    </button>
  </main>;
}
