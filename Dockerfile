FROM ingensi/oracle-jdk
MAINTAINER adam@ajmichael.net

RUN yum update -y && yum install -y unzip
RUN curl -O http://downloads.typesafe.com/typesafe-activator/1.3.6/typesafe-activator-1.3.6.zip 
RUN unzip typesafe-activator-1.3.6.zip -d / && rm typesafe-activator-1.3.6.zip && chmod a+x /activator-dist-1.3.6/activator
ENV PATH $PATH:/activator-dist-1.3.6

EXPOSE 9000 9443
RUN mkdir /app
RUN mkdir /app/project
COPY ./public /app
COPY ./conf /app
COPY ./app /app
COPY ./project/build.properties /app/project/build.properties
COPY ./project/plugins.sbt /app/project/plugins.sbt
COPY ./build.sbt /app/build.sbt
WORKDIR /app
CMD ["activator", "run", "-Dhttps.port=9443"]
