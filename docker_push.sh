tag="$(date '+%Y%m%d%H%M')"
docker build -t mhewedy/log-viewer:build-"$tag" . && \
docker push mhewedy/log-viewer:build-"$tag"
