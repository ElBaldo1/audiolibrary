import { defineConfig } from 'vitest/config';
import { loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const apiUrl = loadEnv(mode, process.cwd(), 'VITE_').VITE_API_BASE_URL?.trim();
  let apiOrigin = '';
  if (apiUrl) {
    const parsed = new URL(apiUrl);
    if (!['http:', 'https:'].includes(parsed.protocol) || parsed.username || parsed.password) {
      throw new Error('VITE_API_BASE_URL must be an HTTP(S) URL without credentials.');
    }
    apiOrigin = parsed.origin;
  }
  // Inline styles support Bootstrap and Toastify; scripts cannot use inline code or eval.
  const policy = [
    "default-src 'self'",
    "script-src 'self'",
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data:",
    "font-src 'self'",
    `connect-src 'self' ${apiOrigin}`.trim(),
    "media-src 'self' blob: data: https://samplelib.com",
    "object-src 'none'",
    "base-uri 'none'",
    "form-action 'none'",
  ].join('; ');
  return {
    plugins: [react(), {
      name: 'production-content-security-policy',
      apply: 'build',
      transformIndexHtml() {
        return [{ tag: 'meta', attrs: { 'http-equiv': 'Content-Security-Policy', content: policy }, injectTo: 'head-prepend' }];
      },
    }],
    server: { port: 3000, strictPort: true },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: './src/setupTests.ts',
      restoreMocks: true,
      unstubEnvs: true,
    },
  };
});
