package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor}

case class NodeHeader(
                       id: String,
                       parentId: String,
                       version: Byte,
                       height: Long,
                       nBits: Long,
                       difficulty: NodeDifficulty,
                       timestamp: Long,
                       stateRoot: String,
                       adProofsRoot: String,
                       transactionsRoot: String,
                       extensionHash: String,
                       equihashSolutions: String,
                       interlinks: List[String]
                     )

object NodeHeader {

  implicit val decoder: Decoder[NodeHeader] = (c: HCursor) => for {
    id <- c.downField("id").as[String]
    parentId <- c.downField("parentId").as[String]
    version <- c.downField("version").as[Byte]
    height <- c.downField("height").as[Long]
    nBits <- c.downField("nBits").as[Long]
    difficulty <- c.downField("difficulty").as[NodeDifficulty]
    timestamp <- c.downField("timestamp").as[Long]
    stateRoot <- c.downField("stateRoot").as[String]
    adProofsRoot <- c.downField("adProofsRoot").as[String]
    transactionsRoot <- c.downField("transactionsRoot").as[String]
    extensionHash <- c.downField("extensionHash").as[String]
    equihashSolutions <- c.downField("equihashSolutions").as[String]
    interlinks <- c.downField("interlinks").as[List[String]]
  } yield NodeHeader(
    id,
    parentId,
    version,
    height,
    nBits,
    difficulty,
    timestamp,
    stateRoot,
    adProofsRoot,
    transactionsRoot,
    extensionHash,
    equihashSolutions,
    interlinks
  )



}
