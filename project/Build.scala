/*
NEED HELP WITH SETTING uTest.
ON js/test -> [error] (js/test:executeTests) The Scala.js framework only works with a class loader of type JSClasspathLoader (class sbt.classpath.ClasspathFilter given)
-- sbt ;jvm/clean ;test
*/


import sbt._
import sbt.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import sbt.TestFramework
import scala.scalajs.sbtplugin.testing.JSClasspathLoader


object MyBuild extends Build {
  val sharedSettings = Defaults.defaultSettings ++ Seq(
    organization := "mntkris",
    version := "0.0.1",
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq("2.11.1"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full),
    scalacOptions ++= Seq()
  )

  val sharedDirs = Seq(
    unmanagedSourceDirectories in Compile <+= baseDirectory(_ / ".." / "main" / "src" / "main" / "scala"),
    unmanagedSourceDirectories in Test    <+= baseDirectory(_ / ".." / "main" / "src" / "test" / "scala")
  )


  lazy val dovire: Project = Project(
    "dovire",
    file("."),
    settings = sharedSettings ++ Seq(
    )
  ).aggregate(abstractions, jvm, js)

  //macros, macro annotations, base traits
  lazy val abstractions: Project = Project(
    "abstractions",
    file("abstractions"),
    settings = sharedSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _))
  )

  lazy val jvm: Project = Project(
    "jvm",
    file("jvm"),
    settings = sharedSettings ++ sharedDirs ++ Seq (
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "utest" % "0.1.6" % "test"
      ),
      testFrameworks += new TestFramework("utest.runner.JvmFramework")
    )
  ).dependsOn(abstractions)

  lazy val js: Project = Project(
    "js",
    file("js"),
    settings = scalaJSSettings ++ sharedSettings ++ sharedDirs ++ Seq (
      libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "utest" % "0.1.6" % "test",
        "org.webjars" % "bignumber" % "1.3.0",
        "org.webjars" % "momentjs" % "2.7.0"
      ),
      ScalaJSKeys.jsDependencies ++= Seq(
        "org.webjars" % "bignumber" % "1.3.0" / "bignumber.js",
        "org.webjars" % "momentjs" % "2.7.0" / "moment.js"
      ),
      (loadedTestFrameworks in Test) := {
        (loadedTestFrameworks in Test).value.updated(
          sbt.TestFramework(classOf[utest.jsrunner.JsFramework].getName),
          new utest.jsrunner.JsFramework(environment = (ScalaJSKeys.jsEnv in Test).value)
        )
      },
      testLoader := JSClasspathLoader((ScalaJSKeys.execClasspath in Compile).value)
    )
  ).dependsOn(abstractions)
}
