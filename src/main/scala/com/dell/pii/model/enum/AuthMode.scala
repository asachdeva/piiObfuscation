package com.dell.pii.model.`enum`

import com.fasterxml.jackson.core.`type`.TypeReference

/** Authentication Mode Enumeration */
object AuthMode extends Enumeration {
  type Auth = Value
  val KEY, SERVICE_PRINCIPLE, IAM, NONE = Value
}

class AuthMode extends TypeReference[AuthMode.type]
