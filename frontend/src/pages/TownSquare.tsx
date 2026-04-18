import React, { useEffect, useState } from 'react';
import { apiClient } from '../services/apiClient';
import NavBar from '../components/NavBar';
import PostCard from '../components/PostCard';
import WebPlayback from '../components/WebPlayback';
import './TownSquare.css';

interface PostResponse {
  id: number;
  username: string;
  content: string;
  mediaType: string;
  spotifyId: string;
  mediaName: string;
  mediaArtist: string;
  mediaArtUrl: string;
  createdAt: string;
  likeCount: number;
  commentCount: number;
  likedByCurrentUser: boolean;
}

export default function TownSquare() {
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadFeed() {
      try {
        const data = await apiClient.getPosts();
        setPosts(data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    }
    loadFeed();
  }, []);

  return (
    <div className="town-square">
      <NavBar />
      <div className="town-square__main">
        <h1 className="town-square__title">Town Square</h1>
        <p className="town-square__subtitle">See what others are listening to.</p>

        {loading ? (
          <div className="town-square__loading">Loading feed...</div>
        ) : (
          <div className="town-square__feed">
            {posts.length === 0 ? (
              <div className="town-square__empty">No posts yet. Be the first to share a track!</div>
            ) : (
              posts.map(post => (
                <PostCard key={post.id} post={post} />
              ))
            )}
          </div>
        )}
      </div>
      <WebPlayback />
    </div>
  );
}
