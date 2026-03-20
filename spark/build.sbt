ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "crm-erp-analytic-spark",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % "3.5.1" % "provided",
      "com.typesafe" % "config" % "1.4.3",
      "org.postgresql" % "postgresql" % "42.7.7"
    )
  )