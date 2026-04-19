import { useState, useEffect } from 'react';
import { apiClient } from '../services/apiClient';
import './WebPlayback.css';

declare global {
  interface Window {
    onSpotifyWebPlaybackSDKReady: () => void;
    Spotify: any;
  }
}

export default function WebPlayback() {
  const [player, setPlayer] = useState<any>(null);
  const [isPaused, setPaused] = useState(false);
  const [isActive, setActive] = useState(false);
  const [currentTrack, setCurrentTrack] = useState<any>(null);
  const [isPremium, setIsPremium] = useState<boolean | null>(null);
  const [token, setToken] = useState<string>('');

  useEffect(() => {
    async function init() {
      try {
        const statusData = await apiClient.getStatus();
        if (statusData.product !== 'premium') {
          setIsPremium(false);
          return;
        }
        setIsPremium(true);

        const tokenData = await apiClient.getToken();
        setToken(tokenData.access_token);
      } catch (err) {
        console.error('Failed to init playback info', err);
      }
    }
    init();
  }, []);

  useEffect(() => {
    if (!isPremium || !token) return;

    const script = document.createElement("script");
    script.src = "https://sdk.scdn.co/spotify-player.js";
    script.async = true;

    document.body.appendChild(script);

    window.onSpotifyWebPlaybackSDKReady = () => {
      const playerInstance = new window.Spotify.Player({
        name: 'Spoti2.0 Web Player',
        getOAuthToken: (cb: (token: string) => void) => { cb(token); },
        volume: 0.5
      });

      playerInstance.addListener('ready', ({ device_id }: { device_id: string }) => {
        console.log('Ready with Device ID', device_id);
      });

      playerInstance.addListener('not_ready', ({ device_id }: { device_id: string }) => {
        console.log('Device ID has gone offline', device_id);
      });

      playerInstance.addListener('player_state_changed', (state: any) => {
        if (!state) return;
        setCurrentTrack(state.track_window.current_track);
        setPaused(state.paused);

        playerInstance.getCurrentState().then((s: any) => {
           (!s) ? setActive(false) : setActive(true);
        });
      });

      playerInstance.connect();
      setPlayer(playerInstance);
    };

    return () => {
      if (player) {
         player.disconnect();
      }
    };
  }, [isPremium, token]);

  if (isPremium === null) return null;

  if (isPremium === false) {
    return (
      <div className="web-playback web-playback--free">
        <div className="web-playback__warning">
          <span>🎵 Spotify Premium Required</span>
          <p>Browser playback via the Spotify Web SDK requires a Premium account. Your account is on the free tier.</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`web-playback ${isActive ? 'web-playback--active' : ''}`}>
      <div className="web-playback__content">
        {!isActive ? (
          <div className="web-playback__state">
            Transfer playback to <strong>Spoti2.0 Web Player</strong> in your Spotify app.
          </div>
        ) : (
          <>
            <div className="web-playback__track-info">
              {currentTrack?.album?.images[0]?.url && (
                <img src={currentTrack.album.images[0].url} alt={currentTrack.name} className="web-playback__cover" />
              )}
              <div className="web-playback__text">
                <div className="web-playback__name">{currentTrack?.name}</div>
                <div className="web-playback__artist">
                  {currentTrack?.artists?.map((a: any) => a.name).join(', ')}
                </div>
              </div>
            </div>

            <div className="web-playback__controls">
              <button 
                className="web-playback__btn" 
                onClick={() => player.previousTrack()}
              >
                ⏮
              </button>
              <button 
                className="web-playback__btn web-playback__btn--play" 
                onClick={() => player.togglePlay()}
              >
                {isPaused ? "▶" : "⏸"}
              </button>
              <button 
                className="web-playback__btn" 
                onClick={() => player.nextTrack()}
              >
                ⏭
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
