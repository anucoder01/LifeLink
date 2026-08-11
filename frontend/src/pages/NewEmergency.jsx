import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, MapPin, Activity, Droplet } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function NewEmergency() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    patientName: '',
    bloodType: 'O-',
    units: 1,
    hospital: '',
    urgency: 'CRITICAL'
  });

  const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

  const handleSubmit = (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    // Simulate API call and geo-matching delay
    setTimeout(() => {
      setIsSubmitting(false);
      alert('EMERGENCY SOS BROADCASTED!\nMatching algorithm has triggered. Donors within a 5km radius are being notified right now via Push & SMS.');
      navigate('/my-requests');
    }, 2000);
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem', display: 'flex', justifyContent: 'center' }}>
      <div style={{ width: '100%', maxWidth: '600px' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(220, 38, 38, 0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem', animation: 'pulse 2s infinite' }}>
            <AlertCircle size={40} color="var(--color-primary)" />
          </div>
          <h2 style={{ margin: 0 }}>Initiate Emergency SOS</h2>
          <p style={{ color: 'var(--color-text-muted)', marginTop: '0.5rem' }}>
            This will trigger an immediate alert to all compatible donors in your radius.
          </p>
        </div>

        <GlassCard style={{ borderTop: '4px solid var(--color-primary)' }}>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Patient Name</label>
              <input required name="patientName" value={formData.patientName} onChange={handleChange} type="text" placeholder="Full Name" className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Required Blood Type</label>
                <div style={{ position: 'relative' }}>
                  <Droplet size={18} color="var(--color-primary)" style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)' }} />
                  <select name="bloodType" value={formData.bloodType} onChange={handleChange} className="form-input" style={{ width: '100%', padding: '0.75rem 1rem 0.75rem 2.5rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }}>
                    <option value="A+">A+</option>
                    <option value="A-">A-</option>
                    <option value="B+">B+</option>
                    <option value="B-">B-</option>
                    <option value="AB+">AB+</option>
                    <option value="AB-">AB-</option>
                    <option value="O+">O+</option>
                    <option value="O-">O-</option>
                  </select>
                </div>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Units Required</label>
                <input required name="units" value={formData.units} onChange={handleChange} type="number" min="1" max="10" className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Hospital Location (GPS Locked)</label>
              <div style={{ position: 'relative' }}>
                <MapPin size={18} color="var(--color-text-muted)" style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)' }} />
                <input required name="hospital" value={formData.hospital} onChange={handleChange} type="text" placeholder="e.g. Apollo Hospital, ER Ward" className="form-input" style={{ width: '100%', padding: '0.75rem 1rem 0.75rem 2.5rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Urgency Level</label>
              <select name="urgency" value={formData.urgency} onChange={handleChange} className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }}>
                <option value="CRITICAL">CRITICAL (Needed Immediately)</option>
                <option value="HIGH">HIGH (Needed within 4 hours)</option>
                <option value="MEDIUM">MEDIUM (Needed today)</option>
              </select>
            </div>

            <PrimaryButton disabled={isSubmitting} type="submit" style={{ width: '100%', marginTop: '1rem', padding: '1rem', fontSize: '1.1rem', display: 'flex', justifyContent: 'center', gap: '0.5rem' }}>
              {isSubmitting ? 'Broadcasting to Network...' : <><Activity size={20} /> Broadcast SOS Now</>}
            </PrimaryButton>
          </form>
        </GlassCard>

      </div>
    </div>
  );
}
