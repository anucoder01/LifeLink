import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

export default function GamificationCard({ onOpenLeaderboard }) {
  const [profile, setProfile] = useState(null);
  const [certificate, setCertificate] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/donors/me/gamification')
      .then(({ data }) => setProfile(data))
      .catch(() => setError('Donor impact data is unavailable.'));
  }, []);

  const loadCertificate = async () => {
    try {
      const { data } = await api.get('/donors/me/certificate');
      setCertificate(data);
      setError('');
    } catch {
      setError('Certificate could not be loaded.');
    }
  };

  const progress = profile ? Math.min(100, Math.round(((profile.level * 250 - profile.xpToNextLevel) / 250) * 100)) : 0;
  return <section className="glass-card" style={{ padding: 18, marginBottom: 20 }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
      <div>
        <h3 style={{ marginTop: 0 }}>Donor impact</h3>
        <p>{profile ? `Level ${profile.level} · ${profile.totalDonations} recorded donations · ${profile.xp} XP` : 'Loading donor impact…'}</p>
      </div>
      <button onClick={onOpenLeaderboard}>Leaderboard</button>
    </div>
    <progress max="100" value={progress} style={{ width: '100%' }} />
    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, margin: '12px 0' }}>
      {profile?.badges?.map(badge => <span key={badge.key} title={badge.name}>{badge.icon} {badge.name}</span>)}
    </div>
    <button onClick={loadCertificate} disabled={!profile}>View donation certificate</button>
    {error && <p role="status">{error}</p>}
    {certificate && <div role="dialog" aria-label="Donation certificate">
      <button onClick={() => setCertificate(null)}>Close</button>
      <h4>Donation certificate</h4>
      <p>{certificate.donorName} · {certificate.bloodType} · {certificate.totalDonations} recorded donations</p>
      <p>Certificate {certificate.certificateId} · Issued {certificate.issuedDate}</p>
      <code>{certificate.verificationHash}</code>
    </div>}
  </section>;
}
