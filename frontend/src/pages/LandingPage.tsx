import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './LandingPage.css';

type Mode = 'login' | 'register';

export default function LandingPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const [mode, setMode]         = useState<Mode>('login');
  const [username, setUsername] = useState('');
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

  const switchMode = (next: Mode) => {
    setMode(next);
    setError('');
    setUsername('');
    setEmail('');
    setPassword('');
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (mode === 'login') {
        await login({ username, password });
      } else {
        await register({ username, email, password });
      }
      navigate('/home');
    } catch (err: unknown) {
      const msg =
        err && typeof err === 'object' && 'message' in err
          ? String((err as { message: string }).message)
          : 'Something went wrong. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="landing">
      {/* ── Animated background orbs ────────────────────────── */}
      <div className="landing__orb landing__orb--green" aria-hidden="true" />
      <div className="landing__orb landing__orb--purple" aria-hidden="true" />

      {/* ── Left branding panel ─────────────────────────────── */}
      <aside className="landing__brand">
        <div className="landing__brand-content">
          <div className="landing__logo">
            <span className="landing__logo-icon">♫</span>
            <span className="landing__logo-text">Spoti<span className="landing__logo-accent">2.0</span></span>
          </div>
          <p className="landing__tagline">Your music,<br />your universe.</p>

          {/* Animated sound wave */}
          <div className="landing__wave" aria-hidden="true">
            {Array.from({ length: 12 }).map((_, i) => (
              <span key={i} className="landing__wave-bar" style={{ animationDelay: `${i * 0.1}s` }} />
            ))}
          </div>

          <p className="landing__brand-sub">
            Stream, discover, and share music<br />with the people you love.
          </p>
        </div>
      </aside>

      {/* ── Right auth panel ────────────────────────────────── */}
      <main className="landing__auth">
        <div className="landing__card">

          {/* Tab switcher */}
          <div className="auth-tabs" role="tablist">
            <button
              id="tab-login"
              role="tab"
              aria-selected={mode === 'login'}
              className={`auth-tab ${mode === 'login' ? 'auth-tab--active' : ''}`}
              onClick={() => switchMode('login')}
            >
              Log In
            </button>
            <button
              id="tab-register"
              role="tab"
              aria-selected={mode === 'register'}
              className={`auth-tab ${mode === 'register' ? 'auth-tab--active' : ''}`}
              onClick={() => switchMode('register')}
            >
              Register
            </button>
            <div
              className="auth-tab-indicator"
              style={{ transform: mode === 'register' ? 'translateX(100%)' : 'translateX(0)' }}
            />
          </div>

          {/* Form heading */}
          <h1 className="auth-heading">
            {mode === 'login' ? 'Welcome back' : 'Create account'}
          </h1>
          <p className="auth-subheading">
            {mode === 'login'
              ? 'Sign in to continue listening'
              : 'Join Spoti2.0 today — it\'s free'}
          </p>

          {/* Error banner */}
          {error && (
            <div className="auth-error" role="alert">
              <span>⚠</span> {error}
            </div>
          )}

          {/* Auth form */}
          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="username" className="form-label">Username</label>
              <input
                id="username"
                type="text"
                className="form-input"
                placeholder="Enter your username"
                value={username}
                onChange={e => setUsername(e.target.value)}
                autoComplete="username"
                required
              />
            </div>

            {mode === 'register' && (
              <div className="form-field">
                <label htmlFor="email" className="form-label">Email</label>
                <input
                  id="email"
                  type="email"
                  className="form-input"
                  placeholder="Enter your email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  autoComplete="email"
                  required
                />
              </div>
            )}

            <div className="form-field">
              <label htmlFor="password" className="form-label">Password</label>
              <input
                id="password"
                type="password"
                className="form-input"
                placeholder={mode === 'login' ? 'Enter your password' : 'At least 6 characters'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                required
              />
            </div>

            <button
              id={`btn-${mode}`}
              type="submit"
              className="btn-primary"
              disabled={loading}
            >
              {loading
                ? <span className="btn-spinner" />
                : mode === 'login' ? 'Sign In' : 'Create Account'}
            </button>
          </form>

          {/* Footer toggle */}
          <p className="auth-footer">
            {mode === 'login'
              ? "Don't have an account? "
              : 'Already have an account? '}
            <button
              className="auth-toggle"
              onClick={() => switchMode(mode === 'login' ? 'register' : 'login')}
            >
              {mode === 'login' ? 'Register' : 'Sign In'}
            </button>
          </p>

          {/* Spotify OAuth */}
          <div className="auth-divider" aria-hidden="true">
            <span>or</span>
          </div>
          <a
            id="btn-spotify"
            href={`${import.meta.env.VITE_API_BASE_URL ?? ''}/oauth2/authorization/spotify`}
            className="btn-spotify"
            aria-label="Continue with Spotify"
          >
            <span className="btn-spotify-icon">♫</span>
            Continue with Spotify
          </a>
        </div>
      </main>
    </div>
  );
}
