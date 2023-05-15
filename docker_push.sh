DOCKER_BUILDKIT=0 docker build -t mhewedy/log-viewer:build-"$(date '+%Y%m%d')" . && \
docker push mhewedy/log-viewer:build-"$(date '+%Y%m%d')"
