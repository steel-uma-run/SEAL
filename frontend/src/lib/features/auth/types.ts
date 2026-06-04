export type UserRole = 'GUEST' | 'STUDENT' | 'JUDGE' | 'MENTOR' | 'COORDINATOR';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING';

export interface UserProfile {
  id: string;
  email: string;
  full_name: string;
  role: UserRole;
  status: UserStatus;
  student_code?: string;
  student_type?: 'FPT' | 'EXTERNAL';
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
