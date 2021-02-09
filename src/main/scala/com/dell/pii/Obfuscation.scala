package com.dell.pii

import com.dell.pii.IO.Dictionary
import com.dell.pii.columns.{Anonymizer, Generalizer, Pseudonymizer}
import com.dell.pii.model.protection.ProtectionDescription
import com.dell.pii.model.`enum`.ObfuscationMethod
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row}

import scala.util.{Failure, Success}

/** Obfuscation of Data via Nullify, Anonymize, Generalization or Replacement */
object Obfuscation extends SharedSparkSession {

  /**
   * Run the obfuscation techniques over data from a policy configuration
   *
   * @param data   data to obfuscate
   * @param config policy to apply over data
   * @return obfuscated data
   */
  def run(data: DataFrame, config: List[ProtectionDescription]): DataFrame = {
    val obfuscated = config.foldLeft(data)((df, col) => obfuscate(col, df))
    obfuscated.select(data.columns.head, data.columns.tail: _*)
  }

  private[pii] def obfuscate(protection: ProtectionDescription, df: DataFrame): DataFrame =
    protection.obfuscation match {
      case ObfuscationMethod.PSEUDONYMIZE => pseudonymize(df, protection)
      case ObfuscationMethod.GENERALIZE   => generalize(df, protection)
      case ObfuscationMethod.ANONYMIZE    => replace(df, protection)
    }

  private def replace(df: DataFrame, config: ProtectionDescription): DataFrame =
    df.transform(
      Anonymizer.AnonymizeColumns(
        config.value.getOrElse(throw new NoSuchElementException("Missing Value for Replace Rule")),
        config.columns: _*
      )
    )

  private def generalize(df: DataFrame, config: ProtectionDescription): DataFrame = {
    val generalizer = new Generalizer(
      config.split.getOrElse(throw new NoSuchElementException("Missing Value for Replace Rule")).doubleValue
    )
    val generalizerFold = (data: DataFrame, column: String) => data.transform(generalizer.generalize(column))

    config.columns.foldLeft(df)((data, column) => generalizerFold(data, column))
  }

  private def pseudonymize(df: DataFrame, config: ProtectionDescription): DataFrame = {

    val dict: DataFrame = Dictionary.readDictionary(
      config.dictionary.getOrElse(throw new NoSuchElementException("Missing Dictionary Path"))
    ) match {
      case Success(data) => data
      case Failure(_)    => createDictionary()
    }

    config.columns.foldLeft(df) { (data, column) =>
      val (pseudonmyized, tokens) = Pseudonymizer.PseudonymizeColumn(data, dict, column)
      Dictionary.updateDictionary(config.dictionary.get, tokens)
      pseudonmyized
    }
  }

  private def createDictionary(): DataFrame = {
    val schema = StructType(
      List(
        StructField("Key", StringType),
        StructField("Token", StringType)
      )
    )

    spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
  }
}
