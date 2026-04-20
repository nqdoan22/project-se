FROM quay.io/keycloak/keycloak:24.0

ENV KC_DB=dev-file
ENV KC_HEALTH_ENABLED=true
ENV KC_HTTP_ENABLED=true
ENV KC_HOSTNAME_STRICT=false
ENV KC_PROXY=edge

COPY ims-realm.json /opt/keycloak/data/import/ims-realm.json

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--import-realm"]
