package com.spotiapp.backend.service;

import com.spotiapp.backend.model.User;
import com.spotiapp.backend.model.UserProfile;
import com.spotiapp.backend.repository.FollowerRepository;
import com.spotiapp.backend.repository.PostRepository;
import com.spotiapp.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;
    @Mock private FollowerRepository followerRepository;

    @InjectMocks private ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFullProfile_Success_CaseInsensitive() {
        // Arrange
        String username = "JohnDoe";
        User user = new User();
        user.setId(1L);
        user.setUsername("johndoe"); // lowercase in DB
        
        UserProfile profile = new UserProfile(user);
        profile.setDisplayName("John Doe");
        user.setProfile(profile);

        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));
        when(postRepository.countByUser(user)).thenReturn(5L);
        when(followerRepository.countByFollowing(user)).thenReturn(10L);
        when(followerRepository.countByFollower(user)).thenReturn(15L);

        // Act
        Map<String, Object> result = profileService.getFullProfile(username);

        // Assert
        assertEquals("johndoe", result.get("username"));
        assertEquals("John Doe", result.get("displayName"));
        assertEquals(5L, result.get("postCount"));
        assertEquals(10L, result.get("followersCount"));
        assertEquals(15L, result.get("followingCount"));
        verify(userRepository).findByUsernameIgnoreCase(username);
    }

    @Test
    void testGetFullProfile_UserNotFound() {
        // Arrange
        String username = "NonExistent";
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> profileService.getFullProfile(username));
    }
}
