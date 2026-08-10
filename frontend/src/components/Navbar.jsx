import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Droplet, Menu, X } from 'lucide-react';
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
        <Link to="/emergency" style={getLinkStyle('/emergency')}>Need Blood</Link>
        <Link to="/dashboard" style={getLinkStyle('/dashboard')}>Dashboard</Link>
        <Link to="/auth">
          <PrimaryButton variant="primary" style={{ padding: '0.5rem 1rem' }}>Sign In</PrimaryButton>
        </Link>
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
