import React, { useState } from 'react';
import { Tent, Calendar, MapPin, Users, Heart } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Camps() {
  const [camps] = useState([
    {
      id: 1,
      name: 'City Central Mega Drive',
      organizer: 'Red Cross Society',
      date: 'This Saturday, 10:00 AM - 4:00 PM',
      location: 'Central Park Pavilion, Bengaluru',
      registered: 145,
      target: 500,
      isRegistered: false
    },
    {
      id: 2,
      name: 'Tech Park Techies for Life',
      organizer: 'Rotary Club IT Corridor',
      date: 'Next Tuesday, 9:00 AM - 6:00 PM',
      location: 'Manyata Tech Park, Block D',
      registered: 89,
      target: 200,
      isRegistered: true
    }
  ]);

  const handleRegister = (campId) => {
    alert(`Successfully registered for camp! We will send you a reminder 24 hours before.`);
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Tent color="var(--color-primary)" />
            Blood Donation Camps
          </h2>
          <p style={{ color: 'var(--color-text-muted)' }}>Find and participate in organized donation drives near you.</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        {camps.map(camp => (
          <GlassCard key={camp.id} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.25rem' }}>{camp.name}</h3>
                {camp.isRegistered && (
                  <span style={{ background: 'rgba(16, 185, 129, 0.2)', color: '#10b981', padding: '0.25rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold' }}>
                    Registered
                  </span>
                )}
              </div>
              <p style={{ margin: 0, color: 'var(--color-primary)', fontWeight: '500', fontSize: '0.875rem' }}>by {camp.organizer}</p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>
                <Calendar size={16} /> {camp.date}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>
                <MapPin size={16} /> {camp.location}
              </div>
            </div>

            <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--color-border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.875rem' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}><Users size={14}/> {camp.registered} Donors</span>
                <span style={{ color: 'var(--color-text-muted)' }}>Target: {camp.target}</span>
              </div>
              <div style={{ background: 'rgba(255,255,255,0.05)', borderRadius: '99px', height: '6px', overflow: 'hidden' }}>
                <div style={{ background: 'var(--color-primary)', width: `${(camp.registered / camp.target) * 100}%`, height: '100%' }}></div>
              </div>
            </div>

            <PrimaryButton 
              variant={camp.isRegistered ? 'secondary' : 'primary'} 
              disabled={camp.isRegistered}
              onClick={() => handleRegister(camp.id)}
              style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: '0.5rem' }}
            >
              <Heart size={18} /> {camp.isRegistered ? "You're Going!" : "Register Now"}
            </PrimaryButton>
          </GlassCard>
        ))}
      </div>
    </div>
  );
}
