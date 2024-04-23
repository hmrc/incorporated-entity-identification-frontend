import uk.gov.hmrc.DefaultBuildSettings

val appName = "incorporated-entity-identification-frontend"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.8"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(ScoverageSettings())
  .settings(
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= AppDependencies.apply()
  )
  .disablePlugins(JUnitXmlReportPlugin)
PlayKeys.playDefaultPort := 9718
scalacOptions += "-Wconf:src=routes/.*:s"
scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s"

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._"
)


Test / Keys.fork := true
Test / javaOptions += "-Dlogger.resource=logback-test.xml"
Test / parallelExecution := true

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
