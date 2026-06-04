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

  // Kiểm tra lỗi HTTP TRƯỚC KHI parse JSON
  if (!response.ok) {
    const text = await response.text();
    let errorMessage = 'Tài khoản hoặc mật khẩu không chính xác';
    if (text) {
      try {
        const errorData = JSON.parse(text);
        errorMessage = errorData.message || errorMessage;
      } catch (e) {
        // Rơi vào đây nếu server trả về chuỗi rỗng hoặc HTML (không phải JSON)
        errorMessage = `Lỗi hệ thống từ server (Status: ${response.status})`;
      }
    }
    throw new Error(errorMessage);
  }
  
  return await response.json();
}

export async function register(params: URLSearchParams): Promise<RegisterResponse> {
  const response = await fetch(`${API_BASE_URL}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params
  });

  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || 'Lỗi đăng ký');
  }

  return { success: true, message: 'Đăng ký thành công' };
}
