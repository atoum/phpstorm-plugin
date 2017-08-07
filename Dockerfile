FROM java:8-jdk

RUN apt-get update && apt-get install -y ant
RUN apt-get update && apt-get install -y make
