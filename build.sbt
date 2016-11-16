name := "signal-desktop-client"
version := "0.0.1"

scalaVersion := "2.11.8"
test in assembly := {}
fork := true


val jfxrtJar = file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar")
unmanagedJars in Compile += Attributed.blank(jfxrtJar)

mainClass in (Compile, run) := Some("de.m7w3.signal.Main")

// scala specific options
scalacOptions := Vector(
  "-deprecation", "-unchecked", "-feature", "-encoding", "utf8",
  "-Xlint", "-Yno-adapted-args", "-Ywarn-dead-code",
  "-Ywarn-numeric-widen", "-Ywarn-value-discard", "-Ywarn-inaccessible",
  "-Ywarn-nullary-override", "-Ywarn-nullary-unit", "-Ywarn-unused-import",
  "-Dscala.usejavacp=true"
)

// needed for scalafxml
addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.whispersystems"        % "signal-service-java"  % "2.4.0",
  "org.bouncycastle"          % "bcprov-jdk16"         % "1.46",
  "com.github.scopt"          %% "scopt"               % "3.5.0",
  "org.scalafx"               %% "scalafx"             % "8.0.102-R11",
  "org.scalafx"               %% "scalafxml-core-sfx8" % "0.3",
  "org.controlsfx"            %  "controlsfx"          % "8.40.12",
  "org.scalacheck"            %% "scalacheck"          % "1.13.4"       % Test,
  "org.scalatest"             %% "scalatest"           % "3.0.0"        % Test,
  "junit"                     % "junit"                % "4.12"         % Test,
  "org.testfx"                % "testfx-core"          % "4.0.4-alpha"  % Test,
  "org.testfx"                % "testfx-junit"         % "4.0.4-alpha"  % Test,
  "org.apache.logging.log4j"  % "log4j-api"            % "2.7",
  "org.apache.logging.log4j"  % "log4j-core"           % "2.7"
)
