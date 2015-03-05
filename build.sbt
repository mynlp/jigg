import AssemblyKeys._

assemblySettings

organization := "jp"

name := "jigg"

scalaVersion := "2.10.2"

version := "0.4"

fork in run := true

parallelExecution in Test := false

crossPaths := false

mainClass in assembly := Some("jp.jigg.nlp.pipeline.Pipeline")

javacOptions ++= Seq("-Xlint:all")

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers ++= Seq(
  "Atilika Open Source repository" at "http://www.atilika.org/nexus/content/repositories/atilika"
)

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.10-M4" % "test->default",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.atilika.kuromoji" % "kuromoji" % "0.7.7")
