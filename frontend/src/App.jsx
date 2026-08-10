import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Landing from './pages/Landing';

function App() {
  return (
    <Router>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/auth" element={<div style={{ paddingTop: '100px', textAlign: 'center' }}>Auth Page Coming Soon</div>} />
          <Route path="/emergency" element={<div style={{ paddingTop: '100px', textAlign: 'center' }}>Emergency Page Coming Soon</div>} />
          <Route path="/dashboard" element={<div style={{ paddingTop: '100px', textAlign: 'center' }}>Dashboard Coming Soon</div>} />
        </Routes>
      </main>
    </Router>
  );
}

export default App;
