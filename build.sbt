ThisBuild / name := "address-lookup-file-download-lambda-functions"
ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.12.12"

ThisBuild / assemblyJarName := "address-lookup-file-download-lambda-functions_2.12-1.0.jar"

ThisBuild / parallelExecution := false

//val jacksonVersion = "2.9.7"

val excludeJackonsBinding = Seq(
  ExclusionRule(organization = "com.fasterxml.jackson.core"),
  ExclusionRule(organization = "com.fasterxml.jackson.dataformat"),
  ExclusionRule(organization = "com.fasterxml.jackson.databind"),
  ExclusionRule(organization = "com.fasterxml.jackson.datatype")
)

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws"                 %   "aws-lambda-java-core"  % "1.1.0" ,
  "me.lamouri"                    %   "jcredstash"            % "2.1.1" ,
  "com.softwaremill.sttp.client3" %%  "core"                  % "3.6.2" ,
  "com.typesafe.play"             %%  "play-json"             % "2.8.2" excludeAll(excludeJackonsBinding: _*),
  "org.scalatest"                 %%  "scalatest"             % "3.2.2"           % Test,
  "org.scalatestplus"             %   "mockito-3-4_2.12"      % "3.1.3.0"         % Test
)
