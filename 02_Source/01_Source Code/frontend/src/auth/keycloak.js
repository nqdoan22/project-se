import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:9090',
  realm: 'ims',
  clientId: 'ims-frontend',
});

export default keycloak;
