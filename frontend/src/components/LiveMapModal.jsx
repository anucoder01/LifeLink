import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import GoogleMapView from './GoogleMapView';

export default function LiveMapModal({ isOpen, onClose, request }) {
  const [requestId, setRequestId] = useState(request?.id || '');
  const [locations, setLocations] = useState([]);
  const [error, setError] = useState('');
  useEffect(() => { setRequestId(request?.id || ''); }, [request?.id]);
  useEffect(() => {
    if (!isOpen || !requestId) return undefined;
    let stopped = false;
    const poll = async () => {
      try {
        const { data } = await api.get(`/requests/${encodeURIComponent(requestId)}/live-locations`);
        if (!stopped) { setLocations(data || []); setError(''); }
      } catch {
        if (!stopped) setError('Live locations are available to the request owner only.');
      }
    };
    poll();
    const timer = setInterval(poll, 5000);
    return () => { stopped = true; clearInterval(timer); };
  }, [isOpen, requestId]);

  if (!isOpen) return null;
  const center = locations.length ? [locations[0].latitude, locations[0].longitude] : null;
  const markers = locations.map(item => ({
    position: [item.latitude, item.longitude],
    title: 'Matched donor location',
    label: `Matched donor location · Updated ${new Date(item.timestamp).toLocaleTimeString()}`,
    color: '#2563eb'
  }));
  return <div style={overlayStyle}>
    <section className="glass-card" role="dialog" aria-modal="true" aria-label="Live donor tracking" style={modalStyle}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3>Live donor locations</h3><button onClick={onClose} aria-label="Close">×</button>
      </header>
      <label>Request ID <input value={requestId} onChange={e => setRequestId(e.target.value)} placeholder="Paste your request ID" /></label>
      {error && <p role="status">{error}</p>}
      {!error && requestId && !locations.length && <p>Waiting for a donor to share their live location…</p>}
      {center && <div style={{ height: 360, marginTop: 12 }}><GoogleMapView center={center} zoom={13} markers={markers} /></div>}
    </section>
  </div>;
}

const overlayStyle = { position: 'fixed', inset: 0, background: 'rgba(0,0,0,.8)', display: 'grid', placeItems: 'center', zIndex: 1100, padding: 16 };
const modalStyle = { width: '100%', maxWidth: 800, padding: 24, color: '#fff' };
