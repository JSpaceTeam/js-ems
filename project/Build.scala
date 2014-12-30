import java.io.File
import java.util.jar.JarFile

import com.typesafe.sbt.SbtScalariform._
import com.typesafe.sbteclipse.core.EclipsePlugin.{EclipseCreateSrc, EclipseKeys}
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbt.Keys._
import sbt._
import scoverage.ScoverageSbtPlugin._
import spray.revolver.RevolverPlugin.Revolver
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object Build extends Build {

  val akkaV = "2.3.7"

  val sprayV = "1.3.2"

  var spray_jsonV = "1.2.6"

  val yangPackageName = SettingKey[Option[String]]("yang-package-name")

  var gSettings = Defaults.coreDefaultSettings ++ Seq(
    scalaVersion  := "2.11.4",
    organization  := "net.juniper.productname",
    version       := "0.1.1",
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= Seq(
      "io.spray"            %%  "spray-can"     % sprayV,
      "io.spray"            %%  "spray-routing" % sprayV,
      "io.spray"            %%  "spray-client"  % sprayV,
      "io.spray"            %%  "spray-json"    % spray_jsonV,
      "io.spray"            %%  "spray-testkit" % sprayV  % "test",
      "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
      "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
      "org.specs2"          %%  "specs2-core"   % "2.3.11",
      "net.juniper"         %% "easy-rest-core" % "0.1",
      "net.juniper"         %% "easy-rest-persistence" % "0.1",
      "net.juniper"         %% "easy-rest-orm" % "0.1",
      "net.juniper"         % "jnc-library" % "0.1.1",
      "ch.qos.logback"      %   "logback-classic" % "1.1.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
    ),
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed,
    ScoverageKeys.minimumCoverage := 70,
    ScoverageKeys.failOnMinimumCoverage := false,
    ScoverageKeys.highlighting := {
      if (scalaBinaryVersion.value == "2.10") false
      else false
    },
    publishMavenStyle := true,
    publishTo := Some(Resolver.file("file",  new File("../mavenrepo/release"))),
    resolvers += "JSpace Maven Repo" at "https://raw.github.com/JSpaceTeam/mavenrepo/master/release",
    yangPackageName := Option("net.juniper.yang")
  ) ++ Revolver.settings ++ jacoco.settings ++ instrumentSettings ++ scalariformSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ XitrumPackage.copy()

  lazy val root = Project("js-ems", file("."), settings = gSettings ++ XitrumPackage.copy("configuration", "runner.sh", "runner.bat")).aggregate(emsDevice)

  lazy val emsDevice = Project("ems-device", file("ems-device"), settings = gSettings ++ yangSettings)
  /**
   * Generate code from YANG via JNC
   */
  val Yang = config("yang")
  val yangGenerate = TaskKey[Seq[File]]("yang-generate")

  def yangGeneratorTask: Def.Initialize[Task[Seq[File]]] = Def.task {
    val cachedCompile = FileFunction.cached(streams.value.cacheDirectory / "yang", FilesInfo.lastModified, FilesInfo.exists) {
      in: Set[File] =>
        extractYangDependencies((managedClasspath in Runtime).value ++ (unmanagedClasspath in Runtime).value, streams.value.log)
        runJncGen(
          srcFiles = in,
          srcDir = (resourceDirectory in Yang).value,
          targetBaseDir = (javaSource in Yang).value,
          log = streams.value.log,
          packageName = (yangPackageName in Yang).value,
          dependencyModules = (projectDependencies in Compile).value,
          moduleRoot = baseDirectory.value.getAbsolutePath
        )
    }
    cachedCompile(((resourceDirectory in Yang).value ** "*.yang").get.toSet).toSeq
  }

  def runJncGen( srcFiles: Set[File],
                 srcDir: File,
                 targetBaseDir: File,
                 log: Logger,
                 packageName: Option[String], dependencyModules: Seq[sbt.ModuleID], moduleRoot: String): Set[File] = {
    val targetDir = packageName.map {
      _.split('.').foldLeft(targetBaseDir) {
        _ / _
      }
    }.getOrElse(targetBaseDir)
    val baseArgs = Seq("-p", getYangDependencies(dependencyModules, moduleRoot), "-f", "jnc", "-d", targetDir.getAbsolutePath, "--jnc-no-pkginfo", "--jnc-classpath-schema-loading")
    srcFiles map { file =>
      println("generating :" + file.toString)
      val args = baseArgs ++ Seq(file.toString)
      val exitCode = Process("pyang", args) ! log
      if (exitCode != 0) sys.error(s"pyang failed with exit code $exitCode")
    }
    (targetDir ** "*.java").get.toSet ++ (targetDir ** "*.scala").get.toSet
  }

  def extractYangDependencies(libs: Seq[Attributed[File]], log: Logger): Unit = {
    val tmpDir = System.getProperty("user.home") + "/tmp"
    val yangDir = tmpDir + "/yang"
    val args = Seq("-rf", yangDir)
    Process("rm", args) ! log
    new File(yangDir).mkdirs()

    libs.foreach { lib =>
      val file = lib.data
      if (file.exists) {
        if (!file.isDirectory && file.getName.endsWith(".jar")) {
          val jarFile = new JarFile(file)
          val yangEntry = jarFile.getEntry("yang")
          if (yangEntry != null) {
            val entries = jarFile.entries
            for (entry <- entries) {
              if(entry.getName.startsWith("yang/") && entry.getName.endsWith(".yang")) {
                val targetFile = new File(tmpDir + "/" + entry.getName)
                IO.write(targetFile, IO.readBytes(jarFile.getInputStream(entry)))
              }
            }
          }
        }
      }
    }
  }

  def getYangDependencies(modules: Seq[sbt.ModuleID], moduleRoot: String): String = {
    val dArr = ArrayBuffer[String]()
    //add dependency path
    dArr += System.getProperty("user.home") + "/tmp/yang"

    //add dependency module paths
    for (module <- modules)
    dArr += root.base.getAbsolutePath + "/" + module.name + "/src/main/resources/yang"

    //add current module path
    dArr += moduleRoot + "/src/main/resources/yang"

    dArr.mkString(":")
  }

  val yangSettings = inConfig(Yang)(Seq(
    resourceDirectory <<= (resourceDirectory in Compile) {_ / "yang"},
    javaSource <<= sourceManaged in Compile,
    yangGenerate <<= yangGeneratorTask,
    yangPackageName <<= yangPackageName in Yang
  )) ++ Seq(
    managedSourceDirectories in Compile <+= (javaSource in Yang),
    sourceGenerators in Compile <+= (yangGenerate in Yang),
    cleanFiles <+= (javaSource in Yang)
  )

}
