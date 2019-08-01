name := "FullstackOpen"

version := "0.3"

scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.9",
  "com.typesafe.akka" %% "akka-actor"   % "2.5.23",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.9",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0",
  "ch.megard" %% "akka-http-cors" % "0.4.1"
)

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

mainClass in Compile := Some("fto.Main")
