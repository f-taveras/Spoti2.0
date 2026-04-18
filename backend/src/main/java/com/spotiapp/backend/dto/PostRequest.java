package com.spotiapp.backend.dto;

import com.spotiapp.backend.model.MediaType;

public class PostRequest {
    private String content;
    private MediaType mediaType;
    private String spotifyId;

    public PostRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }
}
