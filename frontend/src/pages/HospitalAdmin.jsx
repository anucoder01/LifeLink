import React, { useState } from 'react';
import { Building2, Link as LinkIcon, Database, CheckCircle2, Plus } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function HospitalAdmin() {
  const [webhooks, setWebhooks] = useState([
    { id: 1, url: 'https://api.citygeneral.com/lifelink/webhooks', eventType: 'DONOR_MATCHED', status: 'ACTIVE' },
  ]);

  const handleAddWebhook = (e) => {
    e.preventDefault();
    alert('Webhook added successfully!');
  };

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

      <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
        <div style={{ flex: '1 1 400px' }}>
          <GlassCard style={{ height: '100%' }}>
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
        </div>
      </div>
    </div>
  );
}
