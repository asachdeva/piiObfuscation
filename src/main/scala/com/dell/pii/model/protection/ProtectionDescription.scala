package com.dell.pii.model.protection

import com.dell.pii.model.common.DataSource
import com.dell.pii.model.`enum`.ObfuscationMethod
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

case class ProtectionDescription(
  @JsonScalaEnumeration(classOf[ObfuscationMethod]) obfuscation: ObfuscationMethod.Obfuscation,
  columns: List[String],
  dictionary: Option[DataSource],
  split: Option[Number],
  value: Option[String]
)
