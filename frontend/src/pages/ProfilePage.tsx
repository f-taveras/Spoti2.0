import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { apiClient } from '../services/apiClient';
import { useAuth } from '../context/AuthContext';
import './ProfilePage.css';

interface ProfileData {
  id: number;
  username: string;
  displayName: string;
  bio: string;
  location: string;
  avatarUrl: string;
  favoriteGenres: string[];
  favoriteArtists: string[];
  tasteScore: number;
  curatorScore: number;
  signalScore: number;
  postCount: number;
  followersCount: number;
  followingCount: number;
}

export default function ProfilePage() {
  const { username } = useParams<{ username: string }>();
  const { user: currentUser } = useAuth();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [loading, setLoading] = useState(true);

  const isOwner = currentUser?.username?.toLowerCase() === username?.toLowerCase();

  useEffect(() => {
    if (username) {
      loadProfile(username);
      if (currentUser && !isOwner) {
        checkFollowStatus(username);
      }
    }
  }, [username, currentUser]);

  const loadProfile = async (name: string) => {
    try {
      setErrorMsg(null);
      const data = await apiClient.getProfile(name);
      setProfile(data);
    } catch (err: any) {
      console.error('Failed to load profile', err);
      // Try to extract backend error message
      const msg = err.body?.error || `Profile for "${name}" not found.`;
      setErrorMsg(msg);
    } finally {
      setLoading(false);
    }
  };

  const checkFollowStatus = async (name: string) => {
    try {
      const { following } = await apiClient.checkFollowing(name);
      setIsFollowing(following);
    } catch (err) {
      console.error('Failed to check follow status', err);
    }
  };

  const handleFollow = async () => {
    if (!username || !currentUser) return;
    try {
      const { following } = await apiClient.toggleFollow(username);
      setIsFollowing(following);
      loadProfile(username); // refresh counts
    } catch (err) {
      console.error('Failed to follow', err);
    }
  };

  if (loading) return <div className="loading">Loading Profile...</div>;
  if (errorMsg) return <div className="error-container"><h1>Oops!</h1><p>{errorMsg}</p></div>;
  if (!profile) return <div className="error">Profile not found</div>;

  return (
    <div className="profile-page">
      <header className="profile-header">
        <div className="profile-header__main">
          <div className="profile-avatar">
            {profile.avatarUrl ? (
              <img src={profile.avatarUrl} alt={profile.username} className="profile-avatar__img" />
            ) : (
              <div className="profile-avatar__placeholder">{profile.username[0].toUpperCase()}</div>
            )}
          </div>
          <div className="profile-info">
            <h1 className="profile-info__name">{profile.displayName || profile.username}</h1>
            <span className="profile-info__username">@{profile.username}</span>
            <div className="profile-info__badges">
              <span className="badge">Curator: {profile.curatorScore}</span>
              <span className="badge">Taste: {profile.tasteScore}</span>
              <span className="badge">Signal: {profile.signalScore}</span>
            </div>
            {profile.bio && <p className="profile-info__bio">{profile.bio}</p>}
            {profile.location && <span className="profile-info__location">📍 {profile.location}</span>}
          </div>
          <div className="profile-actions">
            {currentUser && !isOwner && (
              <button onClick={handleFollow} className={`btn-follow ${isFollowing ? 'following' : ''}`}>
                {isFollowing ? 'Following' : 'Follow'}
              </button>
            )}
            {isOwner && (
              <button className="btn-edit" onClick={() => alert('Editing coming soon!')}>
                Edit Profile
              </button>
            )}
          </div>
        </div>

        <div className="profile-stats">
          <div className="stat"><strong>{profile.postCount}</strong><span>Posts</span></div>
          <div className="stat"><strong>{profile.followersCount}</strong><span>Followers</span></div>
          <div className="stat"><strong>{profile.followingCount}</strong><span>Following</span></div>
        </div>
      </header>

      <div className="profile-body">
        <section className="profile-section">
          <h2>Musical Taste</h2>
          <div className="taste-tags">
            {profile.favoriteGenres?.map(g => <span key={g} className="tag tag--genre">{g}</span>)}
            {profile.favoriteArtists?.map(a => <span key={a} className="tag tag--artist">{a}</span>)}
          </div>
          {(!profile.favoriteGenres || profile.favoriteGenres.length === 0) && <p className="empty-msg">No taste tags added yet.</p>}
        </section>

        <section className="profile-section">
          <h2>Recent Activity</h2>
          <p className="empty-msg">Activity feed coming soon...</p>
        </section>
      </div>
    </div>
  );
}
