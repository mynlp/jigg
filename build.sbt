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

javacOptions ++= Seq("-Xlint:all", "-source", "1.6", "-target", "1.6")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.10-M4" % "test->default",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "com.atilika.kuromoji" % "kuromoji-ipadic" % "0.9.0",
  "com.atilika.kuromoji" % "kuromoji-jumandic" % "0.9.0",
  "com.atilika.kuromoji" % "kuromoji-unidic" % "0.9.0")
