ThisBuild / name := "address-lookup-file-download-lambda-function"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.12.12"

ThisBuild / assemblyJarName := "address-lookup-file-download-lambda-function_2.12-1.0.jar"

ThisBuild / parallelExecution := false

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.150",
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "commons-io" % "commons-io" % "2.5",
  "co.wrisk.jcredstash" % "jcredstash" % "0.0.3",
  "com.github.lookfirst" % "sardine" % "5.7"
)