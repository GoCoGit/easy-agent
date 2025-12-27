import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";

const baseURL = "http://localhost:8881"

const instance = axios.create({
  // 如果后面需要，可以在这里统一配置 baseURL、超时时间、headers 等
  // baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
  baseURL: baseURL,
});

// 这里可以按需添加请求/响应拦截器
// instance.interceptors.request.use(
//   (config) => {
//     // 例如在这里统一加 token
//     return config;
//   },
//   (error) => Promise.reject(error)
// );
//
// instance.interceptors.response.use(
//   (response) => response,
//   (error) => Promise.reject(error)
// );

export const request = {
  get<T = any>(
    url: string,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>> {
    return instance.get<T>(url, config);
  },

  post<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>> {
    return instance.post<T>(url, data, config);
  },
};

export { request as default, baseURL };


