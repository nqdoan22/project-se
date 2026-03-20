import { createContext, useContext } from 'react';

const AuthContext = createContext({
  authenticated: false,
  username: '',
  roles: [],
  logout: () => {},
});

export const useAuth = () => useContext(AuthContext);
export default AuthContext;
