name := "FullstackOpen"

version := "0.3"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.9",
  "com.typesafe.akka" %% "akka-actor"   % "2.5.23",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9"
)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

mainClass in Compile := Some("fto.Main")
