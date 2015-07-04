organization := "usernameinuse"

version := "0.1"

scalaVersion := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray" %% "spray-util" % sprayV,
    "io.spray" %% "spray-client" % sprayV,
    "io.spray" %% "spray-servlet" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "io.spray" %% "spray-json" % "1.3.2",
    "io.spray" %% "spray-http" % sprayV,
    "io.spray" %% "spray-httpx" % sprayV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.json4s" % "json4s-native_2.11" % "3.2.11",
    "org.specs2" %% "specs2-core" % "2.3.11" % "test",
    "com.google.api-client" % "google-api-client" % "1.19.1",
    "com.google.api-client" % "google-api-client-java6" % "1.19.1",
    "com.google.apis" % "google-api-services-oauth2" % "v2-rev83-1.19.0",
    "com.google.oauth-client" % "google-oauth-client-jetty" % "1.19.0",
    "com.google.apis" % "google-api-services-fitness" % "v1-rev4-1.19.0",
    "com.google.apis" % "google-api-services-storage" % "v1-rev23-1.19.0",
    "com.google.apis" % "google-api-services-datastore-protobuf" % "v1beta2-rev1-2.1.0",
    "com.google.protobuf" % "protobuf-java" % "2.6.1"
  )
}

inConfig(Compile)(
  artifact in packageWar <<= moduleName(n => Artifact("ROOT.war"))
)

tomcat()
Revolver.settings
