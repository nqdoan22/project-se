import { useEffect, useState } from 'react';
import keycloak from './keycloak';
import AuthContext from './AuthContext';

const KEYCLOAK_TIMEOUT_MS = 3000;

export default function KeycloakProvider({ children }) {
  const [ready, setReady] = useState(false);
  const [auth, setAuth] = useState({
    authenticated: false,
    username: '',
    roles: [],
  });

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
          setAuth({
            authenticated: true,
            username: keycloak.tokenParsed?.preferred_username || '',
            roles: keycloak.tokenParsed?.realm_access?.roles || [],
          });
          setReady(true);
        } else {
          keycloak.login();
        }
      })
      .catch(() => {
        setReady(true);
      });
  }, []);

  const logout = () => keycloak.logout();

  if (!ready) return <div style={{ padding: 40 }}>Loading...</div>;

  return (
    <AuthContext.Provider value={{ ...auth, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
