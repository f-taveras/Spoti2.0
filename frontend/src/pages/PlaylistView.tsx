import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../services/apiClient';
import WebPlayback from '../components/WebPlayback';
import NavBar from '../components/NavBar';
import ShareModal from '../components/ShareModal';
import './PlaylistView.css';

interface Track {
  id: string;
  uri: string;
  name: string;
  artists: { name: string }[];
  album: {
    images: { url: string }[];
  };
}

interface PlaylistTrack {
  track: Track;
}

export default function PlaylistView() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [tracks, setTracks] = useState<PlaylistTrack[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [shareData, setShareData] = useState({
    isOpen: false,
    spotifyId: '',
    mediaName: '',
    mediaArtist: '',
    mediaArtUrl: ''
  });

  const handleShareClick = (e: React.MouseEvent, track: Track) => {
    e.stopPropagation();
    setShareData({
      isOpen: true,
      spotifyId: track.id,
      mediaName: track.name,
      mediaArtist: track.artists?.map(a => a.name).join(', ') || 'Unknown',
      mediaArtUrl: track.album?.images?.[0]?.url || ''
    });
  };

  useEffect(() => {
    const fetchTracks = async () => {
      try {
        const data = await apiClient.get<any>(`/api/spotify/playlists/${id}/tracks`);
        setTracks(data.items || []);
      } catch (err: any) {
        console.error('Failed to fetch tracks:', err);
        setError('Could not load playlist tracks.');
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchTracks();
  }, [id]);

  return (
    <div className="playlist-view">
      <NavBar />
      <div className="playlist-view__header">
        <button className="btn-back" onClick={() => navigate('/home')}>
          ← Back to Home
        </button>
      </div>

      <div className="playlist-view__content">
        <h2>Playlist Tracks</h2>

        {loading && <div className="loading">Loading tracks...</div>}
        {error && <div className="error">{error}</div>}

        {!loading && !error && (
          <div className="tracks-list">
            {tracks.map(({ track }, index) => {
              if (!track) return null; // Some local files in Spotify might not have a full track object
              return (
                <div key={`${track.id}-${index}`} className="track-item">
                  <div className="track-img-wrapper">
                    {track.album?.images?.[0]?.url && (
                      <img src={track.album.images[0].url} alt={track.name} className="track-img" />
                    )}
                  </div>
                  <div className="track-info" style={{ flex: 1 }}>
                    <span className="track-name">{track.name}</span>
                    <span className="track-artist">
                      {track.artists?.map(a => a.name).join(', ')}
                    </span>
                  </div>
                  <button className="btn-share" onClick={(e) => handleShareClick(e, track)} title="Share to Town Square">
                    📣
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <ShareModal 
        isOpen={shareData.isOpen}
        onClose={() => setShareData(prev => ({ ...prev, isOpen: false }))}
        mediaType="TRACK"
        spotifyId={shareData.spotifyId}
        mediaName={shareData.mediaName}
        mediaArtist={shareData.mediaArtist}
        mediaArtUrl={shareData.mediaArtUrl}
      />

      {/* Persistent Web Playback UI */}
      <WebPlayback />
    </div>
  );
}
