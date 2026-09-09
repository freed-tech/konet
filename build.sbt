ThisBuild / scalaVersion := "2.12.10"
ThisBuild / organization := "care.freed"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "konet",
    libraryDependencies ++= Seq(
      "com.squareup.okhttp3"          %  "okhttp"               % "3.13.1",
      "com.google.inject"             %  "guice"                % "4.1.0",
      "com.fasterxml.jackson.core"    %  "jackson-databind"     % "2.8.11.1",
      "com.fasterxml.jackson.core"    %  "jackson-annotations"  % "2.8.11",
      "com.fasterxml.jackson.core"    %  "jackson-core"         % "2.8.11",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.11",
      "com.typesafe"                  %  "config"               % "1.4.3"
    )
  )