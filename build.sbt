name := "signal-desktop-client"
version := "0.0.1"

scalaVersion := "2.11.8"
scalaVersion in ThisBuild := "2.11.8"
test in assembly := {}
fork := true

mainClass in (Compile, run) := Some("de.m7w3.signal.Main")

// scala specific options
scalacOptions := Vector(
  "-deprecation", "-unchecked", "-feature", "-encoding", "utf8",
  "-Xlint", "-Yno-adapted-args", "-Ywarn-dead-code",
  "-Ywarn-numeric-widen", "-Ywarn-value-discard", "-Ywarn-inaccessible",
  "-Ywarn-nullary-override", "-Ywarn-nullary-unit", "-Ywarn-unused-import",
  "-Dscala.usejavacp=true", "-Ylog-classpath"
)

// needed for scalafxml
addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "com.github.turasa"         % "signal-service-java"  % "2.4.0_unofficial_1",
  "com.github.kenglxn.QRGen"  % "javase"               % "2.2.0",
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
  "org.testfx"                % "openjfx-monocle"      % "1.8.0_20"     % Test,
  "org.slf4j"                 % "slf4j-api"            % "1.7.21",
  "org.apache.logging.log4j"  % "log4j-slf4j-impl"     % "2.7",
  "org.apache.logging.log4j"  % "log4j-api"            % "2.7",
  "org.apache.logging.log4j"  % "log4j-core"           % "2.7",
  "com.h2database"            % "h2"                   % "1.4.193",
  "com.typesafe.slick"        %% "slick"               % "3.1.1"
)
