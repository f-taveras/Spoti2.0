import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import PlaylistGrid from '../components/PlaylistGrid';

// Mock apiClient to prevent real network requests
vi.mock('../services/apiClient', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

import { apiClient } from '../services/apiClient';

describe('PlaylistGrid Component', () => {
  it('renders a loading state initially', () => {
    // Make the mock return an unresolved promise to keep it in loading state
    (apiClient.get as any).mockReturnValue(new Promise(() => {}));

    render(
      <MemoryRouter>
        <PlaylistGrid />
      </MemoryRouter>
    );
    expect(screen.getByText(/loading playlists/i)).toBeInTheDocument();
  });

  it('renders an empty state when no playlists exist', async () => {
    (apiClient.get as any).mockResolvedValue({ items: [] });

    render(
      <MemoryRouter>
        <PlaylistGrid />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/no playlists found/i)).toBeInTheDocument();
    });
  });

  it('renders a list of playlists correctly', async () => {
    const mockData = {
      items: [
        {
          id: '1',
          name: 'Chill Vibes',
          owner: { display_name: 'Spotify' },
          images: [{ url: 'https://example.com/chill.jpg' }],
        },
        {
          id: '2',
          name: 'Workout Mix',
          owner: { display_name: 'User1' },
          images: [], // Test fallback image handling
        },
      ],
    };

    (apiClient.get as any).mockResolvedValue(mockData);

    render(
      <MemoryRouter>
        <PlaylistGrid />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Chill Vibes')).toBeInTheDocument();
      expect(screen.getByText('Spotify')).toBeInTheDocument();
      expect(screen.getByText('Workout Mix')).toBeInTheDocument();
      expect(screen.getByText('User1')).toBeInTheDocument();
    });
  });

  it('renders an error message on failure', async () => {
    (apiClient.get as any).mockRejectedValue(new Error('Failed to fetch'));

    render(
      <MemoryRouter>
        <PlaylistGrid />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/could not load playlists/i)).toBeInTheDocument();
    });
  });
});
