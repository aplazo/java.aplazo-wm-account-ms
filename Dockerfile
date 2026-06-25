FROM maven:3-amazoncorretto-17 as build
COPY ./ /app/
WORKDIR /app/

ADD settings.xml /root/.m2/settings.xml

RUN mvn -Dmaven.test.skip=true -s settings.xml clean package &&\
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../../wm-account-ms-service/target/*.jar

FROM amazoncorretto:17-alpine-jdk
ARG DEPENDENCY=target/dependency
COPY --from=build /app/target/ /build/target/
RUN apk add --no-cache curl && \
    cd /build && \
    addgroup -S spring && adduser -S spring -G spring && \
    mkdir /app && \
    cp -R ${DEPENDENCY}/BOOT-INF/lib/ /app/lib/ && \
    cp -R ${DEPENDENCY}/META-INF/ /app/META-INF/ && \
    cp -R ${DEPENDENCY}/BOOT-INF/classes/* /app/  && \
    cd /app && \
    wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer \
    && echo 'networkaddress.cache.ttl=1' >> $JAVA_HOME/conf/security/java.security \
    && echo 'networkaddress.cache.negative.ttl=3' >> $JAVA_HOME/conf/security/java.security \

USER spring:spring
ENTRYPOINT ["java","--add-opens","java.base/java.io=ALL-UNNAMED","-Dspring.profiles.active=api","-XX:+UseContainerSupport","-javaagent:/app/dd-java-agent.jar","-cp","app:/app/lib/*","mx.aplazo.microservices.wm.account.WMAccountMsApp"]
