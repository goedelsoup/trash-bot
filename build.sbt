import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport._

import ReleaseTransformations._

enablePlugins(DockerComposePlugin)

val CatsVersion = "1.6.0"
val CatsRetryVersion = "0.2.7"
val CirceVersion = "0.11.0"
val CirceGeoJsonVersion = "5.0.0"
val CirisVersion = "0.12.1"
val Cloud4sVersion = "0.1.2"
val CompEffCommonsVersion = "0.3.0"
val CormorantVersion = "0.2.0-M4"
val FS2Version = "1.0.4"
val Http4sVersion = "0.20.3"
val Log4CatsVersion = "0.3.0"
val ScalaCheckVersion = "1.14.0"
val ScalaTestVersion = "3.0.5"
val ScanamoVersion = "1.0.0-M10"

lazy val CompileAndTest = "compile->compile;test->test"

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / organization := "goedelsoup"
ThisBuild / organizationName := "goedelsoup"

enablePlugins(DockerComposePlugin)

addCommandAlias("fmtAll", ";scalafmt; test:scalafmt; scalafmtSbt")
addCommandAlias("fmtCheck", ";scalafmtCheck; test:scalafmtCheck; scalafmtSbtCheck")

lazy val sharedSettings = Seq(
  Compile / run / fork := true,
  scalafmtOnCompile := true,
  testOptions in Test += Tests.Argument(
    TestFrameworks.ScalaCheck,
    "-maxSize",
    "5",
    "-minSuccessfulTests",
    "100",
    "-workers",
    "2",
    "-verbosity",
    "2"
  ),
  logLevel in Test := Level.Error,
  scalacOptions := Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xfuture", // Turn on future language features.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match", // Pattern match may not be typesafe.
    "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification", // Enable partial unification in type constructor inference
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Xmax-classfile-name",
    "78"
  ),
  resolvers ++= Seq(
    "CompStak Releases".at("s3://compstak-maven.s3-us-east-1.amazonaws.com/releases"),
    "CompStak Snapshots".at("s3://compstak-maven.s3-us-east-1.amazonaws.com/snapshots")
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7"),
  addCompilerPlugin(("org.scalamacros" %% "paradise" % "2.1.1").cross(CrossVersion.full)),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
  publishTo := {
    val prefix = if (isSnapshot.value) "snapshots" else "releases"
    Some("CompStak".at(s"s3://compstak-maven.s3-us-east-1.amazonaws.com/$prefix"))
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  }
)

lazy val trash =
  project
    .in(file("."))
    .enablePlugins(
      DockerPlugin,
      DockerComposePlugin,
      JavaServerAppPackaging,
      AshScriptPlugin
    )
    .configs(IntegrationTest)
    .settings(sharedSettings)
    .settings(
      Defaults.itSettings,
      name := "trash-bot",
      mainClass in assembly := Some("goedelsoup.trash.Bot"),
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-macros" % CatsVersion,
        "org.typelevel" %% "cats-kernel" % CatsVersion,
        "org.typelevel" %% "cats-core" % CatsVersion,
        "org.typelevel" %% "cats-free" % CatsVersion,
        "org.typelevel" %% "cats-laws" % CatsVersion % Test,
        "org.typelevel" %% "cats-testkit" % CatsVersion % Test,
        "org.typelevel" %% "mouse" % "0.17",
        "io.chrisdavenport" %% "cats-par" % "0.2.0",
        "com.github.cb372" %% "cats-retry-core" % CatsRetryVersion,
        "com.github.cb372" %% "cats-retry-cats-effect" % CatsRetryVersion,
        "io.chrisdavenport" %% "mules" % "0.2.0",
        "io.chrisdavenport" %% "log4cats-core" % Log4CatsVersion,
        "io.chrisdavenport" %% "log4cats-slf4j" % Log4CatsVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "ch.qos.logback" % "logback-core" % "1.2.3",
        "co.fs2" %% "fs2-core" % FS2Version,
        "co.fs2" %% "fs2-io" % FS2Version,
        "io.circe" %% "circe-core" % CirceVersion,
        "io.circe" %% "circe-fs2" % CirceVersion,
        "io.circe" %% "circe-generic" % CirceVersion,
        "io.circe" %% "circe-generic-extras" % CirceVersion,
        "io.circe" %% "circe-java8" % CirceVersion,
        "io.circe" %% "circe-literal" % CirceVersion,
        "io.circe" %% "circe-optics" % CirceVersion,
        "io.circe" %% "circe-parser" % CirceVersion,
        "io.circe" %% "circe-refined" % CirceVersion,
        "io.circe" %% "circe-shapes" % CirceVersion,
        "org.scanamo" %% "scanamo" % ScanamoVersion,
        "is.cir" %% "ciris-cats" % CirisVersion,
        "is.cir" %% "ciris-cats-effect" % CirisVersion,
        "is.cir" %% "ciris-core" % CirisVersion,
        "is.cir" %% "ciris-refined" % CirisVersion,
        "commons-codec" % "commons-codec" % "1.10",
        "org.typelevel" %% "discipline" % "0.10.0" % Test,
        "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % Test,
        "eu.timepit" %% "refined-scalacheck" % "0.9.8" % Test,
        "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.6" % Test,
        "com.47deg" %% "scalacheck-toolbox-datetime" % "0.2.5" % "test",
        "com.ironcorelabs" %% "cats-scalatest" % "2.3.1" % Test,
        "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
        "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
        "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
        "org.http4s" %% "http4s-circe" % Http4sVersion,
        "org.http4s" %% "http4s-dsl" % Http4sVersion,
        "org.http4s" %% "http4s-prometheus-metrics" % Http4sVersion,
        "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
        "org.scalatest" %% "scalatest" % ScalaTestVersion % IntegrationTest,
        "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % Test,
        "eu.timepit" %% "refined-scalacheck" % "0.9.8" % Test
      ),
      inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings),
      dockerBaseImage := "adoptopenjdk/openjdk10:jdk-10.0.2.13-alpine",
      dockerRepository := Some(s"278696104475.dkr.ecr.us-east-1.amazonaws.com"),
      dockerUpdateLatest := true,
      packageName in Docker := "critter-bot",
      dockerExposedPorts += 9500,
      dockerCommands := Seq(
        Cmd("FROM", dockerBaseImage.value),
        Cmd("RUN", "apk add --no-cache bash"),
        Cmd("WORKDIR", "/opt/docker"),
        Cmd("ADD", "--chown=daemon:daemon opt /opt"),
        Cmd("USER", "daemon"),
        ExecCmd("ENTRYPOINT", "/opt/docker/bin/trash-bot"),
        Cmd("CMD", "[]")
      ),
      version in Docker := (if (releaseUseGlobalVersion.value) {
                              (version in ThisBuild).value
                            } else version.value),
      dockerImageCreationTask := (publishLocal in Docker).value,
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepTask(publish in Docker),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    )