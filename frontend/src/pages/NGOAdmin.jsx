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

      <GlassCard style={{ marginBottom: '2rem', background: 'linear-gradient(135deg, rgba(37,99,235,0.1) 0%, rgba(139,92,246,0.1) 100%)', border: '1px solid rgba(139,92,246,0.3)' }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', marginTop: 0 }}>
          <TrendingUp color="#8b5cf6" size={20} /> Request vs Fulfillment Metrics
        </h3>
        
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: '1rem', height: '200px', padding: '1rem 0', borderBottom: '1px solid var(--color-border)' }}>
          {/* Mock Bar Chart */}
          {[
            { month: 'Jan', req: 120, ful: 100 },
            { month: 'Feb', req: 150, ful: 130 },
            { month: 'Mar', req: 200, ful: 160 },
            { month: 'Apr', req: 180, ful: 175 },
            { month: 'May', req: 250, ful: 240 },
          ].map((data, idx) => (
            <div key={data.month} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem', height: '100%', justifyContent: 'flex-end' }}>
              <div style={{ display: 'flex', gap: '0.25rem', alignItems: 'flex-end', height: '100%', width: '60%' }}>
                <div style={{ 
                  width: '50%', 
                  height: `${(data.req / 250) * 100}%`, 
                  background: 'var(--color-warning)',
                  borderRadius: '4px 4px 0 0',
                  animation: `slideUp 1s ease-out ${idx * 0.1}s backwards`
                }} title={`Requests: ${data.req}`} />
                <div style={{ 
                  width: '50%', 
                  height: `${(data.ful / 250) * 100}%`, 
                  background: 'var(--color-success)',
                  borderRadius: '4px 4px 0 0',
                  animation: `slideUp 1s ease-out ${(idx * 0.1) + 0.2}s backwards`
                }} title={`Fulfillments: ${data.ful}`} />
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>{data.month}</span>
            </div>
          ))}
        </div>
        <div style={{ display: 'flex', gap: '2rem', justifyContent: 'center', marginTop: '1rem', fontSize: '0.875rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{ width: '12px', height: '12px', background: 'var(--color-warning)', borderRadius: '2px' }} /> Total Requests
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{ width: '12px', height: '12px', background: 'var(--color-success)', borderRadius: '2px' }} /> Fulfillments
          </div>
        </div>
      </GlassCard>

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
              {campaigns.map((c, i) => (
                <tr key={c.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)', animation: `fadeIn 0.5s ease-out ${i * 0.1}s backwards` }}>
                  <td style={{ padding: '1rem 0', fontWeight: '500' }}>{c.name}</td>
                  <td style={{ padding: '1rem 0' }}>
                    <span style={{ 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '4px', 
                      fontSize: '0.75rem', 
                      fontWeight: 'bold',
                      background: c.status === 'ACTIVE' ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255,255,255,0.1)',
                      color: c.status === 'ACTIVE' ? '#10b981' : 'var(--color-text-muted)',
                      boxShadow: c.status === 'ACTIVE' ? '0 0 10px rgba(16, 185, 129, 0.4)' : 'none'
                    }}>
                      {c.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem 0' }}>{c.totalDonors}</td>
                  <td style={{ padding: '1rem 0', color: 'var(--color-primary)' }}>{c.impact}</td>
                  <td style={{ padding: '1rem 0' }}>
                    <button style={{ background: 'var(--color-primary)', border: 'none', color: 'white', padding: '0.4rem 1rem', borderRadius: '4px', cursor: 'pointer', transition: 'all 0.2s', fontWeight: 'bold' }}>View Report</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </GlassCard>
      
      <style>{`
        @keyframes slideUp {
          from { height: 0; opacity: 0; }
          to { opacity: 1; }
        }
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
}
