FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .
RUN mvn --batch-mode --no-transfer-progress clean package

FROM tomcat:10.1-jdk21-temurin

ENV PORT=8080

RUN sed -i 's/port="8080"/port="${http.port}"/' "$CATALINA_HOME/conf/server.xml" \
    && rm -rf "$CATALINA_HOME/webapps/ROOT" "$CATALINA_HOME/webapps/ROOT.war"

COPY --from=build /workspace/target/CodeCraversCanteen.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["sh", "-c", "export CATALINA_OPTS=\"$CATALINA_OPTS -Dhttp.port=${PORT:-8080}\"; exec catalina.sh run"]
