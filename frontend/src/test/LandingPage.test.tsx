import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import LandingPage from '../pages/LandingPage';

// ── Mock AuthContext value ────────────────────────────────────────────────────

const mockAuth = {
  user:      null,
  isLoading: false,
  login:     vi.fn().mockResolvedValue(undefined),
  register:  vi.fn().mockResolvedValue(undefined),
  logout:    vi.fn().mockResolvedValue(undefined),
};

function renderLandingPage() {
  return render(
    <BrowserRouter>
      <AuthContext.Provider value={mockAuth}>
        <LandingPage />
      </AuthContext.Provider>
    </BrowserRouter>
  );
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe('LandingPage', () => {
  it('renders the login form by default', () => {
    renderLandingPage();

    // Heading
    expect(screen.getByRole('heading', { name: /welcome back/i })).toBeInTheDocument();
    // Inputs
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    // No email field in login mode
    expect(screen.queryByLabelText(/email/i)).not.toBeInTheDocument();
  });

  it('shows the register form when the Register tab is clicked', () => {
    renderLandingPage();

    const registerTab = screen.getByRole('tab', { name: /register/i });
    fireEvent.click(registerTab);

    expect(screen.getByRole('heading', { name: /create account/i })).toBeInTheDocument();
    // Email field is now visible
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  });

  it('switches back to login form when Log In tab is clicked', () => {
    renderLandingPage();

    // Go to register
    fireEvent.click(screen.getByRole('tab', { name: /register/i }));
    // Go back
    fireEvent.click(screen.getByRole('tab', { name: /log in/i }));

    expect(screen.getByRole('heading', { name: /welcome back/i })).toBeInTheDocument();
    expect(screen.queryByLabelText(/email/i)).not.toBeInTheDocument();
  });
});
