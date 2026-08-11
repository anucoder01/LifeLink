import React, { useState } from 'react';
import { HeartHandshake, TrendingUp, Users, Target, FileText } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function NGOAdmin() {
  const [campaigns] = useState([
    { id: 1, name: 'Summer Blood Drive 2026', totalDonors: 1450, impact: '4350 Lives Saved', status: 'ACTIVE' },
    { id: 2, name: 'Corporate Plasma Campaign', totalDonors: 320, impact: '960 Lives Saved', status: 'COMPLETED' },
  ]);

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <HeartHandshake color="var(--color-primary)" />
            NGO Partner Dashboard
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Manage your campaigns and measure your life-saving impact.</p>
        </div>
        <PrimaryButton style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Target size={18} /> Launch Campaign
        </PrimaryButton>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        <GlassCard style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(220, 38, 38, 0.1)', borderRadius: '12px', color: 'var(--color-primary)' }}>
            <Users size={24} />
          </div>
          <div>
            <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Total Donors Mobilized</p>
            <h3 style={{ margin: 0, fontSize: '1.75rem' }}>1,770</h3>
          </div>
        </GlassCard>
        
        <GlassCard style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ padding: '1rem', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '12px', color: '#10b981' }}>
            <TrendingUp size={24} />
          </div>
          <div>
            <p style={{ margin: 0, color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>Total Lives Impacted</p>
            <h3 style={{ margin: 0, fontSize: '1.75rem' }}>5,310</h3>
          </div>
        </GlassCard>
      </div>

      <GlassCard>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', marginTop: 0 }}>
          <FileText color="var(--color-primary)" size={20} /> My Campaigns
        </h3>
        
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
                <th style={{ padding: '1rem 0', color: 'var(--color-text-muted)', fontWeight: '500' }}>Campaign Name</th>
                <th style={{ padding: '1rem 0', color: 'var(--color-text-muted)', fontWeight: '500' }}>Status</th>
                <th style={{ padding: '1rem 0', color: 'var(--color-text-muted)', fontWeight: '500' }}>Donors</th>
                <th style={{ padding: '1rem 0', color: 'var(--color-text-muted)', fontWeight: '500' }}>Est. Impact</th>
                <th style={{ padding: '1rem 0', color: 'var(--color-text-muted)', fontWeight: '500' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map(c => (
                <tr key={c.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                  <td style={{ padding: '1rem 0', fontWeight: '500' }}>{c.name}</td>
                  <td style={{ padding: '1rem 0' }}>
                    <span style={{ 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '4px', 
                      fontSize: '0.75rem', 
                      fontWeight: 'bold',
                      background: c.status === 'ACTIVE' ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255,255,255,0.1)',
                      color: c.status === 'ACTIVE' ? '#10b981' : 'var(--color-text-muted)'
                    }}>
                      {c.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem 0' }}>{c.totalDonors}</td>
                  <td style={{ padding: '1rem 0', color: 'var(--color-primary)' }}>{c.impact}</td>
                  <td style={{ padding: '1rem 0' }}>
                    <button style={{ background: 'none', border: '1px solid var(--color-border)', color: 'white', padding: '0.25rem 0.75rem', borderRadius: '4px', cursor: 'pointer' }}>View Report</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </GlassCard>
    </div>
  );
}
