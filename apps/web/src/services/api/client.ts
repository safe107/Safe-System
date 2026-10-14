import type { ApiErrorBody } from "../../types/content";
export class ApiError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorBody,
  ) {
    super(body.message || `HTTP ${status}`);
  }
}
const base = import.meta.env.VITE_API_URL || "";
let token: string | undefined;
export async function csrf(): Promise<string> {
  const res = await fetch(`${base}/api/v1/auth/csrf`, {
    credentials: "include",
  });
  if (!res.ok)
    throw new ApiError(res.status, {
      message: "Falha ao iniciar sessão segura",
    });
  const data = (await res.json()) as { token: string };
  token = data.token;
  return data.token;
}
export function clearCsrf() {
  token = undefined;
}
export async function api<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const method = options.method || "GET";
  const headers = new Headers(options.headers);
  if (options.body && !(options.body instanceof FormData))
    headers.set("Content-Type", "application/json");
  if (!["GET", "HEAD", "OPTIONS"].includes(method))
    headers.set("X-CSRF-TOKEN", token || (await csrf()));
  const res = await fetch(`${base}/api/v1${path}`, {
    ...options,
    headers,
    credentials: "include",
  });
  if (!res.ok) {
    let body: ApiErrorBody;
    try {
      body = (await res.json()) as ApiErrorBody;
    } catch {
      body = { message: `HTTP ${res.status}` };
    }
    throw new ApiError(res.status, body);
  }
  if (res.status === 204 || res.headers.get("content-length") === "0")
    return undefined as T;
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}
export function downloadUrl(fileId: string, admin = false, inline = false) {
  return `${base}/api/v1/${admin ? "admin" : "public"}/files/${encodeURIComponent(fileId)}/download?inline=${inline}`;
}
