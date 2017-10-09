import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, rdd}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming._

import scala.io.Source

object TwitterStreaming {
  def main(args: Array[String]): Unit = {

    if (args.length < 4) {
      System.err.println("Twitter Keys:" + args.length +
        "\nUsage: TwitterPopularTags <consumer key> <consumer secret>" +
        "<access token> <access token secret> [<filters>]")
      System.exit(1)
    }

    LogUtils.setStreamingLogLevels()

    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)

    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val conf = new SparkConf()
    conf.setAppName("Streaming Twitter Data Using Apache Spark")
    conf.setMaster("local[*]")

    val filters = Source.fromFile("src/main/resources/filters.txt").getLines().toArray

    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(30))
    val stream = TwitterUtils.createStream(ssc, None, filters)

    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val tweets = stream.map(s=> ("" -> s.getText, "" -> s.getRetweetCount.toString, "" -> s.isFavorited, "" -> s.isTruncated, "" -> s.getId.toString, "" -> s.getInReplyToScreenName, "" -> s.getSource.toString, "" -> s.isRetweetedByMe))
    tweets.foreachRDD(rdd => rdd.toDF().write
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save("newdata.csv")
    )


    ssc.start()
    ssc.awaitTermination()

  }
}