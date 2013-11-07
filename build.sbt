import AssemblyKeys._

assemblySettings

name := "shift-reduce-enju"

version := "0.1"

fork in run := true

// parallelExecution in Test := false

crossPaths := false

mainClass in assembly := Some("enju.ccg.Driver")

javacOptions ++= Seq("-Xlint:all")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M4" % "test->default"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"