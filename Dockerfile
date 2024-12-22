FROM openjdk:17-jdk-slim

ENV LANG=C.UTF-8

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y sqlite3 curl && \
    apt-get clean

WORKDIR /rest-api-scala

COPY out/production/rest-api-scala/ /rest-api-scala/

RUN curl -L -o /rest-api-scala/sqlite-jdbc.jar https://github.com/xerial/sqlite-jdbc/releases/download/3.36.0.3/sqlite-jdbc-3.36.0.3.jar && \
    curl -L -o /rest-api-scala/scala-library.jar https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.15/scala-library-2.13.15.jar && \
    curl -L -o /rest-api-scala/scala-reflect.jar https://repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.13.15/scala-reflect-2.13.15.jar

EXPOSE 8080

CMD ["java", "-cp", ".:/rest-api-scala/scala-library.jar:/rest-api-scala/scala-reflect.jar:/rest-api-scala/sqlite-jdbc.jar", "Main"]