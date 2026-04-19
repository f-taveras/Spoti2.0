import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProfilePage from '../pages/ProfilePage';
import { apiClient } from '../services/apiClient';
import { AuthProvider } from '../context/AuthContext';
import { vi, describe, it, expect, beforeEach } from 'vitest';

// Mock the apiClient
vi.mock('../services/apiClient', () => ({
  apiClient: {
    get: vi.fn(() => Promise.resolve(null)),
    getProfile: vi.fn(),
    checkFollowing: vi.fn()
  }
}));

describe('ProfilePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderProfile = (username: string) => {
    return render(
      <MemoryRouter initialEntries={[`/profile/${username}`]}>
        <AuthProvider>
          <Routes>
            <Route path="/profile/:username" element={<ProfilePage />} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );
  };

  it('renders "Profile not found" when the API returns an error', async () => {
    // Arrange: Mock API to throw
    (apiClient.getProfile as any).mockRejectedValue({ body: { error: 'Not found' }, status: 404 });

    renderProfile('missinguser');

    // Assert: Check for loading first
    expect(screen.getByText(/Loading Profile/i)).toBeInTheDocument();

    // Assert: Check for error message
    await waitFor(() => {
      expect(screen.getByText(/Not found/i)).toBeInTheDocument();
    });
  });

  it('renders profile data when API succeeds', async () => {
    // Arrange: Mock API success
    const mockProfile = {
      username: 'testuser',
      displayName: 'Test User',
      favoriteGenres: ['Rock', 'Jazz'],
      favoriteArtists: [],
      curatorScore: 5,
      tasteScore: 10,
      signalScore: 3,
      postCount: 1,
      followersCount: 2,
      followingCount: 0
    };
    (apiClient.getProfile as any).mockResolvedValue(mockProfile);
    (apiClient.checkFollowing as any).mockResolvedValue({ following: true });

    renderProfile('testuser');

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/Test User/i)).toBeInTheDocument();
      expect(screen.getByText(/@testuser/i)).toBeInTheDocument();
      expect(screen.getByText(/Rock/i)).toBeInTheDocument();
    });
  });
});
