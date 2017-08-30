FROM  openjdk

WORKDIR /jigg

# Download dependencies
COPY build.sbt /jigg/
COPY project/*.sbt /jigg/project/
COPY bin /jigg/bin
RUN bin/sbt update

# Download models
RUN mkdir -p models && cd models && \
 wget --no-verbose --show-progress --progress=bar:force:noscroll https://github.com/mynlp/jigg-models/raw/master/jigg-models.jar

# Build
COPY . /jigg/
RUN bin/sbt assembly

# Run a simple test
RUN echo "テレビで自転車で走っている少女を見た" |\
 java -Xms1024M -Xmx1024M -cp "target/*:models/jigg-models.jar" \
 jigg.pipeline.Pipeline -annotators ssplit,kuromoji,jaccg
