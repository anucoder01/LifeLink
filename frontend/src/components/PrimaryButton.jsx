import React from 'react';

const PrimaryButton = ({ children, onClick, type = 'button', disabled = false, className = '', variant = 'primary', style = {} }) => {
  const baseStyle = {
    padding: '0.75rem 1.5rem',
    borderRadius: 'var(--radius-md)',
    fontWeight: '600',
    fontFamily: 'var(--font-body)',
    fontSize: '1rem',
    cursor: disabled ? 'not-allowed' : 'pointer',
    transition: 'all 0.2s ease',
    border: 'none',
    outline: 'none',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.5rem',
    opacity: disabled ? 0.6 : 1,
  };

  const variants = {
    primary: {
      backgroundColor: 'var(--color-primary)',
      color: '#ffffff',
      boxShadow: '0 4px 14px rgba(220, 38, 38, 0.4)',
    },
    secondary: {
      backgroundColor: 'rgba(255, 255, 255, 0.1)',
      color: 'var(--color-text)',
      border: '1px solid var(--color-border)',
      backdropFilter: 'blur(10px)',
    },
    danger: {
      backgroundColor: 'transparent',
      color: 'var(--color-primary)',
      border: '1px solid var(--color-primary)',
    }
  };

  const hoverStyle = disabled ? {} : {
    transform: 'translateY(-2px)',
    boxShadow: variant === 'primary' 
      ? '0 6px 20px rgba(220, 38, 38, 0.6)' 
      : '0 6px 20px rgba(0, 0, 0, 0.2)',
    ...(variant === 'primary' && { backgroundColor: 'var(--color-primary-hover)' }),
    ...(variant === 'secondary' && { backgroundColor: 'rgba(255, 255, 255, 0.15)' }),
    ...(variant === 'danger' && { backgroundColor: 'rgba(220, 38, 38, 0.1)' }),
  };

  const [isHovered, setIsHovered] = React.useState(false);

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={className}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{ ...baseStyle, ...variants[variant], ...(isHovered ? hoverStyle : {}), ...style }}
    >
      {children}
    </button>
  );
};

export default PrimaryButton;
