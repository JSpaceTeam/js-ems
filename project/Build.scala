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

  val spray_jsonV = "1.2.6"

  val easy_restV = "0.3.2"

  var gSettings = Defaults.coreDefaultSettings ++ Seq(
    scalaVersion  := "2.11.4",
    organization  := "net.juniper",
    version       := "0.3.1",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
      "net.juniper"         %% "easy-rest-core" % easy_restV                 withSources(),
      "net.juniper"         %% "easy-rest-persistence" % easy_restV          withSources(),
      "net.juniper"         %% "easy-rest-orm" % easy_restV                  withSources(),
      "net.juniper"         %% "easy-rest-integration-patterns" % easy_restV withSources(),
      "net.juniper"         %% "js-yang-model" % "0.1.1"                     withSources(),
      "net.juniper"         %  "jmpsubsystem"        % "14.1.2"              withSources()    intransitive()
    ),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    ScoverageKeys.minimumCoverage := 70,
    ScoverageKeys.failOnMinimumCoverage := false,
    ScoverageKeys.highlighting := {
      if (scalaBinaryVersion.value == "2.10") false
      else false
    },
    YangPlugin.yangPackageName := Some("net.juniper.yang"),
    publishMavenStyle := true,
    publishTo := Some(Resolver.file("file",  new File(System.getProperty("user.home") + "/mavenrepo/release"))),
    resolvers += "JBoss Maven Repo" at "https://repository.jboss.org/nexus/content/repositories/releases",
    resolvers += "JBoss 3rdParty Maven repo" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases",
    resolvers += "BaseX Maven Repo" at "http://files.basex.org/maven",
    resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/mavenrepo/release",
    resolvers += "JSpace Maven Repo" at "http://10.155.87.253:8080/mavenrepo/release"
  ) ++ Revolver.settings ++ jacoco.settings ++ instrumentSettings ++ scalariformSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ XitrumPackage.copy() ++ YangPlugin.yangSettings

  lazy val root = Project("jspace-ems", file("."), settings = gSettings ++ XitrumPackage.copy("configuration", "bin/run.sh", "bin/run.bat") ++ Seq(publishArtifact := false)).aggregate(server, deviceMgt, emsBoot)

  lazy val server = Project("jspace-ems-server", file("server"), settings = gSettings)

  lazy val deviceMgt = Project("jspace-device-mgt", file("imp-device-mgt"), settings = gSettings).dependsOn(server)

  lazy val emsBoot = Project("jspace-ems-boot", file("ems-boot"), settings = gSettings).dependsOn(server, deviceMgt)

}
