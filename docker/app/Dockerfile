FROM tomcat

ADD Communote-3.4.0.7b5d-linux.tar.gz /
RUN mv /Communote-3.4.0.7b5d /app
ADD context.xml /app/communote/conf/context.xml
RUN mkdir /communote-data
ADD startup.properties /app/communote/conf/communote/

ENV CATALINA_HOME=/app/communote/

CMD /app/communote/bin/startup.sh && /bin/bash -c "sleep inf"