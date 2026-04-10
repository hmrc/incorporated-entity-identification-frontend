import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.7.0"
  val mongoVersion = "2.12.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "12.20.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % mongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.21.2"
  )

  val sharedTestDependencies: Seq[ModuleID] = {
    Seq(
      "org.jsoup"          % "jsoup"                  % "1.22.1",
      "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion
    ).map(_ % Test)
  }

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "mockito-5-10" % "3.2.18.0"
  ).map(_ % Test)

  val it: Seq[ModuleID] = Seq(
    "org.wiremock" % "wiremock" % "3.13.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ sharedTestDependencies ++ test ++ it

}