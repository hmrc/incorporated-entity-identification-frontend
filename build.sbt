import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "incorporated-entity-identification-frontend"

val silencerVersion = "1.7.0"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.11",
    libraryDependencies ++= AppDependencies.apply(),
    // ***************
    // Use the silencer plugin to suppress warnings
    // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
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
