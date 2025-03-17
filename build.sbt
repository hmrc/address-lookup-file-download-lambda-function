ThisBuild / name := "address-lookup-file-download-lambda-functions"
ThisBuild / version := "1.2.2.0"
ThisBuild / scalaVersion := "3.3.5"

ThisBuild / assemblyJarName := "address-lookup-file-download-lambda-functions_3-1.2.2.0.jar"

ThisBuild / parallelExecution := false

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) =>
    MergeStrategy.discard // from com/fasterxml/jackson libraries
  case PathList("module-info.class") =>
    MergeStrategy.discard // from com/fasterxml/jackson libraries
  case x =>
    (assembly / assemblyMergeStrategy).value(x) // For all the other files, use the default sbt-assembly merge strategy
}

ThisBuild / libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.3",
  "com.amazonaws.secretsmanager" % "aws-secretsmanager-caching-java" % "1.0.2",
  "com.softwaremill.sttp.client3" %% "core" % "3.10.3",
  "ch.qos.logback" % "logback-core" % "1.5.17",
  "ch.qos.logback" % "logback-classic" % "1.5.17",
  "org.slf4j" % "slf4j-api" % "2.0.17",
  "org.playframework" %% "play-json" % "3.0.4",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" % "mockito-5-12_3" % "3.2.19.0" % Test
)
