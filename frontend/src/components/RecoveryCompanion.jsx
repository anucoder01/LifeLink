import React, { useEffect, useState } from 'react';
import { api } from '../services/api';

export default function RecoveryCompanion() {
  const [status, setStatus] = useState(null);
  const [waterMl, setWaterMl] = useState(250);
  const [feeling, setFeeling] = useState('GOOD');
  const [error, setError] = useState('');

  const refresh = () => api.get('/donors/me/recovery')
    .then(({ data }) => { setStatus(data); setError(''); })
    .catch(() => setError('Recovery check-in is unavailable. Sign in as a donor and try again.'));
  useEffect(() => { refresh(); }, []);

  const logCheckIn = async () => {
    if (!status) return;
    const today = status.timeline?.find(day => day.dayNumber === status.currentDay);
    try {
      const { data } = await api.post('/donors/me/recovery/log', {
        dayNumber: status.currentDay,
        waterMlLogged: (today?.loggedWaterMl || 0) + waterMl,
        feelingStatus: feeling
      });
      setStatus(data);
      setError('');
    } catch {
      setError('Could not save your check-in. Please retry.');
    }
  };

  const currentDay = status?.timeline?.find(day => day.dayNumber === status.currentDay);
  return <section className="glass-card" style={{ padding: 18, marginBottom: 20 }}>
    <h3>Post-donation check-in</h3>
    <p>This is a self-reported wellness log, not medical advice. Follow the instructions from your donation center.</p>
    {status && <>
      <p>Day {status.currentDay} of 7 · Logged fluids: {status.totalHydrationMl} ml</p>
      <label>Fluids to add (ml) <input type="number" min="0" max="5000" step="50" value={waterMl} onChange={e => setWaterMl(Number(e.target.value))} /></label>
      <label style={{ marginLeft: 12 }}>How are you feeling? <select value={feeling} onChange={e => setFeeling(e.target.value)}>
        <option value="EXCELLENT">Well</option><option value="GOOD">Okay</option><option value="TIRED">Tired</option><option value="DIZZY">Dizzy</option><option value="COMPLICATION">Other concern</option>
      </select></label>
      <button onClick={logCheckIn} style={{ marginLeft: 12 }}>Save check-in</button>
      {currentDay && <p>{currentDay.title}: {currentDay.nutritionAdvice}</p>}
      {currentDay?.safetyAlert && <p role="alert">{currentDay.safetyAlert}</p>}
    </>}
    {error && <p role="status">{error}</p>}
  </section>;
}
