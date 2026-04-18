import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './NavBar.css';

export default function NavBar() {
  const { user, logout } = useAuth();

  return (
    <header className="navbar">
      <div className="navbar__logo">
        <span className="navbar__logo-icon">♫</span>
        <span className="navbar__logo-text">
          Spoti<span className="navbar__logo-accent">2.0</span>
        </span>
      </div>

      <nav className="navbar__links">
        <NavLink to="/home" className={({ isActive }) => (isActive ? 'navbar__link active' : 'navbar__link')}>
          Home
        </NavLink>
        <NavLink to="/feed" className={({ isActive }) => (isActive ? 'navbar__link active' : 'navbar__link')}>
          Town Square
        </NavLink>
      </nav>

      <div className="navbar__user">
        <span className="navbar__avatar" aria-hidden="true">
          {user?.username?.[0]?.toUpperCase() ?? '?'}
        </span>
        <span className="navbar__username">{user?.username}</span>
        <button id="btn-logout" className="navbar__logout" onClick={logout}>
          Log out
        </button>
      </div>
    </header>
  );
}
