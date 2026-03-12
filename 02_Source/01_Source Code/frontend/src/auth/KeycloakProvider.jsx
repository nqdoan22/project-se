import { useEffect, useState } from 'react';
import keycloak from './keycloak';

export default function KeycloakProvider({ children }) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
    }).then((authenticated) => {
      if (authenticated) setReady(true);
    });
  }, []);

  if (!ready) return <div style={{ padding: 40 }}>Authenticating...</div>;
  return children;
}
