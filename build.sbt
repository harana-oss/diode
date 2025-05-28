import sbt.Keys._
import sbt._
// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import Util._

ThisBuild / scalafmtOnCompile := true

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion       := "3.7.0"

val commonSettings = Seq(
  scalacOptions := Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.8.5" % "test"
  ),
)

inThisBuild(
  List(
    homepage            := Some(url("https://github.com/suzaku-io/diode")),
    licenses            := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    sonatypeProfileName := "io.suzaku",
    developers := List(
      Developer("ochrons", "Otto Chrons", "", url("https://github.com/ochrons"))
    ),
    organization := "io.suzaku",
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/suzaku-io/diode"),
        "scm:git:git@github.com:suzaku-io/diode.git",
        Some("scm:git:git@github.com:suzaku-io/diode.git")
      )
    ),
    Test / publishArtifact := false
  )
)

val sourceMapSetting: Def.Initialize[Option[String]] = Def.settingDyn(
  if (isSnapshot.value) Def.setting(None)
  else {
    val a   = baseDirectory.value.toURI.toString.replaceFirst("[^/]+/?$", "")
    val g   = "https://raw.githubusercontent.com/suzaku-io/diode"
    val uri = s"$a->$g/v${version.value}/${name.value}/"
    scalaVerDependent {
      case (2, _) => s"-P:scalajs:mapSourceURI:$uri"
      case (3, _) => s"-scalajs:mapSourceURI:$uri"
    }
  }
)

val commonJsSettings = Seq(
  scalacOptions += sourceMapSetting.value,
)

lazy val diodeCore = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("diode-core"))
  .settings(commonSettings*)
  .settings(
    name := "diode-core",
  )
  .jsSettings(commonJsSettings*)

lazy val diodeData = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("diode-data"))
  .settings(commonSettings*)
  .settings(
    name := "diode-data"
  )
  .jsSettings(commonJsSettings*)
  .dependsOn(diodeCore)

lazy val diode = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("diode"))
  .settings(commonSettings*)
  .settings(
    name := "diode",
    test := {}
  )
  .dependsOn(diodeCore, diodeData)

lazy val diodeDevtools = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("diode-devtools"))
  .settings(commonSettings*)
  .settings(
    name := "diode-devtools"
  )
  .jsSettings(commonJsSettings*)
  .jsSettings(
    libraryDependencies ++= Seq("org.scala-js" %%% "scalajs-dom" % "2.8.0")
  )
  .dependsOn(diodeCore)