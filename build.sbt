val appVersion = "2.0.0"

ThisBuild / name := "address-lookup-file-download-lambda-functions"
ThisBuild / version := appVersion
ThisBuild / scalaVersion := "3.3.7"

ThisBuild / assemblyJarName := s"address-lookup-file-download-lambda-functions_3-$appVersion.jar"

ThisBuild / parallelExecution := false

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "FastDoubleParser-LICENSE") => MergeStrategy.first
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
  case PathList("module-info.class") => MergeStrategy.discard
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case x =>
    (assembly / assemblyMergeStrategy).value(x) // For all the other files, use the default sbt-assembly merge strategy
}

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws"                  % "aws-lambda-java-core"             % "1.4.0",
  "com.amazonaws.secretsmanager"   % "aws-secretsmanager-caching-java"  % "2.1.0",
  "com.softwaremill.sttp.client4" %% "core"                             % "4.0.13",
  "ch.qos.logback"                 % "logback-core"                     % "1.5.21",
  "ch.qos.logback"                 % "logback-classic"                  % "1.5.21",
  "org.slf4j"                      % "slf4j-api"                        % "2.0.17",
  "org.playframework"             %% "play-json"                        % "3.0.6",
  "org.scalatest"                 %% "scalatest"                        % "3.2.19" % Test,
  "org.scalatestplus"             %% "mockito-5-12"                     % "3.2.19.0" % Test
)
