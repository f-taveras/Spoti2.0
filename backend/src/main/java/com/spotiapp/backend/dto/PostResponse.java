package com.spotiapp.backend.dto;

import com.spotiapp.backend.model.MediaType;
import java.time.LocalDateTime;

public class PostResponse {
    private Long id;
    private String username;
    private String content;
    private MediaType mediaType;
    private String spotifyId;
    private String mediaName;
    private String mediaArtist;
    private String mediaArtUrl;
    private LocalDateTime createdAt;
    private long likeCount;
    private int commentCount;
    private boolean likedByCurrentUser;
    private String userProfileImageUrl;
    private int curatorScore;
    private int signalScore;

    public PostResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }
    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }

    public String getMediaName() { return mediaName; }
    public void setMediaName(String mediaName) { this.mediaName = mediaName; }

    public String getMediaArtist() { return mediaArtist; }
    public void setMediaArtist(String mediaArtist) { this.mediaArtist = mediaArtist; }

    public String getMediaArtUrl() { return mediaArtUrl; }
    public void setMediaArtUrl(String mediaArtUrl) { this.mediaArtUrl = mediaArtUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public String getUserProfileImageUrl() { return userProfileImageUrl; }
    public void setUserProfileImageUrl(String userProfileImageUrl) { this.userProfileImageUrl = userProfileImageUrl; }

    public int getCuratorScore() { return curatorScore; }
    public void setCuratorScore(int curatorScore) { this.curatorScore = curatorScore; }

    public int getSignalScore() { return signalScore; }
    public void setSignalScore(int signalScore) { this.signalScore = signalScore; }
}
