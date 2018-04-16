name := "akka-http-example"

organization := "io.wonder.soft"

version := "0.0.1"

scalaVersion := "2.12.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV        = "2.5.10"
  val akkaStreamV  = "10.1.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor"              % akkaV,
    "com.typesafe.akka" %% "akka-persistence"        % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"              % akkaV,
    "ch.qos.logback"    %  "logback-classic"         % "1.1.7",
    "com.typesafe.akka" %% "akka-stream"             % akkaV,

    "com.typesafe.akka" %% "akka-http-core"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http"               % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json"    % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit"       % akkaStreamV
  )
}
