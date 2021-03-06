package org.ergoplatform.explorer.services

import cats.effect.IO
import doobie.implicits._
import org.ergoplatform.explorer.db.dao.{HeadersDao, InputsDao, OutputsDao, TransactionsDao}
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.http.protocol.AddressInfo
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class AddressesServiceSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "search address by substring and get address info by full id" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, _, tx, inputs, outputs, _) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()

    val service = new AddressesServiceIOImpl[IO](xa, ec)

    val random = Random.shuffle(outputs).head.hash


    val addressInfo = service.getAddressInfo(random).unsafeRunSync()

    val expected = {
      val id = random
      val txsCount = outputs.count(_.hash == random)
      val totalReceived = outputs.filter(_.hash == random).map(_.value).sum
      val inputsBoxIds = inputs.map(_.boxId)
      val balance = outputs.filter{o => o.hash == random && !inputsBoxIds.contains(o.boxId)}.map(_.value).sum
      AddressInfo(id, txsCount, totalReceived, balance)
    }

    addressInfo shouldBe expected


    val random2 = Random.shuffle(outputs).head.hash.take(6)

    val expected2 = outputs.filter(_.hash.startsWith(random2)).map(_.hash)
    val searchResults = service.searchById(random2).unsafeRunSync()

    searchResults should contain theSameElementsAs expected2
  }
}
