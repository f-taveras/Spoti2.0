package com.spotiapp.backend.controller;

import com.spotiapp.backend.dto.CommentRequest;
import com.spotiapp.backend.dto.PostRequest;
import com.spotiapp.backend.dto.PostResponse;
import com.spotiapp.backend.model.Comment;
import com.spotiapp.backend.model.*;
import com.spotiapp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private CachedPlaylistRepository cachedPlaylistRepository;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(Authentication authentication) {
        if (authentication == null) {
            logger.warn("Unauthenticated request to GET /api/posts");
            return ResponseEntity.status(403).build();
        }
        
        String username = authentication.getName();
        logger.info("Fetching feed for user: {}", username);
        
        User currentUser = userRepository.findByUsername(username)
            .orElseGet(() -> {
                logger.error("User not found in DB but authenticated: {}", username);
                return null;
            });
            
        if (currentUser == null) return ResponseEntity.status(403).build();

        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponse> responses = posts.stream().map(post -> {
            PostResponse res = new PostResponse();
            res.setId(post.getId());
            res.setUsername(post.getUser().getUsername());
            res.setContent(post.getContent());
            res.setMediaType(post.getMediaType());
            res.setSpotifyId(post.getSpotifyId());
            res.setCreatedAt(post.getCreatedAt());
            res.setLikeCount(reactionRepository.countByPostId(post.getId()));
            res.setCommentCount(post.getComments().size());
            
            boolean liked = reactionRepository.findByPostIdAndUserId(post.getId(), currentUser.getId()).isPresent();
            res.setLikedByCurrentUser(liked);
            
            if (post.getMediaType() == MediaType.TRACK) {
                trackRepository.findBySpotifyId(post.getSpotifyId()).ifPresent(track -> {
                    res.setMediaName(track.getName());
                    res.setMediaArtist(track.getArtistName());
                    res.setMediaArtUrl(track.getAlbumArtUrl());
                });
            } else if (post.getMediaType() == MediaType.PLAYLIST) {
                cachedPlaylistRepository.findBySpotifyId(post.getSpotifyId()).ifPresent(playlist -> {
                    res.setMediaName(playlist.getName());
                    res.setMediaArtist(playlist.getOwnerName());
                    res.setMediaArtUrl(playlist.getCoverArtUrl());
                });
            }

            return res;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest request, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(403).build();
        String username = authentication.getName();
        logger.info("User {} is creating a post: {}", username, request.getMediaType());
        
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            logger.error("CreatePost failed: User {} not found", username);
            return ResponseEntity.status(403).build();
        }

        Post newPost = new Post(currentUser, request.getContent(), request.getMediaType(), request.getSpotifyId());
        Post saved = postRepository.save(newPost);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editPost(@PathVariable Long id, @RequestBody PostRequest request, Authentication authentication) {
        String username = authentication.getName();
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();
        
        if (!post.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(403).body("You can only edit your own posts");
        }

        post.setContent(request.getContent());
        postRepository.save(post);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        if (!post.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(403).body("You can only delete your own posts");
        }

        postRepository.delete(post);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        Optional<Reaction> existing = reactionRepository.findByPostIdAndUserId(postId, currentUser.getId());
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            return ResponseEntity.ok("Unliked");
        } else {
            Reaction reaction = new Reaction(post, currentUser);
            reactionRepository.save(reaction);
            return ResponseEntity.ok("Liked");
        }
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody CommentRequest request, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        Comment comment = new Comment(post, currentUser, request.getContent());
        Comment saved = commentRepository.save(comment);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return ResponseEntity.ok(comments);
    }
}
