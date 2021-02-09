package com.dell.pii.IO

import com.dell.pii.model.common.DataSource
import com.dell.pii.model.`enum`.FileFormat
import org.apache.spark.sql.{DataFrame, SaveMode}

object DataWriter extends DataIO {

  def writeData(source: DataSource, data: DataFrame): Unit = writeData(source, data, source.location)

  def writeData(source: DataSource, data: DataFrame, path: String): Unit = {
    setConfig(source)

    source.fileFormat match {
      case FileFormat.JSON    => writeJSON(path, data, source)
      case FileFormat.CSV     => writeCSV(path, data, source)
      case FileFormat.PARQUET => writeParquet(path, data, source)
      case FileFormat.DELTA   => writeDelta(path, data)
    }
  }

  private def writeJSON(path: String, data: DataFrame, source: DataSource) =
    data.write.mode(SaveMode.Overwrite).json(path)

  private def writeCSV(path: String, data: DataFrame, source: DataSource) =
    data.write.mode(SaveMode.Overwrite).csv(path)

  private def writeParquet(path: String, data: DataFrame, source: DataSource) =
    data.write.mode(SaveMode.Overwrite).parquet(path)

  private def writeDelta(path: String, data: DataFrame): Unit =
    //noinspection ScalaCustomHdfsFormat
    data.write.mode(SaveMode.Overwrite).format("delta").save(path)
}
