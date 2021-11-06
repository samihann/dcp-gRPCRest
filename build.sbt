
name := "dcp-gRPCRest"

version := "0.1"

scalaVersion := "3.1.0"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)


/***
 * Please uncomment the below line if the project is being run on M1 Macbooks.
 *
 * 1. Install protobuf package in the mac by executing following command
 *        brew install protobuf
 * 2. Un-comment the below given line. The project should compile without any errors.
 */
//PB.protocExecutable := file("/opt/homebrew/Cellar/protobuf/3.17.3/bin/protoc")



libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
)

val logbackVersion = "1.3.0-alpha10"
val sfl4sVersion = "2.0.0-alpha5"
val scalacticVersion = "3.2.9"
val typesafeConfigVersion = "1.4.1"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.2"
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.slf4j" % "slf4j-api" % sfl4sVersion,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test,
  "com.typesafe" % "config" % typesafeConfigVersion,

)