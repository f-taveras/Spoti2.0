import React, { useState } from 'react';
import { apiClient } from '../services/apiClient';
import './ShareModal.css';

interface ShareModalProps {
  isOpen: boolean;
  onClose: () => void;
  mediaType: 'TRACK' | 'PLAYLIST';
  spotifyId: string;
  mediaName: string;
  mediaArtist: string;
  mediaArtUrl: string;
  sourcePostId?: number;
}

export default function ShareModal({ isOpen, onClose, mediaType, spotifyId, mediaName, mediaArtist, mediaArtUrl, sourcePostId }: ShareModalProps) {
  const [content, setContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleShare = async () => {
    setIsSubmitting(true);
    try {
      // 1. Ensure the Media is cached on our Backend
      if (mediaType === 'TRACK') {
        await apiClient.cacheTrack({ spotifyId, name: mediaName, artistName: mediaArtist, albumArtUrl: mediaArtUrl });
      } else {
        await apiClient.cachePlaylist({ spotifyId, name: mediaName, ownerName: mediaArtist, coverArtUrl: mediaArtUrl });
      }

      // 2. Create the Post
      await apiClient.createPost({ content, mediaType, spotifyId, sourcePostId });
      
      onClose();
      setContent('');
    } catch (err) {
      console.error("Failed to share post", err);
      alert("Failed to share. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="share-modal__overlay">
      <div className="share-modal">
        <button className="share-modal__close" onClick={onClose}>×</button>
        <h2 className="share-modal__title">Share to Town Square</h2>
        
        <div className="share-modal__media">
          <img src={mediaArtUrl} alt={mediaName} className="share-modal__media-art" />
          <div className="share-modal__media-info">
            <strong>{mediaName}</strong>
            <span>{mediaArtist}</span>
            <span className="share-modal__media-type">{mediaType}</span>
          </div>
        </div>

        <textarea 
          className="share-modal__input"
          placeholder="Say something about this..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          maxLength={2000}
        />

        <div className="share-modal__actions">
          <button className="share-modal__cancel" onClick={onClose}>Cancel</button>
          <button className="share-modal__submit" onClick={handleShare} disabled={isSubmitting || !content.trim()}>
            {isSubmitting ? 'Posting...' : 'Post to Town Square'}
          </button>
        </div>
      </div>
    </div>
  );
}
