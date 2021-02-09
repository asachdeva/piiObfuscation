package com.dell.pii.model.protection

import com.dell.pii.model.common.DataSource

case class ProtectionPolicy(
  version: String,
  name: String,
  source: DataSource,
  destination: DataSource,
  protections: List[ProtectionDescription]
)
