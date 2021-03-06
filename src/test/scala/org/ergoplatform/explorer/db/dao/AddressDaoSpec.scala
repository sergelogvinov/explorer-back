package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.models.AddressSummaryData
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class AddressDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "get correct address data" in {

    val (h, _, tx, inputs, outputs, _) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()


    val randomHash = Random.shuffle(outputs).head.hash

    val related = outputs.filter(_.hash == randomHash)

    val spent = related.filter{o => inputs.map(_.boxId).contains(o.boxId)}.map(_.value).sum
    val unspent = related.filterNot{o => inputs.map(_.boxId).contains(o.boxId)}.map(_.value).sum
    val txsCount = tx.count(tx => related.map(_.txId).contains(tx.id))

    val expected = AddressSummaryData(randomHash, txsCount, spent, unspent)

    val dao = new AddressDao

    dao.getAddressData(randomHash).transact(xa).unsafeRunSync() shouldBe expected
  }

}
