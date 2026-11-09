import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [responses, setResponses] = useState({});
  const [error, setError] = useState('');
  const refresh = async () => {
    try {
      const { data } = await api.get('/requests?size=50&sort=createdAt,desc');
      const items = data.content || [];
      setRequests(items);
      const entries = await Promise.all(items.map(async request => {
        if (!request.verified) return [request.id, []];
        try { const response = await api.get(`/requests/${request.id}/responses?size=100`); return [request.id, response.data.content || []]; }
        catch { return [request.id, []]; }
      }));
      setResponses(Object.fromEntries(entries));
      setError('');
    } catch { setError('Could not load your requests. Please sign in and retry.'); }
  };
  useEffect(() => { refresh(); }, []);

  const action = async (request, name) => {
    try {
      if (name === 'cancel') await api.put(`/requests/${request.id}/cancel`);
      if (name === 'fulfill') await api.put(`/requests/${request.id}/fulfill`);
      if (name === 'driver') await api.post(`/requests/${request.id}/request-driver`);
      await refresh();
    } catch { setError('That action could not be completed. Refresh and try again.'); }
  };

  return <main className="container" style={{ padding: '2rem 1.5rem' }}>
    <h2>My emergency requests</h2>
    <p>Requests appear here with their verification and donor response status.</p>
    {error && <p role="alert">{error}</p>}
    {!error && !requests.length && <p>No emergency requests yet.</p>}
    <div style={{ display: 'grid', gap: 16 }}>{requests.map(request => <GlassCard key={request.id}>
      <h3>{request.bloodType} · {request.unitsRequired} unit(s)</h3>
      <p>{request.facilityName || 'Care facility'} · {request.urgency} · {request.status}</p>
      <p>{request.verified ? 'Verified; donor matching is active.' : 'Awaiting verification; donors have not been contacted.'}</p>
      {request.verified && <section><h4>Donor responses</h4>{(responses[request.id] || []).length
        ? responses[request.id].map(response => <p key={response.id}>{response.donorName} · {response.status}{response.donorPhone ? ` · ${response.donorPhone}` : ''}</p>)
        : <p>No donor responses yet.</p>}</section>}
      {['PENDING', 'IN_PROGRESS'].includes(request.status) && <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <PrimaryButton onClick={() => action(request, 'cancel')}>Cancel request</PrimaryButton>
        <PrimaryButton onClick={() => action(request, 'fulfill')}>Mark fulfilled</PrimaryButton>
        {request.verified && <PrimaryButton onClick={() => action(request, 'driver')}>Request a driver</PrimaryButton>}
      </div>}
    </GlassCard>)}</div>
  </main>;
}
