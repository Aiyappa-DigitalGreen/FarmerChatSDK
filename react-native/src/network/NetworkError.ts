/**
 * NetworkError.ts
 * Error classes for network-layer failures.
 */

export class NetworkError extends Error {
  public readonly code: number | undefined;
  public readonly apiPath: string;

  constructor(
    code: number | undefined,
    apiPath: string,
    message: string,
  ) {
    super(message);
    this.name = 'NetworkError';
    this.code = code;
    this.apiPath = apiPath;
    // Maintain proper prototype chain in TypeScript
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Thrown when the server returns 401 and token refresh also fails. */
export class TokenExpiredError extends NetworkError {
  constructor(apiPath: string) {
    super(401, apiPath, 'Session expired. Please log in again.');
    this.name = 'TokenExpiredError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Thrown when there is no network connectivity. */
export class NetworkUnavailableError extends NetworkError {
  constructor(apiPath: string) {
    super(undefined, apiPath, 'No internet connection. Please check your network.');
    this.name = 'NetworkUnavailableError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Thrown for 4xx errors that are not auth-related. */
export class ApiError extends NetworkError {
  public readonly serverMessage?: string;

  constructor(code: number, apiPath: string, serverMessage?: string) {
    super(code, apiPath, serverMessage ?? `Request failed with status ${code}`);
    this.name = 'ApiError';
    this.serverMessage = serverMessage;
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Thrown for 5xx server errors. */
export class ServerError extends NetworkError {
  constructor(code: number, apiPath: string) {
    super(code, apiPath, 'Server error. Please try again later.');
    this.name = 'ServerError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}
