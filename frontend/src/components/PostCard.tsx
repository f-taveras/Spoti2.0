import React, { useState, useEffect } from 'react';
import { apiClient } from '../services/apiClient';
import './PostCard.css';

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

interface Comment {
  id: number;
  content: string;
  createdAt: string;
  user: { username: string };
}

export default function PostCard({ post }: { post: PostResponse }) {
  const [likes, setLikes] = useState(post.likeCount);
  const [isLiked, setIsLiked] = useState(post.likedByCurrentUser);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');

  const handleLike = async () => {
    try {
      const responseText = await apiClient.toggleLike(post.id);
      if (responseText === 'Liked') {
        setLikes(l => l + 1);
        setIsLiked(true);
      } else {
        setLikes(l => l - 1);
        setIsLiked(false);
      }
    } catch(err) {
      console.error(err);
    }
  };

  const loadComments = async () => {
    try {
      const data = await apiClient.getComments(post.id);
      setComments(data);
    } catch(err) {
      console.error(err);
    }
  };

  const handleToggleComments = () => {
    if (!showComments) {
      loadComments();
    }
    setShowComments(!showComments);
  };

  const handlePostComment = async () => {
    if (!newComment.trim()) return;
    try {
      await apiClient.addComment(post.id, newComment);
      setNewComment('');
      loadComments(); // refresh
    } catch(err) {
      console.error(err);
    }
  };

  return (
    <div className="post-card">
      <div className="post-card__header">
        <div className="post-card__avatar">{post.username?.[0]?.toUpperCase()}</div>
        <div className="post-card__meta">
          <strong>{post.username}</strong>
          <span>{new Date(post.createdAt).toLocaleString()}</span>
        </div>
      </div>

      <div className="post-card__content">
        <p>{post.content}</p>
      </div>

      <div className="post-card__media">
        <img src={post.mediaArtUrl} alt={post.mediaName} className="post-card__media-art" />
        <div className="post-card__media-info">
          <span className="post-card__media-type">{post.mediaType}</span>
          <strong className="post-card__media-name">{post.mediaName}</strong>
          <span className="post-card__media-artist">{post.mediaArtist}</span>
        </div>
        {post.mediaType === 'TRACK' && (
          <button className="post-card__play" onClick={() => alert('Will launch SDK with ' + post.spotifyId)}>▶</button>
        )}
      </div>

      <div className="post-card__actions">
        <button className={`post-card__btn ${isLiked ? 'liked' : ''}`} onClick={handleLike}>
          {isLiked ? '❤️' : '🤍'} {likes}
        </button>
        <button className="post-card__btn" onClick={handleToggleComments}>
          💬 {post.commentCount + comments.length - (showComments ? comments.length : 0) /* naive visual adjust for now */}
        </button>
      </div>

      {showComments && (
        <div className="post-card__comments">
          <div className="post-card__comments-list">
            {comments.map(c => (
              <div key={c.id} className="post-card__comment">
                <strong>{c.user.username}</strong>
                <p>{c.content}</p>
                <span className="post-card__comment-time">{new Date(c.createdAt).toLocaleDateString()}</span>
              </div>
            ))}
          </div>
          <div className="post-card__comment-input-area">
            <input 
              type="text" 
              placeholder="Write a comment..." 
              value={newComment}
              onChange={e => setNewComment(e.target.value)}
              className="post-card__comment-input"
            />
            <button className="post-card__comment-submit" onClick={handlePostComment}>Post</button>
          </div>
        </div>
      )}
    </div>
  );
}
