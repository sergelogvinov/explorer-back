package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.Transaction

case class TransactionSummaryInfo(id: String,
                                  timestamp: Long,
                                  size: Int,
                                  confirmationsCount: Int,
                                  miniBlockInfo: MiniBlockInfo)

object TransactionSummaryInfo {

  import org.ergoplatform.explorer.utils.Converter._

  def fromDb(tx: Transaction, height: Int): TransactionSummaryInfo = TransactionSummaryInfo(
    id = from16to58(tx.id),
    miniBlockInfo = MiniBlockInfo(tx.blockId, height),
    //TODO Need to add this data to tx
    timestamp = System.currentTimeMillis(),
    size = 0,
    confirmationsCount = 0
  )

  implicit val encoder: Encoder[TransactionSummaryInfo] = (ts: TransactionSummaryInfo) => Json.obj(
    ("id", Json.fromString(ts.id)),
    ("timestamp", Json.fromLong(ts.timestamp)),
    ("size", Json.fromInt(ts.size)),
    ("confirmationsCount", Json.fromInt(ts.confirmationsCount)),
    ("block", ts.miniBlockInfo.asJson)
  )

}
