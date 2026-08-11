import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Landing from './pages/Landing';
import Auth from './pages/Auth';
import Dashboard from './pages/Dashboard';
import Requests from './pages/Requests';
import MyRequests from './pages/MyRequests';
import HospitalAdmin from './pages/HospitalAdmin';
import Camps from './pages/Camps';
import DriverDashboard from './pages/DriverDashboard';
import NGOAdmin from './pages/NGOAdmin';
import Profile from './pages/Profile';
import Chat from './pages/Chat';
import NewEmergency from './pages/NewEmergency';
import Navbar from './components/Navbar';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Navbar />
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/auth" element={<Auth />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/requests" element={<Requests />} />
          <Route path="/my-requests" element={<MyRequests />} />
          <Route path="/admin" element={<HospitalAdmin />} />
          <Route path="/camps" element={<Camps />} />
          <Route path="/driver" element={<DriverDashboard />} />
          <Route path="/ngo" element={<NGOAdmin />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/new-sos" element={<NewEmergency />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
