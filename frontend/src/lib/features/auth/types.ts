export type UserRole = 'GUEST' | 'STUDENT' | 'JUDGE' | 'MENTOR' | 'COORDINATOR';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING';

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  status: UserStatus;
  studentCode?: string;
  studentType?: 'FPT' | 'EXTERNAL';
}

export interface LoginResponse {
  token: string;
  user: UserProfile;
  message?: string;
}

export interface RegisterResponse {
  success: boolean;
  message?: string;
}
