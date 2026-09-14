ThisBuild / scalaVersion := "2.12.10"
ThisBuild / organization := "com.github.freed-tech"
ThisBuild / version      := "1.0.0"

packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
  "Implementation-Title"     -> "konet",
  "Implementation-Version"   -> version.value,
  "Implementation-Vendor"    -> "care.freed",
  "Specification-Title"      -> "konet",
  "Specification-Version"    -> version.value,
  "Specification-Vendor"     -> "care.freed",
  "Created-By"               -> s"sbt ${sbtVersion.value}",
  "Build-Jdk"                -> System.getProperty("java.version")
)

lazy val root = (project in file("."))
  .settings(
    name := "konet",
    crossPaths := false,

    Compile / packageDoc / publishArtifact := true,
    Compile / packageSrc / publishArtifact := true,

    licenses := Seq("Proprietary" -> url("https://care.freed/licenses/internal")),
    homepage := Some(url("https://github.com/freed-tech/konet")),
    scmInfo  := Some(
      ScmInfo(
        url("https://github.com/freed-tech/konet"),
        "scm:git:git@github.com:freed-tech/konet.git"
      )
    ),
    developers := List(
      Developer(
        id    = "core-team",
        name  = "Freed Tech Team",
        email = "techsupport@freed.care",
        url   = url("https://freed.care")
      )
    ),

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

ThisBuild / organization := "com.github.freed-tech"

publishTo := Some(
  "GitHub Packages" at "https://maven.pkg.github.com/freed-tech/konet"
)

credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "freed-tech",
  System.getenv("GITHUB_TOKEN") // Reads token from environment
)