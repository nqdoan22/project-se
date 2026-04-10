import { createContext, useContext } from 'react';

const AuthContext = createContext({
  authenticated: false,
  username: '',
  roles: [],
  logout: () => {},
});

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
export default AuthContext;
