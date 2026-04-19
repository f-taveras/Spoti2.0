/**
 * Typed fetch wrapper — always sends credentials (HttpOnly cookie)
 * and handles non-OK responses by throwing structured errors.
 */

// In production (Vercel), VITE_API_BASE_URL points to the Railway backend.
// In local dev, it's empty so the Vite proxy (/api → localhost:8080) handles requests.
const BASE = import.meta.env.VITE_API_BASE_URL ?? '';

interface ApiError {
  message: string;
  status: number;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    ...options,
    credentials: 'include',              // Always send the auth-token cookie
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    let message = `Request failed: ${response.status}`;
    try {
      const body = await response.json();
      message = body.message ?? message;
    } catch { /* response may not be JSON */ }
    const err: ApiError = { message, status: response.status };
    throw err;
  }

  // 204 No Content — return undefined
  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body: unknown) => request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),

  /**
   * Securely fetch the user's Spotify access token.
   */
  getToken: async () => {
    return request<{ access_token: string }>('/api/spotify/token');
  },

  /**
   * Fetch connected Spotify user's profile status.
   */
  getStatus: async () => {
    return request<{ product: string }>('/api/spotify/status');
  },

  /**
   * --- Town Square Social Endpoints ---
   */
  getPosts: async () => {
    return request<any[]>('/api/posts');
  },
  createPost: async (payload: { content: string, mediaType: 'TRACK' | 'PLAYLIST', spotifyId: string, sourcePostId?: number }) => {
    return request<any>('/api/posts', { method: 'POST', body: JSON.stringify(payload) });
  },
  toggleLike: async (postId: number) => {
    return request<string>(`/api/posts/${postId}/like`, { method: 'POST', body: JSON.stringify({}) });
  },
  addComment: async (postId: number, content: string) => {
    return request<any>(`/api/posts/${postId}/comments`, { method: 'POST', body: JSON.stringify({ content }) });
  },
  getComments: async (postId: number) => {
    return request<any[]>(`/api/posts/${postId}/comments`);
  },
  cachePlaylist: async (payload: { spotifyId: string, name: string, ownerName: string, coverArtUrl: string }) => {
    return request<any>('/api/playlists/cache', { method: 'POST', body: JSON.stringify(payload) });
  },
  cacheTrack: async (payload: { spotifyId: string, name: string, artistName: string, albumArtUrl: string }) => {
    return request<any>('/api/tracks/cache', { method: 'POST', body: JSON.stringify(payload) });
  },

  /**
   * --- Profile & Social Endpoints ---
   */
  getProfile: async (username: string) => {
    return request<any>(`/api/profiles/${username}`);
  },
  updateProfile: async (updates: any) => {
    return request<any>('/api/profiles/me', { method: 'PUT', body: JSON.stringify(updates) });
  },
  toggleFollow: async (username: string) => {
    return request<{ message: string, following: boolean }>(`/api/social/follow/${username}`, { method: 'POST', body: JSON.stringify({}) });
  },
  checkFollowing: async (username: string) => {
    return request<{ following: boolean }>(`/api/social/is-following/${username}`);
  },
};
