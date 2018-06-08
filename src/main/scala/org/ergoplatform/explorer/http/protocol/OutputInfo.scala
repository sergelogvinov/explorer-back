package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Output

case class OutputInfo(id: String, value: Long, script: String, hash: String, spent: Boolean)

object OutputInfo {


  def apply(o: Output): OutputInfo = OutputInfo(
    o.id,
    o.value,
    o.script,
    if (o.hash.startsWith("cd0703")) {o.hash} else { "Unable to decode output address."},
    o.spent
  )

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    "id" -> Json.fromString(o.id),
    "value" -> Json.fromLong(o.value),
    "script" -> Json.fromString(o.script),
    "hash" -> Json.fromString(o.hash),
    "spent" -> Json.fromBoolean(o.spent)
  )
}
