import React, { useState } from 'react';
import { Truck, Navigation, CheckCircle2, Clock, Map, Lock } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function DriverDashboard() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [password, setPassword] = useState('');
  
  const [tasks, setTasks] = useState([
    {
      id: 1,
      type: 'DONOR_PICKUP',
      title: 'Urgent Donor Transport',
      pickup: 'Koramangala, 4th Block',
      dropoff: 'City General Hospital',
      distance: '4.2 km',
      timeEstimate: '15 mins',
      status: 'AVAILABLE'
    },
    {
      id: 2,
      type: 'BLOOD_DELIVERY',
      title: 'Blood Bag Transfer (Cold Chain)',
      pickup: 'City General Hospital Blood Bank',
      dropoff: 'Apollo Spectra ER',
      distance: '12.5 km',
      timeEstimate: '35 mins',
      status: 'IN_PROGRESS'
    }
  ]);

  const handleAction = (id, action) => {
    if (action === 'ACCEPTED') {
      setTasks(tasks.map(t => t.id === id ? { ...t, status: 'IN_PROGRESS' } : t));
    } else if (action === 'COMPLETED') {
      setTasks(tasks.filter(t => t.id !== id));
    }
  };

  const handleLogin = (e) => {
    e.preventDefault();
    if (password === 'driver123') {
      setIsAuthenticated(true);
    } else {
      alert('Invalid driver passcode. Try "driver123".');
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="container animate-fade-in" style={{ padding: '4rem 1.5rem', display: 'flex', justifyContent: 'center' }}>
        <GlassCard style={{ width: '100%', maxWidth: '400px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <Lock size={48} color="var(--color-primary)" style={{ marginBottom: '1rem' }} />
          <h2 style={{ marginBottom: '0.5rem', margin: 0 }}>Driver Access</h2>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: '2rem', textAlign: 'center', fontSize: '0.875rem' }}>
            This area is restricted to verified volunteer drivers.
          </p>
          
          <form onSubmit={handleLogin} style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter Driver Passcode" 
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
            <Truck color="var(--color-primary)" />
            Volunteer Driver Portal
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Help save lives by providing crucial transportation logistics.</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '0.5rem 1rem', borderRadius: '99px', fontWeight: 'bold' }}>
          <div style={{ width: '8px', height: '8px', background: '#10b981', borderRadius: '50%' }}></div>
          On Duty
        </div>
      </div>

      {tasks.length === 0 ? (
        <GlassCard style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '4rem 2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>
          <CheckCircle2 size={48} color="var(--color-success)" style={{ marginBottom: '1rem' }} />
          <h3 style={{ margin: '0 0 0.5rem 0', color: 'white' }}>All Caught Up!</h3>
          <p style={{ margin: 0 }}>You have completed all active rides. Standby for emergency logistics requests.</p>
        </GlassCard>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '1.5rem' }}>
          {tasks.map(task => (
            <GlassCard key={task.id} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', borderLeft: task.type === 'DONOR_PICKUP' ? '4px solid var(--color-warning)' : '4px solid var(--color-primary)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <span style={{ 
                    fontSize: '0.75rem', fontWeight: 'bold', padding: '0.25rem 0.5rem', borderRadius: '4px',
                    background: task.type === 'DONOR_PICKUP' ? 'rgba(245, 158, 11, 0.2)' : 'rgba(220, 38, 38, 0.2)',
                    color: task.type === 'DONOR_PICKUP' ? 'var(--color-warning)' : 'var(--color-primary)'
                  }}>
                    {task.type.replace('_', ' ')}
                  </span>
                  <h3 style={{ margin: '0.75rem 0 0 0', fontSize: '1.25rem' }}>{task.title}</h3>
                </div>
                <span style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <Clock size={14} /> {task.timeEstimate}
                </span>
              </div>

              <div style={{ background: 'rgba(0,0,0,0.1)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-start' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.25rem', marginTop: '0.25rem' }}>
                    <div style={{ width: '10px', height: '10px', borderRadius: '50%', border: '2px solid var(--color-text-muted)' }}></div>
                    <div style={{ width: '2px', height: '20px', background: 'var(--color-border)' }}></div>
                    <Map color="var(--color-primary)" size={14} />
                  </div>
                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    <div>
                      <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>PICKUP</p>
                      <p style={{ margin: 0, fontWeight: '500' }}>{task.pickup}</p>
                    </div>
                    <div>
                      <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>DROPOFF</p>
                      <p style={{ margin: 0, fontWeight: '500' }}>{task.dropoff}</p>
                    </div>
                  </div>
                </div>
              </div>

              {task.status === 'AVAILABLE' ? (
                <PrimaryButton onClick={() => handleAction(task.id, 'ACCEPTED')} style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: '0.5rem' }}>
                  <Navigation size={18} /> Accept Ride
                </PrimaryButton>
              ) : (
                <PrimaryButton onClick={() => handleAction(task.id, 'COMPLETED')} style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: '0.5rem', background: 'var(--color-success)', boxShadow: '0 4px 14px rgba(16,185,129,0.4)' }}>
                  <CheckCircle2 size={18} /> Complete Trip
                </PrimaryButton>
              )}
            </GlassCard>
          ))}
        </div>
      )}
    </div>
  );
}
