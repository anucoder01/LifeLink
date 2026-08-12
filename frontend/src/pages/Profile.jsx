import React, { useState, useEffect } from 'react';
import { User, Activity, FileText, Settings, Save } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import { api } from '../services/api';

export default function Profile() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    address: '',
    dateOfBirth: '',
    gender: '',
    emergencyContact: '',
    weight: '',
    medicalConditions: ''
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const response = await api.get('/users/me');
      const data = response.data;
      setFormData({
        name: data.name || '',
        email: data.email || '',
        address: data.address || '',
        dateOfBirth: data.dateOfBirth || '',
        gender: data.gender || '',
        emergencyContact: data.emergencyContact || '',
        weight: data.weight || '',
        medicalConditions: data.medicalConditions || ''
      });
    } catch (error) {
      console.error('Error fetching profile:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      const payload = { ...formData };
      if (payload.weight) payload.weight = parseFloat(payload.weight);
      await api.put('/users/me', payload);
      alert('Medical Profile Updated Successfully!\nThis data is encrypted and only shared with hospitals during an emergency match.');
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile. Please check the required fields.');
    }
  };

  const donorName = formData.name || 'User Name';

  if (loading) {
    return <div style={{ padding: '2rem', textAlign: 'center', color: 'white' }}>Loading profile...</div>;
  }

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
          <GlassCard className="animate-fade-in-delayed-1" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
            <div className="animate-pulse-glow" style={{ width: '100px', height: '100px', borderRadius: '50%', background: 'var(--color-primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '3rem', fontWeight: 'bold', marginBottom: '1rem', transition: 'all 0.3s ease' }}>
              <User size={48} />
            </div>
            <h3 style={{ margin: '0 0 0.25rem 0' }}>{donorName}</h3>
            <p style={{ margin: 0, color: 'var(--color-success)', fontWeight: 'bold' }}>Active User</p>
          </GlassCard>

          <GlassCard className="animate-fade-in-delayed-2">
            <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: '0 0 1rem 0' }}><Activity size={18} color="var(--color-primary)" /> Quick Stats</h4>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>Weight</span>
              <span style={{ fontWeight: '500' }}>{formData.weight || '--'} kg</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--color-text-muted)' }}>DOB</span>
              <span style={{ fontWeight: '500' }}>{formData.dateOfBirth || '--'}</span>
            </div>
          </GlassCard>
        </div>

        {/* Right Column - Medical Form */}
        <GlassCard className="animate-fade-in-delayed-3">
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', marginTop: 0 }}>
            <FileText color="var(--color-primary)" /> Comprehensive Medical History
          </h3>
          
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Name <span style={{color: 'red'}}>*</span></label>
                <input required name="name" value={formData.name} onChange={handleChange} type="text" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Email</label>
                <input name="email" value={formData.email} onChange={handleChange} type="email" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Address <span style={{color: 'red'}}>*</span></label>
                <input required name="address" value={formData.address} onChange={handleChange} type="text" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Date of Birth <span style={{color: 'red'}}>*</span></label>
                <input required name="dateOfBirth" value={formData.dateOfBirth} onChange={handleChange} type="date" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Gender</label>
                <select name="gender" value={formData.gender} onChange={handleChange} className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }}>
                  <option value="" style={{color: 'black'}}>Select Gender</option>
                  <option value="Male" style={{color: 'black'}}>Male</option>
                  <option value="Female" style={{color: 'black'}}>Female</option>
                  <option value="Other" style={{color: 'black'}}>Other</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Emergency Contact</label>
                <input name="emergencyContact" value={formData.emergencyContact} onChange={handleChange} type="text" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Weight (kg)</label>
                <input name="weight" value={formData.weight} onChange={handleChange} type="number" step="0.1" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Medical Conditions (Allergies, etc.)</label>
                <textarea name="medicalConditions" value={formData.medicalConditions} onChange={handleChange} rows="3" className="form-input" style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white', resize: 'vertical' }}></textarea>
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

