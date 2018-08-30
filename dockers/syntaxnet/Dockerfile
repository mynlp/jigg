# If you build a image using this file, please run the following command at a directory 'jigg/',
# ```
# docker build -t {image name}:{tag} -f dockers/syntaxnet/Dockerfile .
# ```

FROM tensorflow/syntaxnet

WORKDIR /jigg

RUN apt-get update -y && apt-get install -y less wget tar bzip2 unzip sudo make gcc g++ libz-dev

# install jigg
COPY build.sbt /jigg/
COPY project/*.sbt project/build.properties /jigg/project/
COPY bin /jigg/bin
RUN bin/sbt update

# Build
COPY src /jigg/src
COPY jar /jigg/jar
RUN bin/sbt assembly
