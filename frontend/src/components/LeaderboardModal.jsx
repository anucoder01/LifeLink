import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

export default function LeaderboardModal({ isOpen, onClose }) {
  const [leaderboard, setLeaderboard] = useState([]);
  const [error, setError] = useState('');
  useEffect(() => {
    if (!isOpen) return;
    api.get('/donors/leaderboard')
      .then(({ data }) => { setLeaderboard(data || []); setError(''); })
      .catch(() => setError('Leaderboard is currently unavailable.'));
  }, [isOpen]);

  if (!isOpen) return null;
  return <div className="modal-overlay" style={overlayStyle}>
    <section className="glass-card" role="dialog" aria-modal="true" aria-label="Donor leaderboard" style={modalStyle}>
      <header style={{ display: 'flex', justifyContent: 'space-between' }}>
        <h3>Donor leaderboard</h3><button onClick={onClose} aria-label="Close">×</button>
      </header>
      {error && <p role="status">{error}</p>}
      {!error && !leaderboard.length && <p>No donor impact has been recorded yet.</p>}
      {!!leaderboard.length && <div style={{ overflowX: 'auto' }}><table style={{ width: '100%', textAlign: 'left' }}>
        <thead><tr><th>Rank</th><th>Donor</th><th>Blood type</th><th>Level</th><th>Donations</th></tr></thead>
        <tbody>{leaderboard.map(row => <tr key={`${row.rank}-${row.donorName}`}>
          <td>{row.rank}</td><td>{row.donorName}</td><td>{row.bloodType}</td><td>{row.level}</td><td>{row.totalDonations}</td>
        </tr>)}</tbody>
      </table></div>}
    </section>
  </div>;
}

const overlayStyle = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,.75)', display: 'grid', placeItems: 'center', zIndex: 1200, padding: 16 };
const modalStyle = { width: '100%', maxWidth: 650, maxHeight: '85vh', overflow: 'auto', padding: 24 };
