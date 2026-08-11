import React, { useState } from 'react';
import { Activity, XCircle, CheckCircle2, Car, Users, Phone } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function MyRequests() {
  const [requests, setRequests] = useState([
    {
      id: 1,
      bloodType: 'A+',
      status: 'IN_PROGRESS',
      createdAt: '10 mins ago',
      donors: [
        { id: 101, name: 'Alice Smith', status: 'ACCEPTED', phone: '+1234567890' },
        { id: 102, name: 'Bob Jones', status: 'EN_ROUTE', phone: '+1987654321' }
      ]
    }
  ]);

  const handleAction = (reqId, action) => {
    alert(`Action: ${action} triggered for request ${reqId}`);
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Activity color="var(--color-primary)" />
            My SOS Requests
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Manage your active emergency requests.</p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {requests.map(req => (
          <GlassCard key={req.id} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--color-border)', paddingBottom: '1rem' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.5rem' }}>{req.bloodType} Required</h3>
                <p style={{ color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0' }}>Created {req.createdAt} • Status: <span style={{ color: 'var(--color-warning)' }}>{req.status}</span></p>
              </div>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <PrimaryButton variant="danger" onClick={() => handleAction(req.id, 'CANCEL')} style={{ padding: '0.5rem 1rem' }}>
                  <XCircle size={18} /> Cancel
                </PrimaryButton>
                <PrimaryButton variant="primary" onClick={() => handleAction(req.id, 'FULFILL')} style={{ padding: '0.5rem 1rem', background: 'var(--color-success)', boxShadow: '0 4px 14px rgba(16, 185, 129, 0.4)' }}>
                  <CheckCircle2 size={18} /> Mark Fulfilled
                </PrimaryButton>
              </div>
            </div>

            <div>
              <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                <Users size={20} color="var(--color-primary)" /> Live Donor Responses
              </h4>
              {req.donors.length === 0 ? (
                <p style={{ color: 'var(--color-text-muted)' }}>Waiting for donors to respond...</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {req.donors.map(donor => (
                    <div key={donor.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255,255,255,0.02)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
                      <div>
                        <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold' }}>{donor.name}</p>
                        <p style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>
                          <Phone size={14} /> {donor.phone}
                        </p>
                      </div>
                      <div>
                        <span style={{ 
                          padding: '0.25rem 0.75rem', 
                          borderRadius: '99px', 
                          fontSize: '0.75rem', 
                          fontWeight: '600',
                          background: donor.status === 'EN_ROUTE' ? 'rgba(59, 130, 246, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                          color: donor.status === 'EN_ROUTE' ? '#3b82f6' : '#10b981'
                        }}>
                          {donor.status}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <h4 style={{ margin: '0 0 0.25rem 0' }}>Need Transportation Logistics?</h4>
                <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Request a volunteer driver to transport the donor.</p>
              </div>
              <PrimaryButton variant="secondary" onClick={() => handleAction(req.id, 'REQUEST_DRIVER')} style={{ padding: '0.5rem 1rem' }}>
                <Car size={18} /> Request Driver
              </PrimaryButton>
            </div>
          </GlassCard>
        ))}
      </div>
    </div>
  );
}
