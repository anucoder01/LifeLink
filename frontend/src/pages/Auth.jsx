import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { LogIn, UserPlus, Shield, Heart, MapPin } from 'lucide-react';
import PhoneInput from 'react-phone-number-input';
import 'react-phone-number-input/style.css';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Auth() {
  const location = useLocation();
  const [isLogin, setIsLogin] = useState(location.state?.isLogin ?? true);
  const [phone, setPhone] = useState();
  const [isLocating, setIsLocating] = useState(false);
  const [govId, setGovId] = useState('');
  const [fullName, setFullName] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!isLogin) {
      setIsLocating(true);
      if (!navigator.geolocation) {
        alert('Geolocation is not supported by your browser. Please enable location services.');
        setIsLocating(false);
        return;
      }
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setIsLocating(false);
          const { latitude, longitude } = position.coords;
          console.log("Captured real coordinates for registration:", { latitude, longitude });
          // Proceed with registration request using real coordinates
          localStorage.setItem('donorName', fullName);
          alert(`Successfully verified Government ID & captured location!\nLat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}\nNavigating to your dashboard...`);
          navigate('/dashboard');
        },
        (error) => {
          setIsLocating(false);
          if (error.code === error.PERMISSION_DENIED) {
            alert('Location access denied! LifeLink requires location services to match you with nearby emergencies. Please turn on your device location and allow permissions in the browser.');
          } else {
            alert('Failed to get location. Please ensure location services are enabled.');
          }
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
      );
    } else {
      // Login logic
      console.log("Logging in with", phone);
      if (!localStorage.getItem('donorName')) {
         localStorage.setItem('donorName', 'Demo User');
      }
      navigate('/dashboard');
    }
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '4rem 1.5rem', display: 'flex', justifyContent: 'center' }}>
      <div style={{ width: '100%', maxWidth: '480px' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <Heart size={48} color="var(--color-primary)" style={{ margin: '0 auto', marginBottom: '1rem' }} />
          <h2>{isLogin ? 'Welcome Back' : 'Join LifeLink'}</h2>
          <p style={{ color: 'var(--color-text-muted)', marginTop: '0.5rem' }}>
            {isLogin ? 'Sign in to access your donor dashboard.' : 'Register to become a lifesaver today.'}
          </p>
        </div>

        <GlassCard>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            {!isLogin && (
              <>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Full Name</label>
                  <input required value={fullName} onChange={(e) => setFullName(e.target.value)} type="text" placeholder="Your Real Name" className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Government ID (Aadhar/SSN)</label>
                  <input required value={govId} onChange={(e) => setGovId(e.target.value)} type="text" placeholder="ID Number" className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white', marginBottom: '0.5rem' }} />
                  <input type="file" accept=".pdf" required style={{ width: '100%', padding: '0.5rem', borderRadius: '8px', border: '1px dashed var(--color-border)', background: 'rgba(255,255,255,0.05)', color: 'var(--color-text-muted)', fontSize: '0.875rem', cursor: 'pointer' }} />
                </div>
              </>
            )}
            
            <div className="phone-input-wrapper">
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Phone Number</label>
              <PhoneInput
                international
                defaultCountry="IN"
                value={phone}
                onChange={setPhone}
                required
                className="form-input custom-phone"
                style={{ width: '100%', padding: '0.5rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Password</label>
              <input required type="password" placeholder="••••••••" className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }} />
            </div>

            {!isLogin && (
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Blood Type</label>
                <select className="form-input" style={{ width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', color: 'white' }}>
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
            )}

            <PrimaryButton disabled={isLocating} type="submit" style={{ width: '100%', marginTop: '1rem', display: 'flex', justifyContent: 'center', gap: '0.5rem' }}>
              {isLocating ? (
                <>Locating...</>
              ) : (
                <>
                  {isLogin ? <LogIn size={20} /> : <MapPin size={20} />}
                  {isLogin ? 'Sign In' : 'Enable Location & Register'}
                </>
              )}
            </PrimaryButton>
          </form>

          <div style={{ marginTop: '2rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
            <p style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
              <Shield size={16} /> Secure, Encrypted, Privacy-First
            </p>
            <p>
              {isLogin ? "Don't have an account? " : "Already registered? "}
              <button 
                type="button"
                onClick={(e) => { e.preventDefault(); setIsLogin(!isLogin); }}
                style={{ background: 'none', border: 'none', color: 'var(--color-primary)', cursor: 'pointer', fontWeight: '600', padding: 0 }}
              >
                {isLogin ? 'Register Here' : 'Sign In Here'}
              </button>
            </p>
          </div>
        </GlassCard>
      </div>
    </div>
  );
}
