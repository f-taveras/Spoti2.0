import { useAuth } from '../context/AuthContext';
import PlaylistGrid from '../components/PlaylistGrid';
import NavBar from '../components/NavBar';
import './HomePage.css';

export default function HomePage() {
  const { user } = useAuth();

  return (
    <div className="home">
      <NavBar />

      {/* ── Main content ─────────────────────────────────────── */}
      <main className="home__main">
        <div className="home__card" role="status" aria-live="polite">
          <div className="home__icon" aria-hidden="true">🎵</div>

          <h1 className="home__title">Successfully Connected!</h1>
          <p className="home__subtitle">
            You're logged in as{' '}
            <strong className="home__name">{user?.username}</strong>.
            <br />
            The frontend is talking to the Spring Boot backend.
          </p>

          <div className="home__badge">
            <span className="home__badge-dot" />
            Backend connected
          </div>
        </div>

        <section className="home__playlists-section">
          <h2 className="home__section-title">Your Playlists</h2>
          <PlaylistGrid />
        </section>
      </main>
    </div>
  );
}
