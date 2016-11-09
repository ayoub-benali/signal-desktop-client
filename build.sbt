name := "signal-desktop-client"
version := "0.0.1"

scalaVersion := "2.12.0"
test in assembly := {}
fork := true

mainClass in (Compile, run) := Some("de.m7w3.signal.Main")

// scala specific options
scalacOptions := Vector(
  "-deprecation", "-unchecked", "-feature", "-encoding", "utf8",
  "-Xlint", "-Yno-adapted-args", "-Ywarn-dead-code",
  "-Ywarn-numeric-widen", "-Ywarn-value-discard", "-Ywarn-inaccessible",
  "-Ywarn-nullary-override", "-Ywarn-nullary-unit", "-Ywarn-unused-import"
)

libraryDependencies ++= Seq(
  "org.whispersystems"  % "signal-service-java" % "2.3.1",
  "org.scalafx"         %% "scalafx"            % "8.0.102-R11",
  "org.scalacheck"      %% "scalacheck"         % "1.13.4"  % Test,
  "org.scalatest"       %% "scalatest"          % "3.0.0"   % Test
)
