FROM java:8-jdk
MAINTAINER edewit

ADD https://repository.jboss.org/nexus/service/local/repo_groups/public/content/org/obsidiantoaster/generator-backend/1.0.0.Alpha1/generator-backend-1.0.0.Alpha1-swarm.jar /app/generator-backend-1.0.0.Alpha1-swarm.jar
EXPOSE 8080

WORKDIR /app/
CMD ["java", "-jar", "generator-backend-1.0.0.Alpha1-swarm.jar"]