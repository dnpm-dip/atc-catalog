
/*
 build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt
*/


name := "atc"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "1.0-SNAPSHOT"


//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------

lazy val global = project
  .in(file("."))
  .settings(
    settings,
    publish / skip := true
  )
  .aggregate(
     impl,
     catalogs_packaged,
     tests
  )


lazy val impl = project
  .settings(
    name := "atc-impl",
    settings,
    libraryDependencies ++= Seq(
      dependencies.model,
      dependencies.scalatest,
    )
  )


lazy val catalogs_packaged = project
  .settings(
    name := "atc-catalogs-packaged",
    settings,
    libraryDependencies ++= Seq()
  )
  .dependsOn(impl)



lazy val tests = project
  .settings(
    name := "tests",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
    ),
    publish / skip := true
  )
  .dependsOn(
    impl % Test,
    catalogs_packaged % Test,
  )


//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------

lazy val dependencies =
  new {
    val scalatest    = "org.scalatest"  %% "scalatest"  % "3.1.1" % Test
    val slf4j        = "org.slf4j"      %  "slf4j-api"  % "1.7.32"
    val model        = "de.dnpm.dip"    %% "core"       % "1.0-SNAPSHOT"
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings

lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-feature",
//  "-language:existentials",
//  "-language:higherKinds",
//  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-deprecation",
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++=
    Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

