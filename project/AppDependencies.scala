import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "4.3.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "4.3.0",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.71.0-play-27",
    "uk.gov.hmrc" %% "simple-reactivemongo" % "8.0.0-play-27",
    "uk.gov.hmrc" %% "auth-client" % "5.4.0-play-27",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.59.0-play-27"

  )

  val sharedTestDependencies: Seq[ModuleID] = {
    val scope = "test,it"
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.8" % scope,
      "org.jsoup" % "jsoup" % "1.11.1" % scope,
      "com.typesafe.play" %% "play-test" % current % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % scope
    )
  }

  val test: Seq[ModuleID] = Seq(
    "org.mockito" % "mockito-core" % "3.9.0" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.7.0" % Test
  )

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % IntegrationTest
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
