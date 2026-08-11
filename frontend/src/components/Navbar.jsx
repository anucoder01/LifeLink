import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Droplet, Menu, X, Bell, User } from 'lucide-react';
import PrimaryButton from './PrimaryButton';

const Navbar = () => {
  const [isOpen, setIsOpen] = React.useState(false);
  const location = useLocation();

  const navStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    height: '70px',
    background: 'rgba(15, 23, 42, 0.8)',
    backdropFilter: 'blur(16px)',
    borderBottom: '1px solid var(--color-border)',
    zIndex: 1000,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 2rem',
  };

  const logoStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    color: 'white',
    fontSize: '1.5rem',
    fontWeight: '700',
    fontFamily: 'var(--font-heading)',
  };

  const linksStyle = {
    display: 'flex',
    gap: '2rem',
    alignItems: 'center',
  };

  const getLinkStyle = (path) => ({
    color: location.pathname === path ? 'var(--color-primary)' : 'var(--color-text)',
    fontWeight: location.pathname === path ? '600' : '500',
    fontSize: '1rem',
  });

  return (
    <nav style={navStyle}>
      <Link to="/" style={logoStyle}>
        <Droplet color="var(--color-primary)" fill="var(--color-primary)" size={28} />
        LifeLink
      </Link>
      
      {/* Desktop Menu */}
      <div style={linksStyle} className="desktop-menu">
        <Link to="/" style={getLinkStyle('/')}>Home</Link>
        <Link to="/requests" style={getLinkStyle('/requests')}>Donate</Link>
        <Link to="/my-requests" style={getLinkStyle('/my-requests')}>My SOS</Link>
        <Link to="/camps" style={getLinkStyle('/camps')}>Camps</Link>
        <Link to="/driver" style={getLinkStyle('/driver')}>Drive</Link>
        <Link to="/ngo" style={getLinkStyle('/ngo')}>NGO</Link>
        <Link to="/admin" style={getLinkStyle('/admin')}>Admin</Link>
        <Link to="/dashboard" style={getLinkStyle('/dashboard')}>Dashboard</Link>
        
        {/* Notifications & Profile */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginLeft: '1rem', borderLeft: '1px solid var(--color-border)', paddingLeft: '1.5rem' }}>
          <div style={{ position: 'relative', cursor: 'pointer' }} onClick={() => alert("Notifications:\n1. O- Blood needed 2km away!\n2. Your SOS request is IN_PROGRESS")}>
            <Bell size={20} color="var(--color-text)" />
            <div style={{ position: 'absolute', top: '-4px', right: '-4px', width: '10px', height: '10px', background: 'var(--color-primary)', borderRadius: '50%' }}></div>
          </div>
          <Link to="/profile" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '36px', height: '36px', borderRadius: '50%', background: 'rgba(255,255,255,0.1)', color: 'var(--color-text)' }}>
            <User size={18} />
          </Link>
          <Link to="/auth" style={{ marginLeft: '0.5rem' }}>
            <PrimaryButton variant="primary" style={{ padding: '0.5rem 1rem' }}>Sign In</PrimaryButton>
          </Link>
        </div>
      </div>

      <style>{`
        @media (max-width: 768px) {
          .desktop-menu { display: none !important; }
        }
      `}</style>
    </nav>
  );
};

export default Navbar;
