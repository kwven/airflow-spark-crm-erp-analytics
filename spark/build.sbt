import sbtassembly.AssemblyPlugin.autoImport._

ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "crm-erp-analytic-spark",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % "3.5.8" % "provided",
      "com.typesafe" % "config" % "1.4.3",
      "org.postgresql" % "postgresql" % "42.7.7"
    ),
    assembly / assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar",
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("META-INF", "services", _ @ _*) => MergeStrategy.concat
      case PathList("META-INF", _ @ _*) => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )
