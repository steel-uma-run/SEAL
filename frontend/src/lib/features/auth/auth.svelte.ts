import type { UserProfile } from './types';

class AuthState {
  user = $state<UserProfile | null>(null);
  token = $state<string | null>(null);
  isAuthenticated = $derived(!!this.user && !!this.token);

  login(userData: UserProfile, authToken: string) {
    this.user = userData;
    this.token = authToken;
    // TODO: Lưu token vào localStorage hoặc cookie nếu cần
  }

  logout() {
    this.user = null;
    this.token = null;
    // TODO: Xóa token khỏi localStorage hoặc cookie
  }
}

export const authState = new AuthState();
