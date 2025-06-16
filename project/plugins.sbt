resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"        % "sbt-auto-build"        % "3.24.0")
addSbtPlugin("uk.gov.hmrc"        % "sbt-distributables"    % "2.6.0")
<<<<<<< HEAD
<<<<<<< HEAD
addSbtPlugin("org.playframework"  % "sbt-plugin"            % "3.0.7")
=======
addSbtPlugin("org.playframework"  % "sbt-plugin"            % "3.0.6")
>>>>>>> ad5563e (VER-5592 [AL] Update dependencies)
addSbtPlugin("org.scoverage"      % "sbt-scoverage"         % "2.0.10")
=======
addSbtPlugin("org.playframework"  % "sbt-plugin"            % "3.0.7")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"         % "2.3.0")
>>>>>>> 47a11dc (VER-5592 [AL] Update dependencies)
addSbtPlugin("io.github.irundaia" % "sbt-sassify"           % "1.5.2")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "1.0.0" exclude("org.scala-lang.modules", "scala-xml_2.12"))