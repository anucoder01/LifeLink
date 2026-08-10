import React from 'react';
import { Link } from 'react-router-dom';
import PrimaryButton from '../components/PrimaryButton';
import GlassCard from '../components/GlassCard';
import { Heart, Shield, Activity, Share2 } from 'lucide-react';

const Landing = () => {
  return (
    <div style={{ paddingTop: '100px', paddingBottom: '100px' }}>
      <div className="container animate-fade-in" style={{ textAlign: 'center', marginTop: '4rem', marginBottom: '6rem' }}>
        <h1 style={{ fontSize: '4rem', marginBottom: '1.5rem', color: 'white' }}>
          Save Lives. <br />
          <span style={{ color: 'var(--color-primary)' }}>When Seconds Matter.</span>
        </h1>
        <p style={{ fontSize: '1.25rem', color: 'var(--color-text-muted)', maxWidth: '700px', margin: '0 auto 3rem' }}>
          LifeLink is a next-generation blood donation network. We use real-time geo-matching and our unique 
          Blood Chain social vouching system to connect patients with nearby donors instantly.
        </p>
        <div style={{ display: 'flex', gap: '1.5rem', justifyContent: 'center' }}>
          <Link to="/emergency">
            <PrimaryButton variant="primary" style={{ padding: '1rem 2rem', fontSize: '1.1rem' }}>
              I Need Blood Now
            </PrimaryButton>
          </Link>
          <Link to="/auth">
            <PrimaryButton variant="secondary" style={{ padding: '1rem 2rem', fontSize: '1.1rem' }}>
              Register as Donor
            </PrimaryButton>
          </Link>
        </div>
      </div>

      <div className="container">
        <h2 style={{ textAlign: 'center', fontSize: '2.5rem', marginBottom: '4rem' }}>How It Works</h2>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '2rem' }}>
          <GlassCard style={{ textAlign: 'center' }}>
            <div style={{ background: 'rgba(220, 38, 38, 0.1)', width: '80px', height: '80px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.5rem', color: 'var(--color-primary)' }}>
              <Activity size={40} />
            </div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Real-time Geo-Matching</h3>
            <p style={{ color: 'var(--color-text-muted)' }}>
              Our system instantly finds eligible donors within a 5km radius and expands up to 30km automatically if no one responds.
            </p>
          </GlassCard>

          <GlassCard style={{ textAlign: 'center' }}>
            <div style={{ background: 'rgba(220, 38, 38, 0.1)', width: '80px', height: '80px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.5rem', color: 'var(--color-primary)' }}>
              <Share2 size={40} />
            </div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>The Blood Chain</h3>
            <p style={{ color: 'var(--color-text-muted)' }}>
              If no registered donors are found, we trigger the Blood Chain—notifying your trusted backup contacts to step in.
            </p>
          </GlassCard>

          <GlassCard style={{ textAlign: 'center' }}>
            <div style={{ background: 'rgba(220, 38, 38, 0.1)', width: '80px', height: '80px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.5rem', color: 'var(--color-primary)' }}>
              <Shield size={40} />
            </div>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Privacy First</h3>
            <p style={{ color: 'var(--color-text-muted)' }}>
              Your contact info is completely masked. Donors and requesters communicate through our secure system.
            </p>
          </GlassCard>
        </div>
      </div>
    </div>
  );
};

export default Landing;
