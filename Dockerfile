FROM openjdk:8-jdk

ADD . .

ENTRYPOINT ["/entrypoint.sh"]
