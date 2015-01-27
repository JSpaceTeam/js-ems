import java.io.File

import com.typesafe.sbt.SbtScalariform._
import com.typesafe.sbteclipse.core.EclipsePlugin.{EclipseCreateSrc, EclipseKeys}
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbt.Keys._
import sbt._
import scoverage.ScoverageSbtPlugin._
import spray.revolver.RevolverPlugin.Revolver
import net.juniper.yang.YangPlugin

object Build extends Build {

  val akkaV = "2.3.7"

  val sprayV = "1.3.2"

  var spray_jsonV = "1.2.6"

  var gSettings = Defaults.coreDefaultSettings ++ Seq(
    scalaVersion  := "2.11.4",
    organization  := "net.juniper",
    version       := "0.1.4",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
      "io.spray"            %%  "spray-can"     % sprayV                  withSources(),
      "io.spray"            %%  "spray-routing" % sprayV                  withSources(),
      "io.spray"            %%  "spray-client"  % sprayV                  withSources(),
      "io.spray"            %%  "spray-json"    % spray_jsonV             withSources(),
      "io.spray"            %%  "spray-testkit" % sprayV  % "test"        withSources(),
      "com.typesafe.akka"   %%  "akka-actor"    % akkaV                   withSources(),
      "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test"        withSources(),
      "org.specs2"          %%  "specs2-core"   % "2.3.11"                withSources(),
      "net.juniper"         %% "easy-rest-core" % "0.1.4"                 withSources(),
      "net.juniper"         %% "easy-rest-persistence" % "0.1.4"          withSources(),
      "net.juniper"         %% "easy-rest-orm" % "0.1.4"                  withSources(),
      "net.juniper"         %% "easy-rest-integration-patterns" % "0.1.4" withSources(),
      "net.juniper"         % "jnc-library" % "0.1.3"                     withSources(),
      "ch.qos.logback"      %   "logback-classic" % "1.1.2"               withSources(),
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"           withSources()
    ),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    ScoverageKeys.minimumCoverage := 70,
    ScoverageKeys.failOnMinimumCoverage := false,
    ScoverageKeys.highlighting := {
      if (scalaBinaryVersion.value == "2.10") false
      else false
    },
    YangPlugin.yangPackageName := Some("net.juniper.yang"),
    YangPlugin.routesTraitName := None,
    publishMavenStyle := true,
    publishTo := Some(Resolver.file("file",  new File(System.getProperty("user.home") + "/mavenrepo/release"))),
    resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/mavenrepo/release",
    resolvers += "JSpace Maven Repo" at "http://10.155.87.253:8080/mavenrepo/release"
  ) ++ Revolver.settings ++ jacoco.settings ++ instrumentSettings ++ scalariformSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ XitrumPackage.copy() ++ YangPlugin.yangSettings

  lazy val root = Project("jspace-ems", file("."), settings = gSettings ++ XitrumPackage.copy("configuration", "bin/run.sh", "bin/run.bat") ++ Seq(publishArtifact := false)).aggregate(server, deviceMgt, emsBoot)

  lazy val server = Project("jspace-ems-server", file("server"), settings = gSettings ++ Seq(
    YangPlugin.routesTraitName := Some("EmsServerAllRoutes")
  ))

  lazy val deviceMgt = Project("jspace-device-mgt", file("imp-device-mgt"), settings = gSettings).dependsOn(server)

  lazy val emsBoot = Project("jspace-ems-boot", file("ems-boot"), settings = gSettings).dependsOn(server, deviceMgt)

}
