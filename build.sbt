val annotatorLibrary = Map[String, (ModuleID, String)]()

organization := "com.github.mynlp"

name := "jigg"

scalaVersion := "2.11.8"

version := "0.6.2"

fork in run := true

parallelExecution in Test := false

crossPaths := false

mainClass in assembly := Some("jp.jigg.nlp.pipeline.Pipeline")

javacOptions ++= Seq("-Xlint:all", "-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.10-M4" % "test->default",
  "org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "com.ibm.icu" % "icu4j" % "56.1",
  "org.scalanlp" %% "breeze" % "0.12",
  "org.scalanlp" % "breeze-config_2.10" % "0.9.1",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.1",
  // "com.typesafe.akka" %% "akka-http-xml" % "10.0.1",
  "edu.ucar" % "cdm" % "4.6.8"
)

libraryDependencies ++= (
  stanfordCoreNLPDependencies
    ++ kuromojiDependencies
    ++ berkeleyParserDependencies)

val stanfordCoreNLPDependencies = Seq(
  "org.slf4j" % "slf4j-api" % "1.7.20", // logger
  "org.slf4j" % "slf4j-simple" % "1.7.6",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0"
)

val kuromojiDependencies = Seq(
  "com.atilika.kuromoji" % "kuromoji-ipadic" % "0.9.0",
  "com.atilika.kuromoji" % "kuromoji-jumandic" % "0.9.0",
  "com.atilika.kuromoji" % "kuromoji-unidic" % "0.9.0"
)

val berkeleyParserDependencies = Seq(
  "edu.berkeley.nlp" % "berkeleyparser" % "r32"
)

libraryDependencies ++= annotatorLibrary.map { case (k, v) => v._1 }.toSeq

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := annotatorLibrary.map { case (k, v) =>
      BuildInfoKey.action("ann_" + k)(v._2)
    }.toSeq,
      // Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "jigg.pipeline.annotator"
  )

resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Unidata maven repository" at "http://artifacts.unidata.ucar.edu/content/repositories/unidata-releases"
)

publishMavenStyle := true

publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := <url>https://github.com/mynlp/jigg</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:mynlp/jigg.git</url>
    <connection>scm:git:git@github.com:mynlp/jigg.git</connection>
  </scm>
  <developers>
    <developer>
      <id>h.nouji@gmail.com</id>
      <name>Hiroshi Noji</name>
      <url>https://github.com/nozyh/</url>
    </developer>
  </developers>
