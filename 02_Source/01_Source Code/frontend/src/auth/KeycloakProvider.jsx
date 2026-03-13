import { useEffect, useState } from 'react';
import keycloak from './keycloak';

const KEYCLOAK_TIMEOUT_MS = 3000;

export default function KeycloakProvider({ children }) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const timeout = new Promise((_, reject) =>
      setTimeout(() => reject(new Error('Keycloak timeout')), KEYCLOAK_TIMEOUT_MS)
    );

    Promise.race([
      keycloak.init({
        onLoad: 'check-sso',
        checkLoginIframe: false,
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      }),
      timeout,
    ])
      .then((authenticated) => {
        if (authenticated) {
          setReady(true);
        } else {
          keycloak.login();
        }
      })
      .catch(() => {
        setReady(true);
      });
  }, []);

  if (!ready) return <div style={{ padding: 40 }}>Loading...</div>;
  return children;
}
