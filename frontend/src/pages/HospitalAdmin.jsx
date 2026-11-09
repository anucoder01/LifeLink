import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function HospitalAdmin() {
  const [hospitals, setHospitals] = useState([]);
  const [hospitalId, setHospitalId] = useState('');
  const [lots, setLots] = useState([]);
  const [incomingHandoffs, setIncomingHandoffs] = useState([]);
  const [stockStatus, setStockStatus] = useState([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ bloodType: 'O-', componentType: 'WHOLE_BLOOD', unitCount: 1, collectedOn: '', expiresOn: '' });
  const [temperature, setTemperature] = useState({ temperatureC: '', withinValidatedRange: true, note: '' });
  const [destinationHospitalId, setDestinationHospitalId] = useState('');
  const [handoffCount, setHandoffCount] = useState(1);

  useEffect(() => {
    api.get('/hospitals?size=100').then(({ data }) => {
      const items = data.content || [];
      setHospitals(items);
      if (items.length) setHospitalId(items[0].id);
    }).catch(() => setError('Sign in with a hospital administrator account to load hospitals.'));
  }, []);

  const loadLots = async id => {
    if (!id) return;
    try {
      const { data } = await api.get(`/hospitals/${id}/lots`);
      setLots(data);
      const incoming = await api.get(`/hospitals/${id}/lots/handoffs/incoming`);
      setIncomingHandoffs(incoming.data || []);
      const types = [...new Map(data.map(lot => [`${lot.bloodType}:${lot.componentType}`, lot])).values()];
      const statuses = await Promise.all(types.map(async lot => {
        const params = new URLSearchParams({ bloodType: lot.bloodType, componentType: lot.componentType });
        const result = await api.get(`/hospitals/inventory/availability/status?${params}`);
        return { key: `${lot.bloodType}:${lot.componentType}`, ...result.data };
      }));
      setStockStatus(statuses);
      setError('');
    }
    catch { setError('Blood lot access is restricted to hospital administrators.'); }
  };
  useEffect(() => { loadLots(hospitalId); }, [hospitalId]);

  const createLot = async event => {
    event.preventDefault();
    try {
      await api.post(`/hospitals/${hospitalId}/lots`, { ...form, unitCount: Number(form.unitCount) });
      await loadLots(hospitalId);
    } catch { setError('Could not save the lot. Check dates, fields, and your hospital role.'); }
  };
  const recordTemperature = async lot => {
    try {
      await api.post(`/hospitals/${hospitalId}/lots/${lot.id}/temperature`, { ...temperature, temperatureC: Number(temperature.temperatureC) });
      await loadLots(hospitalId);
    } catch { setError('Temperature record could not be saved.'); }
  };
  const receiveHandoff = async handoff => {
    try {
      await api.post(`/hospitals/${hospitalId}/lots/handoffs/${handoff.id}/receive`);
      await loadLots(hospitalId);
    } catch { setError('Could not confirm receipt. Verify that this hospital is the assigned destination.'); }
  };
  const sendHandoff = async lot => {
    if (!destinationHospitalId) { setError('Choose the destination hospital first.'); return; }
    try {
      await api.post(`/hospitals/${hospitalId}/lots/${lot.id}/handoffs`, {
        toHospitalId: destinationHospitalId, unitCount: Number(handoffCount), reference: ''
      });
      await loadLots(hospitalId);
    } catch { setError('Could not start the handoff. Check the destination and available lot units.'); }
  };

  return <main className="container" style={{ padding: '2rem 1.5rem' }}>
    <h2>Hospital blood inventory</h2>
    <p>Track stock by collection lot, expiry date, and recorded cold-chain checks.</p>
    <label>Hospital <select value={hospitalId} onChange={event => setHospitalId(event.target.value)}>
      {hospitals.map(hospital => <option key={hospital.id} value={hospital.id}>{hospital.name}</option>)}
    </select></label>
    {error && <p role="alert">{error}</p>}
    <GlassCard style={{ margin: '16px 0' }}><h3>Add blood unit lot</h3>
      <form onSubmit={createLot} style={{ display: 'grid', gap: 12, gridTemplateColumns: 'repeat(auto-fit,minmax(150px,1fr))' }}>
        <label>Blood type<select value={form.bloodType} onChange={e => setForm({ ...form, bloodType: e.target.value })}>{['A+','A-','B+','B-','AB+','AB-','O+','O-'].map(x => <option key={x}>{x}</option>)}</select></label>
        <label>Component<select value={form.componentType} onChange={e => setForm({ ...form, componentType: e.target.value })}>{['WHOLE_BLOOD','PLATELETS','PLASMA'].map(x => <option key={x}>{x}</option>)}</select></label>
        <label>Units<input type="number" min="1" value={form.unitCount} onChange={e => setForm({ ...form, unitCount: e.target.value })} /></label>
        <label>Collected on<input required type="date" value={form.collectedOn} onChange={e => setForm({ ...form, collectedOn: e.target.value })} /></label>
        <label>Expires on<input required type="date" value={form.expiresOn} onChange={e => setForm({ ...form, expiresOn: e.target.value })} /></label>
        <PrimaryButton type="submit">Save lot</PrimaryButton>
      </form>
    </GlassCard>
    <h3>Tracked lots</h3>
    {!!stockStatus.length && <GlassCard><h4>Network stock freshness</h4>{stockStatus.map(item => <p key={item.key}>
      {item.key}: {item.totalUnits} reported units across {item.reportingHospitals} hospitals ·
      {item.stale ? ' needs reconfirmation' : ` last confirmed ${item.lastConfirmedAt ? new Date(item.lastConfirmedAt).toLocaleString() : 'unknown'}`}
    </p>)}</GlassCard>}
    {!!incomingHandoffs.length && <GlassCard><h3>Incoming blood-unit handoffs</h3>{incomingHandoffs.map(handoff => <div key={handoff.id}>
      <p>{handoff.unitCount} unit(s) · From hospital {handoff.fromHospitalId} · Reference: {handoff.reference || 'none'}</p>
      <PrimaryButton onClick={() => receiveHandoff(handoff)}>Confirm receipt</PrimaryButton>
    </div>)}</GlassCard>}
    {!lots.length && <p>No lots recorded for this hospital.</p>}
    <div style={{ display: 'grid', gap: 12 }}>{lots.map(lot => <GlassCard key={lot.id}>
      <strong>{lot.bloodType} {lot.componentType} · {lot.unitCount} unit(s)</strong>
      <p>Collected {lot.collectedOn} · Expires {lot.expiresOn} · {lot.status}</p>
      {lot.status === 'AVAILABLE' && <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <input aria-label="Temperature in Celsius" type="number" step="0.1" placeholder="°C" value={temperature.temperatureC} onChange={e => setTemperature({ ...temperature, temperatureC: e.target.value })} />
        <label><input type="checkbox" checked={temperature.withinValidatedRange} onChange={e => setTemperature({ ...temperature, withinValidatedRange: e.target.checked })} /> Within validated storage range</label>
        <input placeholder="Check note" value={temperature.note} onChange={e => setTemperature({ ...temperature, note: e.target.value })} />
        <PrimaryButton disabled={temperature.temperatureC === ''} onClick={() => recordTemperature(lot)}>Record temperature</PrimaryButton>
        <select aria-label="Destination hospital" value={destinationHospitalId} onChange={e => setDestinationHospitalId(e.target.value)}>
          <option value="">Choose destination</option>{hospitals.filter(item => item.id !== hospitalId && item.verified).map(item => <option key={item.id} value={item.id}>{item.name}</option>)}
        </select>
        <input aria-label="Units to hand off" type="number" min="1" max={lot.unitCount} value={handoffCount} onChange={e => setHandoffCount(e.target.value)} />
        <PrimaryButton disabled={!destinationHospitalId || handoffCount > lot.unitCount} onClick={() => sendHandoff(lot)}>Send handoff</PrimaryButton>
      </div>}
    </GlassCard>)}</div>
  </main>;
}
