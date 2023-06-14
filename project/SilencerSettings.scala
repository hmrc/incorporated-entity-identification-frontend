import sbt.{CrossVersion, compilerPlugin}
import sbt.Keys.*
import sbt.*

object SilencerSettings {

  val silencerVersion = "1.7.12"

  // stop "unused import" warnings from routes files
  def apply(): Seq[Def.Setting[?]] = Seq(
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    // ***************
    // Use the silencer plugin to suppress warnings
    // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
    scalacOptions += "-P:silencer:pathFilters=views;routes",
  )
}
