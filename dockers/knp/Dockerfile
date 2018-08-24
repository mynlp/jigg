# If you build a image using this file, please run the following command at a directory `jigg/`,
# ```
# docker build -t {image name}:{tag} -f docker/knp/Dockerfile . 
# ```
FROM jigg/jigg-dockers:knp

WORKDIR /jigg

ENV LD_LIBRARY_PATH $LD_LIBRARY_PATH:/usr/local/bin:/usr/local/lib
ENV PATH $PATH:$HOME/usr/bin

COPY build.sbt /jigg/
COPY project/*.sbt project/build.properties /jigg/project/
COPY bin /jigg/bin
RUN bin/sbt update

# Build
COPY src /jigg/src
COPY jar /jigg/jar
RUN bin/sbt assembly