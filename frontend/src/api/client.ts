import axios, { AxiosError } from "axios";

export interface ApiError {
  status?: number;
  errorCode?: string;
  message: string;
  path?: string;
  timestamp?: string;
}

const statusMessageMap: Record<number, string> = {
  400: "Request is invalid. Please review your input.",
  404: "Requested resource was not found.",
  409: "The request conflicts with current payment state.",
  500: "Server error occurred. Please retry later."
};

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json"
  }
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status;
    const data = (error.response?.data ?? {}) as Partial<ApiError>;

    const mappedError: ApiError = {
      status,
      errorCode: data.errorCode,
      path: data.path,
      timestamp: data.timestamp,
      message:
        data.message ??
        (status ? statusMessageMap[status] : undefined) ??
        error.message ??
        "Unknown request error"
    };

    return Promise.reject(mappedError);
  }
);