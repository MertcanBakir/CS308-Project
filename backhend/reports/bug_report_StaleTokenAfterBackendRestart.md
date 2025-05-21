# Bug Report: Token Becomes Invalid After Backend Restart

## Description
If a user is already logged in and the backend server is restarted, the frontend still holds the token in `localStorage`. However, the backend no longer recognizes this token (e.g., due to lost in-memory state), resulting in `fetch error` on subsequent API calls. The only way to recover is for the user to manually logout and login again.

## Steps to Reproduce
1. Log in as a user.
2. Navigate to any page that requires authentication.
3. Restart the backend server.
4. Without logging out, try refreshing the profile or accessing any authenticated route.

## Expected Result
The frontend should either:
- Automatically detect that the token is no longer valid and log the user out,
- Or gracefully reauthenticate or refresh the token if applicable.

## Actual Result
The frontend keeps sending the old token. Since the backend has restarted, it cannot validate the token anymore. Every authenticated API call fails with a `fetch error`. The application stays broken until the user manually logs out and logs back in.

## Root Cause
The backend likely does not persist the token secret key or session information between restarts. Since JWT validation depends on this secret, all old tokens become invalid. But the frontend is unaware of this and keeps sending the old token.

## Fix
Two possible approaches:

**Backend-Side:**
- Persist the JWT signing key (e.g., store in application properties or keystore) so that tokens remain valid across restarts.

**Frontend-Side:**
- Add a global interceptor for API errors like 401/403.
- If an auth error occurs on token-protected requests, auto-clear the token and redirect to login.

## Impact
- Logged-in users get stuck after backend restarts.
- Requires manual logout → login to recover.
- Bad user experience and potential confusion.

## Status
**Open**

## Reported On
May 17, 2025

## Reported By
Efecan Kasapoğlu