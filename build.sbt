import Assembly._
import AssemblyPlugin._


name := "StreamingTwitterSpark"
version := "0.1"
scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % "1.5.2",
  "org.apache.spark" % "spark-sql_2.10" % "1.5.2",
  "org.apache.spark" % "spark-streaming_2.10" % "1.5.2",
  "org.apache.spark" % "spark-streaming-twitter_2.10" % "1.5.2",
  "com.databricks" % "spark-csv_2.10" % "1.5.0"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}