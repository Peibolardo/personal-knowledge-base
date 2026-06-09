import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import { ref } from "vue";

interface UseAxiosOptions {
  baseURL?: string;
  headers?: Record<string, string>;
}

export function useAxios(options: UseAxiosOptions = {}) {
  const axiosInstance = axios.create({
    baseURL: options.baseURL || import.meta.env.VITE_API_URL,
    headers: options.headers || {
      "Content-type": "application/json; charset=UTF-8",
    },
  });

  const IS_LOADING = ref(false);
  const ERROR = ref("");
  const DATA = ref<any>(null);

  async function request<T>(
    config: AxiosRequestConfig,
  ): Promise<AxiosResponse<T>> {
    IS_LOADING.value = true;
    ERROR.value = "";

    try {
      const response = await axiosInstance.request<T>(config);
      DATA.value = response.data;
      return response;
    } catch (err: any) {
      ERROR.value = err.message || "An error occurred";
      throw err;
    } finally {
      IS_LOADING.value = false;
    }
  }

  return {
    IS_LOADING,
    ERROR,
    DATA,
    request,
    get<T>(
      url: string,
      config?: AxiosRequestConfig,
    ): Promise<AxiosResponse<T>> {
      return request<T>({ ...config, method: "get", url });
    },
    post<T>(
      url: string,
      data?: any,
      config?: AxiosRequestConfig,
    ): Promise<AxiosResponse<T>> {
      return request<T>({ ...config, method: "post", url, data });
    },
    put<T>(
      url: string,
      data?: any,
      config?: AxiosRequestConfig,
    ): Promise<AxiosResponse<T>> {
      return request<T>({ ...config, method: "put", url, data });
    },
    patch<T>(
      url: string,
      data?: any,
      config?: AxiosRequestConfig,
    ): Promise<AxiosResponse<T>> {
      return request<T>({ ...config, method: "patch", url, data });
    },
    delete<T>(
      url: string,
      config?: AxiosRequestConfig,
    ): Promise<AxiosResponse<T>> {
      return request<T>({ ...config, method: "delete", url });
    },
  };
}
