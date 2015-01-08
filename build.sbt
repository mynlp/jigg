import AssemblyKeys._

assemblySettings

name := "enju-ccg"

scalaVersion := "2.10.2"

version := "0.3"

fork in run := true

parallelExecution in Test := false

crossPaths := false

mainClass in assembly := Some("enju.ccg.Driver")

javacOptions ++= Seq("-Xlint:all")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M4" % "test->default"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

resolvers += "Atilika Open Source repository" at "http://www.atilika.org/nexus/content/repositories/atilika"

libraryDependencies += "org.atilika.kuromoji" % "kuromoji" % "0.7.7"
