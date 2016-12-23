FROM java:8-jdk
MAINTAINER edewit

RUN mkdir -p /home/forge
RUN groupadd -r forge && useradd -r -g forge forge -d /home/forge
RUN chown forge:forge /home/forge
USER forge

COPY generator-backend-1.0.0.Alpha1-swarm.jar /app/generator-backend-1.0.0.Alpha1-swarm.jar
EXPOSE 8080
EXPOSE 8443

WORKDIR /app/
CMD ["java", "-jar", "generator-backend-1.0.0.Alpha1-swarm.jar"]