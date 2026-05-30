import type { LoginResponse, RegisterResponse } from './types';

const API_BASE_URL = 'http://localhost:8080/api/auth';

export async function login(params: URLSearchParams): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || 'Tài khoản hoặc mật khẩu không chính xác');
  }
  
  return data;
}

export async function register(params: URLSearchParams): Promise<RegisterResponse> {
  const response = await fetch(`${API_BASE_URL}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || 'Lỗi đăng ký');
  }

  return { success: true, message: data.message };
}
