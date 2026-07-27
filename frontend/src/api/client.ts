import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";

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

function maskSensitiveData(value: unknown): unknown {
  if (Array.isArray(value)) {
    return value.map(maskSensitiveData);
  }

  if (!value || typeof value !== "object") {
    return value;
  }

  return Object.fromEntries(
    Object.entries(value).map(([key, nestedValue]) => {
      if (/password|token|secret/i.test(key)) {
        return [key, "***masked***"];
      }

      return [key, maskSensitiveData(nestedValue)];
    })
  );
}

function toRequestUrl(config: InternalAxiosRequestConfig): string {
  const baseURL = config.baseURL && config.baseURL.trim() ? config.baseURL : window.location.origin;
  const requestUrl = config.url ?? "";

  return new URL(requestUrl, baseURL).toString();
}

export const apiClient = axios.create({
  // baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json"
  }
});

apiClient.interceptors.request.use((config) => {
  console.debug("[api:request]", {
    method: config.method?.toUpperCase() ?? "GET",
    url: toRequestUrl(config),
    payload: maskSensitiveData(config.data)
  });

  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    console.debug("[api:response]", {
      method: response.config.method?.toUpperCase() ?? "GET",
      url: toRequestUrl(response.config),
      status: response.status,
      data: maskSensitiveData(response.data)
    });

    return response;
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    const data = (error.response?.data ?? {}) as Partial<ApiError>;

    console.error("[api:error]", {
      method: error.config?.method?.toUpperCase() ?? "GET",
      url: error.config ? toRequestUrl(error.config as InternalAxiosRequestConfig) : undefined,
      status,
      response: maskSensitiveData(error.response?.data),
      message: error.message
    });

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