FROM quay.io/keycloak/keycloak:24.0

COPY ims-realm.json /opt/keycloak/data/import/ims-realm.json

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev", "--import-realm"]
