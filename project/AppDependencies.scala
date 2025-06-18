import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.13.0"
  val mongoVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "11.13.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % mongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.18.0"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    Seq(
      "org.scalatest"          %% "scalatest"              % "3.2.19"         % Test,
      "org.jsoup"               % "jsoup"                  % "1.18.1"         % Test,
      "org.playframework"      %% "play-test"              % current          % Test,
      "com.vladsch.flexmark"    % "flexmark-all"           % "0.64.8"         % Test,
      "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion % Test
    )
  }

  val test: Seq[ModuleID] = Seq(
    "org.mockito"        % "mockito-core" % "5.14.0"    % Test,
    "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0"  % Test
  )

  val it: Seq[ModuleID] = Seq(
    "org.wiremock"           % "wiremock"                 % "3.9.1" % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % mongoVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
