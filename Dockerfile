FROM  openjdk

WORKDIR /jigg

# Download dependencies
COPY build.sbt /jigg/
COPY project/*.sbt /jigg/project/
COPY bin /jigg/bin
RUN bin/sbt update

# Build
COPY . /jigg/
RUN bin/sbt assembly

# Download models and run a simple test
RUN mkdir -p models && cd models && \
 wget --no-verbose --show-progress --progress=bar:force:noscroll https://github.com/mynlp/jigg-models/raw/master/jigg-models.jar

RUN echo "テレビで自転車で走っている少女を見た" |\
 java -Xms1024M -Xmx1024M -cp "target/*:models/jigg-models.jar" \
 jigg.pipeline.Pipeline -annotators ssplit,kuromoji,jaccg
