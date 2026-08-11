import React, { useState } from 'react';
import { MessageSquare, Send, User, Phone, Shield } from 'lucide-react';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';

export default function Chat() {
  const [messages, setMessages] = useState([
    { id: 1, text: "Emergency broadcast received. Are you available to donate O- blood?", sender: 'system', time: '10:00 AM' },
    { id: 2, text: "Yes, I am available. I can reach Apollo Hospital in 15 minutes.", sender: 'me', time: '10:02 AM' },
    { id: 3, text: "Thank you! The patient's coordinator has been notified. Please proceed to the ER reception.", sender: 'coordinator', time: '10:03 AM' }
  ]);
  const [input, setInput] = useState('');

  const handleSend = (e) => {
    e.preventDefault();
    if (!input.trim()) return;
    
    const newMsg = {
      id: Date.now(),
      text: input,
      sender: 'me',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    
    setMessages([...messages, newMsg]);
    setInput('');
  };

  return (
    <div className="container animate-fade-in" style={{ padding: '2rem 1.5rem', display: 'flex', flexDirection: 'column', height: 'calc(100vh - 100px)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <div>
          <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
            <MessageSquare color="var(--color-primary)" />
            Secure Messaging
          </h2>
          <p style={{ color: 'var(--color-text-muted)', margin: '0.25rem 0 0 0', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <Shield size={14} color="var(--color-success)" /> End-to-end encrypted. Phone numbers are hidden.
          </p>
        </div>
        <PrimaryButton style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Phone size={18} /> VoIP Call
        </PrimaryButton>
      </div>

      <GlassCard style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 0, overflow: 'hidden' }}>
        {/* Chat Header */}
        <div style={{ padding: '1rem', borderBottom: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)', display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'var(--color-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <User size={20} color="white" />
          </div>
          <div>
            <h4 style={{ margin: 0 }}>Apollo Hospital Coordinator</h4>
            <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--color-success)' }}>Online</p>
          </div>
        </div>

        {/* Message Feed */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {messages.map((msg) => (
            <div key={msg.id} style={{ 
              display: 'flex', 
              flexDirection: 'column',
              alignItems: msg.sender === 'me' ? 'flex-end' : 'flex-start'
            }}>
              <div style={{
                maxWidth: '70%',
                padding: '0.75rem 1rem',
                borderRadius: '12px',
                background: msg.sender === 'me' ? 'var(--color-primary)' : 'rgba(255,255,255,0.05)',
                color: 'white',
                border: msg.sender === 'me' ? 'none' : '1px solid var(--color-border)',
                borderBottomRightRadius: msg.sender === 'me' ? 0 : '12px',
                borderBottomLeftRadius: msg.sender === 'me' ? '12px' : 0,
              }}>
                <p style={{ margin: 0, fontSize: '0.9rem', lineHeight: '1.4' }}>{msg.text}</p>
              </div>
              <span style={{ fontSize: '0.7rem', color: 'var(--color-text-muted)', marginTop: '0.25rem' }}>{msg.time}</span>
            </div>
          ))}
        </div>

        {/* Message Input */}
        <div style={{ padding: '1rem', borderTop: '1px solid var(--color-border)', background: 'rgba(0,0,0,0.2)' }}>
          <form onSubmit={handleSend} style={{ display: 'flex', gap: '0.5rem' }}>
            <input 
              type="text" 
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your message securely..." 
              className="form-input"
              style={{ flex: 1, padding: '0.75rem 1rem', borderRadius: '99px', border: '1px solid var(--color-border)', background: 'rgba(255,255,255,0.05)', color: 'white' }}
            />
            <button type="submit" style={{ 
              width: '48px', height: '48px', 
              borderRadius: '50%', 
              background: 'var(--color-primary)', 
              color: 'white', 
              border: 'none', 
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              cursor: 'pointer'
            }}>
              <Send size={20} />
            </button>
          </form>
        </div>
      </GlassCard>
    </div>
  );
}
