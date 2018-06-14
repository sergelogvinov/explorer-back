package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.StatRecord

case class BlockchainInfo(version: String,
                          supply: Long,
                          averageTransactionPerBlock: Long,
                          hashRate: Long)

object BlockchainInfo {

  def apply(s: StatRecord): BlockchainInfo = BlockchainInfo(
    s.version,
    s.supply,
    s.avgTxsCount,
    s.hashRate
  )

  implicit val encoder: Encoder[BlockchainInfo] = (i: BlockchainInfo) => Json.obj(
    "version" -> Json.fromString(i.version),
    "supply" -> Json.fromLong(i.supply),
    "transactionAverage" -> Json.fromLong(i.averageTransactionPerBlock),
    "hashRate" -> Json.fromLong(i.hashRate)
  )
}
