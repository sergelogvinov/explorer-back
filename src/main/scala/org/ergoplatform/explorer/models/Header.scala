package org.ergoplatform.explorer.models

case class Header(
                  id: String,
                  parentId: String,
                  version: Short,
                  height: Int,
                  adProofsRoot: String,
                  stateRoot: String,
                  transactionsRoot: String,
                  timestamp: Long,
                  nBits: Long,
                  extensionHash: String,
                  blockSize: Long,
                  equihashSolution: List[Int] = List.empty[Int],
                  adProofs: Option[Array[Byte]] = None
                ) extends Entity[String]
