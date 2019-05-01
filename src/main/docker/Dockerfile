FROM openjdk:11-slim

LABEL PROJECT_NAME=brewery-ui \
      PROJECT=${project.id}

ENV SFG_SLEEP=5
ENV SERVER_PORT=8080

EXPOSE 8080

VOLUME /tmp
ADD maven/${project.build.finalName}.jar ${project.build.finalName}.jar
RUN sh -c 'touch /${project.build.finalName}.jar'

CMD echo "The application will start in ${SFG_SLEEP}s..." \
    && sleep ${SFG_SLEEP}  \
    && java -Djava.security.egd=file:/dev/./urandom -jar /${project.build.finalName}.jar