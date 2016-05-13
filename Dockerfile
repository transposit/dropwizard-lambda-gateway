FROM kmonkeyjam/nginx-java8

MAINTAINER Tina Huang (tina@monkey.name)

RUN mkdir /app
WORKDIR /app
RUN mkdir /app/configs
COPY configs/* /app/configs/
COPY lambda-wrapper/target/lambda-wrapper-0.0.1-SNAPSHOT.jar /app

EXPOSE 8080

CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "/winnie/lambda-wrapper-0.0.1-SNAPSHOT.jar", "server"]
