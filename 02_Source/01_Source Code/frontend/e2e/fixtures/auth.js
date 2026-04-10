import { test as base, expect } from '@playwright/test';

/**
 * Custom test fixture that bypasses Keycloak authentication.
 *
 * Aborts all requests to Keycloak so keycloak.init() rejects immediately,
 * triggering the catch branch in KeycloakProvider which sets ready=true
 * and renders the app without auth. Works because the dev backend
 * (spring.profiles.active=dev) uses DevSecurityConfig which permits all
 * requests without a JWT token.
 */
export const test = base.extend({
  page: async ({ page }, use) => {
    await page.route('**/realms/ims/**', (route) => route.abort());
    await page.route('**/silent-check-sso*', (route) => route.abort());
    await use(page);
  },
});

export { expect };
