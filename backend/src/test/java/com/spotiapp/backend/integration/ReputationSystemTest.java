package com.spotiapp.backend.integration;

import com.spotiapp.backend.dto.CommentRequest;
import com.spotiapp.backend.dto.PostRequest;
import com.spotiapp.backend.model.MediaType;
import com.spotiapp.backend.model.Post;
import com.spotiapp.backend.model.User;
import com.spotiapp.backend.repository.PostRepository;
import com.spotiapp.backend.repository.UserRepository;
import com.spotiapp.backend.service.ReputationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ReputationSystemTest {

    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private ReputationService reputationService;
    @Autowired private com.spotiapp.backend.controller.PostController postController;

    @Test
    void testReputationGains() {
        // 1. Setup Users
        User alice = new User("alice", "alice@test.com", "pass");
        User bob = new User("bob", "bob@test.com", "pass");
        userRepository.save(alice);
        userRepository.save(bob);

        // 2. Alice creates a post
        PostRequest pr = new PostRequest();
        pr.setContent("Check this out!");
        pr.setMediaType(MediaType.TRACK);
        pr.setSpotifyId("track123");
        
        // Mock authentication for Alice
        org.springframework.security.core.Authentication aliceAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alice", "pass");
        
        Post alicePost = (Post) postController.createPost(pr, aliceAuth).getBody();

        // 3. Bob comments on Alice's post -> Alice gets Signal score
        CommentRequest cr = new CommentRequest();
        cr.setContent("Great track!");
        
        org.springframework.security.core.Authentication bobAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("bob", "pass");
        
        postController.addComment(alicePost.getId(), cr, bobAuth);
        
        User updatedAlice = userRepository.findByUsername("alice").get();
        assertEquals(2, updatedAlice.getReputation().getSignalScore());

        // 4. Bob shares Alice's track from her post -> Alice gets Curator score
        PostRequest shareRequest = new PostRequest();
        shareRequest.setContent("Sharing from Alice");
        shareRequest.setMediaType(MediaType.TRACK);
        shareRequest.setSpotifyId("track123");
        shareRequest.setSourcePostId(alicePost.getId());
        
        postController.createPost(shareRequest, bobAuth);
        
        updatedAlice = userRepository.findByUsername("alice").get();
        assertEquals(5, updatedAlice.getReputation().getCuratorScore());
    }
}
