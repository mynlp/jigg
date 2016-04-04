import AssemblyKeys._

assemblySettings

organization := "com.github.mynlp"
// organization := "jp"

name := "jigg"

scalaVersion := "2.11.7"

version := "0.5-SNAPSHOT"

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
  "org.scalanlp" % "breeze-config_2.10" % "0.9.1",
  "com.github.mynlp" % "jigg-kuromoji" % "0.1-SNAPSHOT",
  "com.github.mynlp" % "jigg-berkeley-parser" % "0.1-SNAPSHOT",
  "com.github.mynlp" % "jigg-stanford-corenlp" % "0.1-SNAPSHOT"
)

resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
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
