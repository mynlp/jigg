FROM  openjdk:8

WORKDIR /jigg

# Download dependencies
COPY build.sbt /jigg/
COPY project/*.sbt project/build.properties /jigg/project/
COPY bin /jigg/bin
RUN bin/sbt update

# Build
COPY src /jigg/src
COPY jar /jigg/jar
RUN bin/sbt assembly

# Run a simple test
RUN echo "テレビで自転車で走っている少女を見た" |\
 java -Xms1024M -Xmx1024M -cp "target/*:jar/jigg-models.jar" \
 jigg.pipeline.Pipeline -annotators ssplit,kuromoji,jaccg
