package com.dell.pii.IO

import com.dell.pii.SharedSparkSession
import com.dell.pii.model.common.DataSource
import com.dell.pii.model.enum.FileFormat._
import org.apache.spark.sql.DataFrame

object DataReader extends DataIO with SharedSparkSession {

  def readData(source: DataSource): DataFrame = readData(source, source.location)

  def readData(source: DataSource, path: String): DataFrame = {
    setConfig(source)

    source.fileFormat match {
      case JSON    => readJSON(path, source)
      case CSV     => readCSV(path, source)
      case PARQUET => readParquet(path)
      case DELTA   => readDelta(path)
    }
  }

  private def readJSON(path: String, source: DataSource): DataFrame = spark.read.json(path)
  private def readCSV(path: String, source: DataSource): DataFrame  = spark.read.csv(path)
  private def readParquet(path: String)                             = spark.read.parquet(path)

  private def readDelta(path: String): DataFrame                    =
    //noinspection ScalaCustomHdfsFormat
    spark.read.format("delta").load(path)
}
