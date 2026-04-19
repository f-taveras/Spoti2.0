import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../services/apiClient';
import ShareModal from './ShareModal';
import './PlaylistGrid.css';

interface Image {
  url: string;
}

interface Playlist {
  id: string;
  name: string;
  owner: {
    display_name: string;
  };
  images: Image[];
}

const PlaylistGrid: React.FC = () => {
  const navigate = useNavigate();
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Sharing state
  const [shareData, setShareData] = useState<{
    isOpen: boolean;
    spotifyId: string;
    mediaName: string;
    mediaArtist: string;
    mediaArtUrl: string;
  }>({
    isOpen: false,
    spotifyId: '',
    mediaName: '',
    mediaArtist: '',
    mediaArtUrl: ''
  });

  const handleShareClick = (e: React.MouseEvent, playlist: Playlist) => {
    e.stopPropagation(); // don't trigger the card navigator
    setShareData({
      isOpen: true,
      spotifyId: playlist.id,
      mediaName: playlist.name,
      mediaArtist: playlist.owner.display_name,
      mediaArtUrl: playlist.images?.[0]?.url || ''
    });
  };

  useEffect(() => {
    const fetchPlaylists = async () => {
      try {
        const data = await apiClient.get<{ items: Playlist[] }>('/api/spotify/playlists');
        setPlaylists(data.items || []);
      } catch (err: any) {
        console.error('Failed to fetch playlists:', err);
        if (err.message === 'User does not have a connected Spotify account') {
          setError('local');
        } else {
          setError('Could not load playlists. Please try again later.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchPlaylists();
  }, []);

  if (loading) {
    return <div className="playlists-loading">Loading playlists...</div>;
  }

  if (error === 'local') {
    return (
      <div className="playlists-empty">
        <p>You need to connect your Spotify account to see your playlists.</p>
        <a href={`${(import.meta.env.VITE_API_BASE_URL || '').endsWith('/') ? (import.meta.env.VITE_API_BASE_URL || '').slice(0, -1) : (import.meta.env.VITE_API_BASE_URL || '')}/oauth2/authorization/spotify`} className="btn-spotify">
           Connect Spotify
        </a>
      </div>
    );
  }

  if (error) {
    return <div className="playlists-error">{error}</div>;
  }

  if (playlists.length === 0) {
    return <div className="playlists-empty">No playlists found.</div>;
  }

  return (
    <>
      <div className="playlist-grid">
      {playlists.map((playlist) => (
        <div key={playlist.id} className="playlist-card" onClick={() => navigate(`/playlist/${playlist.id}`)}>
          <div className="playlist-image-container">
            {playlist.images && playlist.images.length > 0 ? (
              <img src={playlist.images[0].url} alt={playlist.name} className="playlist-image" />
            ) : (
              <div className="playlist-image-fallback">
                <span>🎵</span>
              </div>
            )}
          </div>
          <div className="playlist-info-row">
            <div>
              <h3 className="playlist-name">{playlist.name}</h3>
              <p className="playlist-owner">{playlist.owner.display_name}</p>
            </div>
            <button className="btn-share" onClick={(e) => handleShareClick(e, playlist)} title="Share to Town Square">
              📣
            </button>
          </div>
        </div>
      ))}
      </div>
      <ShareModal 
        isOpen={shareData.isOpen}
        onClose={() => setShareData(prev => ({ ...prev, isOpen: false }))}
        mediaType="PLAYLIST"
        spotifyId={shareData.spotifyId}
        mediaName={shareData.mediaName}
        mediaArtist={shareData.mediaArtist}
        mediaArtUrl={shareData.mediaArtUrl}
      />
    </>
  );
};

export default PlaylistGrid;
