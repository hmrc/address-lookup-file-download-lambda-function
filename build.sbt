ThisBuild / name := "address-lookup-file-download-lambda-functions"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.12.12"

ThisBuild / assemblyJarName := "address-lookup-file-download-lambda-functions_2.12-1.0.jar"

ThisBuild / parallelExecution := false

val jacksonVersion = "2.9.7"

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "co.wrisk.jcredstash" % "jcredstash" % "0.0.3",
  "com.github.lookfirst" % "sardine" % "5.7",
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion
)