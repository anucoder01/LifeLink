import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, MapPin, Activity, Droplet, Radar, Search, Filter, CheckCircle2 } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function NewEmergency() {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  const [scanStep, setScanStep] = useState(0);
  
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
    setIsScanning(true);
    setScanStep(1);
  };

  useEffect(() => {
    if (isScanning) {
      if (scanStep === 1) {
        setTimeout(() => setScanStep(2), 2000); // Locating donors
      } else if (scanStep === 2) {
        setTimeout(() => setScanStep(3), 2000); // Filtering travel history
      } else if (scanStep === 3) {
        setTimeout(() => setScanStep(4), 2000); // Filtering recent donations
      } else if (scanStep === 4) {
        setTimeout(() => {
          setIsScanning(false);
          navigate('/my-requests');
        }, 3000); // Broadcasting and redirect
      }
    }
  }, [isScanning, scanStep, navigate]);

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

        {isScanning ? (
          <GlassCard style={{ borderTop: '4px solid var(--color-primary)', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '3rem 2rem', textAlign: 'center' }}>
            <div style={{ position: 'relative', width: '120px', height: '120px', marginBottom: '2rem' }}>
              <div style={{ position: 'absolute', inset: 0, border: '2px solid var(--color-primary)', borderRadius: '50%', opacity: 0.2, animation: 'ping 2s cubic-bezier(0, 0, 0.2, 1) infinite' }}></div>
              <div style={{ position: 'absolute', inset: '20px', border: '2px solid var(--color-primary)', borderRadius: '50%', opacity: 0.5, animation: 'ping 2s cubic-bezier(0, 0, 0.2, 1) infinite', animationDelay: '0.5s' }}></div>
              <div style={{ position: 'absolute', inset: '40px', background: 'var(--color-primary)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 10 }}>
                <Radar size={24} color="white" />
              </div>
            </div>
            
            <h3 style={{ margin: '0 0 1.5rem 0' }}>Advanced Matching Algorithm</h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '100%', maxWidth: '400px', textAlign: 'left' }}>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', opacity: scanStep >= 1 ? 1 : 0.3, transition: 'opacity 0.5s' }}>
                {scanStep > 1 ? <CheckCircle2 color="var(--color-success)" size={20} /> : <Search color="var(--color-primary)" size={20} className={scanStep === 1 ? 'animate-spin' : ''} />}
                <span>Identifying all donors in 10km radius...</span>
              </div>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', opacity: scanStep >= 2 ? 1 : 0.3, transition: 'opacity 0.5s' }}>
                {scanStep > 2 ? <CheckCircle2 color="var(--color-success)" size={20} /> : <Filter color="var(--color-primary)" size={20} className={scanStep === 2 ? 'animate-pulse' : ''} />}
                <span>Filtering recent malaria zone travel history...</span>
              </div>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', opacity: scanStep >= 3 ? 1 : 0.3, transition: 'opacity 0.5s' }}>
                {scanStep > 3 ? <CheckCircle2 color="var(--color-success)" size={20} /> : <Filter color="var(--color-primary)" size={20} className={scanStep === 3 ? 'animate-pulse' : ''} />}
                <span>Filtering donors who donated &lt; 56 days ago...</span>
              </div>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', opacity: scanStep >= 4 ? 1 : 0, transition: 'opacity 0.5s', marginTop: '1rem', padding: '1rem', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid var(--color-success)', borderRadius: '8px' }}>
                <Activity color="var(--color-success)" size={24} />
                <div>
                  <strong style={{ color: 'var(--color-success)' }}>12 Eligible Donors Found.</strong><br/>
                  <span style={{ fontSize: '0.875rem' }}>Broadcasting secure SMS & Push alerts...</span>
                </div>
              </div>
            </div>
          </GlassCard>
        ) : (
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
        )}

      </div>
    </div>
  );
}
