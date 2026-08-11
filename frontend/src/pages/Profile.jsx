import React, { useState } from 'react';
import { User, Activity, FileText, Settings, Save } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Profile() {
  const [formData, setFormData] = useState({
    allergies: 'Penicillin',
    medications: 'None',
    surgeries: 'None',
    bloodType: 'O+',
    weight: '75',
    lastDonation: '2026-06-15'
  });

  const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

  const handleSave = (e) => {
    e.preventDefault();
    alert('Medical Profile Updated Successfully!\nThis data is encrypted and only shared with hospitals during an emergency match.');
  };

  const donorName = localStorage.getItem('donorName') || 'John Doe';

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <User color="var(--color-primary)" />
            Medical Profile & Settings
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Manage your health data for accurate emergency matching.</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem' }}>
        
        {/* Left Column - Quick Stats */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <GlassCard style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
            <div style={{ width: '100px', height: '100px', borderRadius: '50%', background: 'var(--color-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '3rem', fontWeight: 'bold', marginBottom: '1rem' }}>
              {formData.bloodType}
            </div>
            <h3 style={{ margin: '0 0 0.25rem 0' }}>{donorName}</h3>
            <p style={{ margin: 0, color: 'var(--color-success)', fontWeight: 'bold' }}>Verified Donor</p>
          </GlassCard>

          <GlassCard>
            <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '0 0 1rem 0' }}><Activity size={18} color="var(--color-primary)" /> Quick Stats</h4>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>Weight</span>
              <span style={{ fontWeight: '500' }}>{formData.weight} kg</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>Last Donation</span>
              <span style={{ fontWeight: '500' }}>{formData.lastDonation}</span>
            </div>
          </GlassCard>
        </div>

        {/* Right Column - Medical Form */}
        <GlassCard>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', marginTop: 0 }}>
            <FileText color="var(--color-primary)" /> Comprehensive Medical History
          </h3>
          
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Known Allergies</label>
                <input name="allergies" value={formData.allergies} onChange={handleChange} type="text" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Current Medications</label>
                <input name="medications" value={formData.medications} onChange={handleChange} type="text" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Past Surgeries (Last 6 Months)</label>
                <textarea name="surgeries" value={formData.surgeries} onChange={handleChange} rows="3" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white', resize: 'vertical' }}></textarea>
              </div>
            </div>

            <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '1.5rem', marginTop: '0.5rem' }}>
              <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '0 0 1rem 0' }}><Settings size={18} color="var(--color-primary)" /> Privacy Settings</h4>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <input type="checkbox" id="shareData" defaultChecked style={{ width: '18px', height: '18px', accentColor: 'var(--color-primary)' }} />
                <label htmlFor="shareData" style={{ fontSize: '0.875rem' }}>Allow hospitals to view my medical history upon emergency SOS acceptance.</label>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <PrimaryButton type="submit" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Save size={18} /> Save Profile
              </PrimaryButton>
            </div>
          </form>
        </GlassCard>

      </div>
    </div>
  );
}
