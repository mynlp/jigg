import AssemblyKeys._

assemblySettings

organization := "kcms.nii.ac.jp"

name := "shift-reduce-enju"

version := "0.0.1"

// autoScalaLibrary := false

fork in run := true

// parallelExecution in Test := false

crossPaths := false

javacOptions ++= Seq("-Xlint:all")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M4" % "test->default"

libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.2" % "test"

libraryDependencies ++= Seq( 
    "junit" % "junit" % "4.10",
    "com.novocode" % "junit-interface" % "0.10-M1" % "test"
    )
