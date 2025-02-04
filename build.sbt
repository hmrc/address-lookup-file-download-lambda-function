ThisBuild / name := "address-lookup-file-download-lambda-functions"
ThisBuild / version := "1.1"
ThisBuild / scalaVersion := "2.12.12"

ThisBuild / assemblyJarName := "address-lookup-file-download-lambda-functions_2.12-1.1.jar"

ThisBuild / parallelExecution := false

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws"                 %   "aws-lambda-java-core"              % "1.1.0" ,
  "com.amazonaws.secretsmanager"  %   "aws-secretsmanager-caching-java"   % "2.0.0",
  "com.softwaremill.sttp.client3" %%  "core"                              % "3.6.2" ,
  "com.typesafe.play"             %%  "play-json"                         % "2.8.2",
  "org.scalatest"                 %%  "scalatest"                         % "3.2.2"     % Test,
  "org.scalatestplus"             %   "mockito-3-4_2.12"                  % "3.1.3.0"   % Test
)
