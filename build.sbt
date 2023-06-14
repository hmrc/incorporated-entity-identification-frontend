
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

val appName = "incorporated-entity-identification-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(
    resolvers += Resolver.jcenterRepo,
    majorVersion := 0,
    scalaVersion := "2.13.8",
    libraryDependencies ++= AppDependencies.apply()
  )
  .settings(ScoverageSettings())
  .settings(SilencerSettings() *)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings) *)
  .disablePlugins(JUnitXmlReportPlugin)

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._"
)

Test / Keys.fork := true
Test / javaOptions += "-Dlogger.resource=logback-test.xml"
Test / parallelExecution := true
addTestReportOption(Test, "test-reports")

IntegrationTest / Keys.fork := true
IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value
IntegrationTest / javaOptions += "-Dlogger.resource=logback-test.xml"
addTestReportOption(IntegrationTest, "int-test-reports")
IntegrationTest / parallelExecution := false
