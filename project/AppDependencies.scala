import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  val bootstrapVersion = "8.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % "0.71.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-28" % "8.4.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.14.2"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    val scope = "test,it"
    Seq(
      "org.scalatest"          %% "scalatest"              % "3.2.15"         % scope,
      "org.jsoup"               % "jsoup"                  % "1.15.4"         % scope,
      "com.typesafe.play"      %% "play-test"              % current          % scope,
      "com.vladsch.flexmark"    % "flexmark-all"           % "0.64.6"         % scope,
      "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion % scope
    )
  }

  val test: Seq[ModuleID] = Seq(
    "org.mockito"        % "mockito-core" % "5.2.0"    % Test,
    "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0" % Test
  )

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst"  % "wiremock-jre8"           % "2.35.0" % IntegrationTest,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % "0.71.0" % IntegrationTest
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
