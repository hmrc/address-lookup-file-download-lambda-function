ThisBuild / name := "address-lookup-file-download-lambda-functions"
ThisBuild / version := "1.2.2"
ThisBuild / scalaVersion := "2.12.12"

ThisBuild / assemblyJarName := "address-lookup-file-download-lambda-functions_2.12-1.2.2.jar"

ThisBuild / parallelExecution := false

ThisBuild / assemblyMergeStrategy := {
  case PathList("module-info.class")  => MergeStrategy.discard // from com/fasterxml/jackson libraries
  case x =>
    (assembly / assemblyMergeStrategy).value(x) // For all the other files, use the default sbt-assembly merge strategy
}

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws"                  % "aws-lambda-java-core"             % "1.2.2",
  "com.amazonaws.secretsmanager"   % "aws-secretsmanager-caching-java"  % "1.0.2",
  "com.softwaremill.sttp.client3" %% "core"                             % "3.6.2",
  "ch.qos.logback"                 % "logback-core"                     % "1.5.6",
  "ch.qos.logback"                 % "logback-classic"                  % "1.5.6",
  "org.slf4j"                      % "slf4j-api"                        % "2.0.16",
  "com.typesafe.play"             %% "play-json"                        % "2.8.2",
  "org.scalatest"                 %% "scalatest"                        % "3.2.19" % Test,
  "org.scalatestplus"              % "mockito-3-4_2.12"                 % "3.2.10.0" % Test
)
