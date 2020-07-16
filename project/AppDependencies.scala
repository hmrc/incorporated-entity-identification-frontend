import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "2.23.0",
    "uk.gov.hmrc" %% "play-frontend-govuk" % "0.49.0-play-27",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.16.0-play-27"
  )

  val it = Seq(
    "org.scalatest" %% "scalatest" % "3.1.2" % IntegrationTest,
    "org.jsoup" % "jsoup" % "1.10.2" % IntegrationTest,
    "com.typesafe.play" %% "play-test" % current % IntegrationTest,
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % IntegrationTest,
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % IntegrationTest,
    "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % IntegrationTest
  )


}
