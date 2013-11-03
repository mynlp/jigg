import AssemblyKeys._

assemblySettings

organization := "kcms.nii.ac.jp"

name := "shift-reduce-enju"

version := "0.0.1"

fork in run := true

// parallelExecution in Test := false

crossPaths := false

mainClass in assembly := Some("enju.ccg.Driver")

javacOptions ++= Seq("-Xlint:all")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M4" % "test->default"

libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.2" % "test"

libraryDependencies ++= Seq(
    "junit" % "junit" % "4.10",
    "com.novocode" % "junit-interface" % "0.10-M1" % "test"
    )
