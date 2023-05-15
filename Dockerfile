FROM maven:3.8-openjdk-17-slim as build

MAINTAINER Mohammad Hewedy (mhewedy@gmail.com)

WORKDIR /build

COPY . .
RUN mvn package -DskipTests -U

RUN mv log-viewer-cli/target/log-viewer*.zip app.zip && \
    mv log-viewer-cli/bin/config.conf config.conf

FROM openjdk:17.0.2-bullseye as runtime

WORKDIR /usr/src
COPY --from=build  --chown=1001:0 /build/app.zip /usr/src/app.zip
COPY --from=build  --chown=1001:0 /build/config.conf /usr/src/conf/config.conf

RUN apt install unzip -y && unzip /usr/src/app.zip

EXPOSE 8111

ENTRYPOINT ["sh", "-c", "java -Dfile.encoding=UTF-8 -Dlog-viewer.config-file=conf/config.conf -jar log-viewer*/lib/log-viewer-cli*.jar startup"]
