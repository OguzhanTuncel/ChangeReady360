export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserInfo {
  id: number;
  email: string;
  role: string;
  companyId?: number;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  userInfo: UserInfo;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: UserInfo | null;
  token: string | null;
}

