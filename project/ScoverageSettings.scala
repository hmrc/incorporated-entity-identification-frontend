import sbt.Def
import scoverage.ScoverageKeys

object ScoverageSettings {

  def apply(): Seq[Def.Setting[?]] =
    Seq(
      ScoverageKeys.coverageExcludedPackages := List(
        "<empty>",
        ".*Routes.*",
        ".*Reverse.*",
        "app.*",
        "prod.*",
        "config.*",
        "com.kenshoo.play.metrics.*",
        "testOnlyDoNotUseInAppConf.*",
        "uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.api.*",
        "uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.frontend.*",
        "uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.*",
        "uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.*"
      ).mkString(";"),
      ScoverageKeys.coverageMinimumStmtTotal := 92,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
}
