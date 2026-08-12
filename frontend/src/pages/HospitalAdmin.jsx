import React, { useState } from 'react';
import { Building2, Link as LinkIcon, Database, CheckCircle2, Plus, Lock, Ambulance, AlertTriangle, TrendingDown } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import LiveMap from '../components/LiveMap';

export default function HospitalAdmin() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [password, setPassword] = useState('');
  
  const [webhooks, setWebhooks] = useState([
    { id: 1, url: 'https://api.citygeneral.com/lifelink/webhooks', eventType: 'DONOR_MATCHED', status: 'ACTIVE' },
  ]);

  const [traumaAlerts, setTraumaAlerts] = useState([
    { id: 101, unit: 'Medic 42', type: 'MVA (Multi-Vehicle Accident)', eta: '8 mins', bloodNeeded: '4 Units O-', status: 'INBOUND' }
  ]);

  const handleLogin = (e) => {
    e.preventDefault();
    if (password === 'admin123') {
      setIsAuthenticated(true);
    } else {
      alert('Invalid admin password. Try "admin123".');
    }
  };

  const handleAddWebhook = (e) => {
    e.preventDefault();
    alert('Webhook added successfully!');
  };

  if (!isAuthenticated) {
    return (
      <div className="container animate-fade-in" style={{ padding: '4rem 1.5rem', display: 'flex', justifyContent: 'center' }}>
        <GlassCard style={{ width: '100%', maxWidth: '400px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <Lock size={48} color="var(--color-primary)" style={{ marginBottom: '1rem' }} />
          <h2 style={{ marginBottom: '0.5rem', margin: 0 }}>Admin Access</h2>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: '2rem', textAlign: 'center', fontSize: '0.875rem' }}>
            This area is restricted to authorized hospital administrators.
          </p>
          
          <form onSubmit={handleLogin} style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter Admin Password" 
              className="form-input" 
              style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} 
              autoFocus
            />
            <PrimaryButton type="submit" style={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
              Authenticate
            </PrimaryButton>
          </form>
        </GlassCard>
      </div>
    );
  }

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Building2 color="var(--color-primary)" />
            Hospital Admin Portal
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Manage your blood inventory and B2B integrations.</p>
        </div>
      </div>

      {/* Trauma Code Red Feed */}
      <GlassCard style={{ marginBottom: '2rem', borderLeft: '4px solid var(--color-primary)' }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '0 0 1rem 0', color: 'var(--color-primary)' }}>
          <Ambulance size={24} /> Field Paramedic Feed (Trauma Code Red)
        </h3>
        {traumaAlerts.map(alert => (
          <div key={alert.id} style={{ background: 'rgba(220, 38, 38, 0.1)', border: '1px solid var(--color-primary)', borderRadius: '8px', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', animation: 'pulse 2s infinite' }}>
            <div>
              <span style={{ fontWeight: 'bold', fontSize: '1.1rem', color: 'var(--color-primary)' }}>{alert.unit}: {alert.type}</span>
              <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.875rem' }}>ETA: <strong>{alert.eta}</strong> • Prepare: <strong>{alert.bloodNeeded}</strong></p>
            </div>
            <PrimaryButton onClick={() => setTraumaAlerts([])}>Acknowledge & Prep</PrimaryButton>
          </div>
        ))}
        {traumaAlerts.length === 0 && (
          <p style={{ margin: 0, color: 'var(--color-text-muted)' }}>No active inbound trauma alerts.</p>
        )}
      </GlassCard>

      <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
        <div style={{ flex: '1 1 400px', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <GlassCard style={{ flex: 1 }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', marginTop: 0 }}>
              <LinkIcon color="var(--color-primary)" /> Webhook Integrations
            </h3>
            <p style={{ color: 'var(--color-text-muted)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              Receive real-time HTTP POST payloads when events occur in the LifeLink network.
            </p>

            <form onSubmit={handleAddWebhook} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem', padding: '1rem', background: 'rgba(0,0,0,0.1)', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.75rem' }}>Payload URL</label>
                <input required type="url" placeholder="https://..." className="form-input" style={{ width: '100%', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--color-border)', background: 'transparent', color: 'white' }} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.75rem' }}>Event Type</label>
                <select className="form-input" style={{ width: '100%', padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--color-border)', background: 'transparent', color: 'white' }}>
                  <option value="REQUEST_CREATED">REQUEST_CREATED</option>
                  <option value="DONOR_MATCHED">DONOR_MATCHED</option>
                  <option value="DONOR_ACCEPTED">DONOR_ACCEPTED</option>
                </select>
              </div>
              <PrimaryButton type="submit" style={{ padding: '0.5rem', marginTop: '0.5rem' }}>
                <Plus size={16} /> Subscribe
              </PrimaryButton>
            </form>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <h4 style={{ margin: 0 }}>Active Subscriptions</h4>
              {webhooks.map(wh => (
                <div key={wh.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem', border: '1px solid var(--color-border)', borderRadius: '8px' }}>
                  <div style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold', fontSize: '0.875rem' }}>{wh.eventType}</p>
                    <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.75rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{wh.url}</p>
                  </div>
                  <CheckCircle2 size={18} color="var(--color-success)" />
                </div>
              ))}
            </div>
          </GlassCard>
        </div>

        <div style={{ flex: '1 1 400px' }}>
          <GlassCard style={{ height: '100%' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', marginTop: 0 }}>
              <Database color="var(--color-primary)" /> Blood Inventory
            </h3>
            <p style={{ color: 'var(--color-text-muted)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              Current real-time stock levels tracked via LifeLink.
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              {['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'].map((type, idx) => {
                const stock = Math.floor(Math.random() * 50);
                return (
                  <div key={type} style={{ padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid var(--color-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: 'bold', fontSize: '1.25rem', color: stock < 5 ? 'var(--color-primary)' : 'white' }}>{type}</span>
                    <span style={{ fontSize: '1.5rem', fontWeight: '300' }}>{stock} <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>units</span></span>
                  </div>
                );
              })}
            </div>
          </GlassCard>
          
          {/* Predictive Shortage AI */}
          <GlassCard style={{ flex: 1, borderTop: '4px solid var(--color-warning)' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', marginTop: 0, color: 'var(--color-warning)' }}>
              <TrendingDown size={20} /> Predictive Shortage AI
            </h3>
            <div style={{ background: 'rgba(245, 158, 11, 0.1)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-warning)' }}>
              <p style={{ margin: '0 0 0.5rem 0', fontWeight: 'bold' }}><AlertTriangle size={16} style={{ verticalAlign: 'middle', marginRight: '0.25rem' }} /> Critical Trajectory Detected</p>
              <p style={{ margin: '0 0 1rem 0', fontSize: '0.875rem' }}>
                Based on current usage rates and scheduled surgeries, O- inventory will be completely depleted within <strong>14 hours</strong>.
              </p>
              <PrimaryButton onClick={() => alert('Preemptive SOS Broadcast sent to donors within 10km!')} style={{ width: '100%', display: 'flex', justifyContent: 'center' }}>
                Auto-Broadcast Preemptive SOS
              </PrimaryButton>
            </div>
          </GlassCard>
        </div>
      </div>
      
      {/* Live Map Radar */}
      <GlassCard style={{ marginTop: '2rem', borderTop: '4px solid var(--color-primary)' }}>
        <h3 style={{ margin: '0 0 1rem 0', color: 'var(--color-primary)' }}>Live Map view of matching in action</h3>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', marginBottom: '1rem' }}>Tracking active emergency radius (5/15/30km) and live donor pins via SSE.</p>
        
        <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
          <input 
            type="text" 
            placeholder="Enter Request ID to track..." 
            id="trackingRequestId"
            className="form-input" 
            style={{ flex: 1, padding: '0.5rem', borderRadius: '4px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} 
          />
          <PrimaryButton onClick={() => {
            const reqId = document.getElementById('trackingRequestId').value;
            if(!reqId) return;
            const eventSource = new EventSource(`http://localhost:8080/api/v1/requests/${reqId}/stream`);
            eventSource.addEventListener("EVENT", (e) => {
              console.log("SSE Event:", e.data);
              // For demonstration purposes, we parse and log it
            });
            alert('Subscribed to SSE stream for Request ' + reqId);
          }}>
            Start Tracking
          </PrimaryButton>
        </div>

        <LiveMap 
            center={[12.9716, 77.5946]} 
            emergencyTitle="Active Trauma Code Red" 
            donors={[
                { id: '1', name: 'Rajesh K. (Matched)', lat: 12.9720, lng: 77.5950, status: 'Notified' },
                { id: '2', name: 'Priya M. (Accepted)', lat: 12.9690, lng: 77.5920, status: 'Accepted' },
                { id: '3', name: 'Amit (Declined)', lat: 12.9800, lng: 77.6000, status: 'Declined' }
            ]} 
        />
      </GlassCard>
    </div>
  );
}
