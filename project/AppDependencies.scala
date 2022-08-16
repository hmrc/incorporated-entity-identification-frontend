import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.18.0",
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.18.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"  % "0.70.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "1.22.0-play-28",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    val scope = "test,it"
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.8" % scope,
      "org.jsoup" % "jsoup" % "1.11.1" % scope,
      "com.typesafe.play" %% "play-test" % current % scope,
      "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope
    )
  }

  val test: Seq[ModuleID] = Seq(
    "org.mockito" % "mockito-core" % "3.9.0" % Test,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.7.0" % Test
  )

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % IntegrationTest,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % "0.70.0" % Test
  )

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}
