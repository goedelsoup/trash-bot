resolvers += Resolver.url("sbts3 ivy resolver", url("https://dl.bintray.com/emersonloureiro/sbt-plugins"))(
  Resolver.ivyStylePatterns
)
resolvers += Resolver.jcenterRepo

// Build + CI/CD
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")
addSbtPlugin("com.github.sbt" % "sbt-cpd" % "2.0.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.34")
addSbtPlugin("com.github.tkawachi" % "sbt-doctest" % "0.9.3")
addSbtPlugin("au.com.onegeek" %% "sbt-dotenv" % "1.2.88")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "3.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.3")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.18.0")
addSbtPlugin("cf.janga" % "sbts3" % "0.10.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.1")
