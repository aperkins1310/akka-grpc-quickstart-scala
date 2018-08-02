name := "akka-grpc-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion     = "2.5.13"
lazy val akkaGrpcVersion = "0.1"

enablePlugins(AkkaGrpcPlugin)

// ALPN agent
enablePlugins(JavaAgent)
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.7" % "runtime;test"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-stream-testkit"              % akkaVersion % "test",
  "org.scalatest"      %% "scalatest"                        % "3.0.5" % "test",
  "com.github.sebruck" %% "opencensus-scala-akka-http"       % "0.6.0",
  "io.opencensus"      % "opencensus-exporter-trace-logging" % "0.15.0",
  "com.typesafe.akka"  %% "akka-http2-support"               % "10.1.3",
  "ch.qos.logback"     % "logback-classic"                   % "1.2.3"
)
