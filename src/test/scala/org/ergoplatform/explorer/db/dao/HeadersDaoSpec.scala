package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.utils.generators.HeadersGen
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import scorex.crypto.encode.Base16

import scala.util.Random

class HeadersDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert,update,select from db" in {

    val dao = new HeadersDao


    val headers = HeadersGen
      .generateHeaders(20)
      .map { h => h.copy(adProofs = None) }
      .reverse
      .zipWithIndex.map { case (h, ts) => h.copy(timestamp = ts.toLong)}

    val head = headers.head
    val tail = headers.tail

    val wrongId = Base16.encode(Array.fill(33)(1: Byte))


    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() shouldBe tail

    dao.getHeightById(head.id).transact(xa).unsafeRunSync() shouldBe head.height
    dao.getByParentId(head.id).transact(xa).unsafeRunSync() shouldBe tail.head

    val rnd1 = Random.shuffle(headers).head
    dao.getHeightById(rnd1.id).transact(xa).unsafeRunSync() shouldBe rnd1.height
    dao.getByParentId(rnd1.parentId).transact(xa).unsafeRunSync() shouldBe rnd1

    dao.find(wrongId).transact(xa).unsafeRunSync() shouldBe None
    dao.findByParentId(wrongId).transact(xa).unsafeRunSync() shouldBe None
    dao.find(head.id).transact(xa).unsafeRunSync() shouldBe Some(head)

    the[NoSuchElementException] thrownBy dao.get(wrongId).transact(xa).unsafeRunSync()
    the[NoSuchElementException] thrownBy dao.getByParentId(wrongId).transact(xa).unsafeRunSync()
    dao.get(head.id).transact(xa).unsafeRunSync() shouldBe head

    dao.count.transact(xa).unsafeRunSync() shouldBe headers.length

    dao.getLast(2).transact(xa).unsafeRunSync() shouldBe headers.sortBy(v => -v.height).take(2)

    dao.list(0, 100, "height", "ASC", 0L, 1000L).transact(xa).unsafeRunSync() shouldBe headers
    dao.list(0, 2, "height", "ASC", 0L, 1000L).transact(xa).unsafeRunSync() shouldBe headers.take(2)
    dao.list(1, 2, "height", "ASC", 0L, 1000L).transact(xa).unsafeRunSync() shouldBe tail.take(2)
    dao.list(0, 100, "height", "ASC", 0L, 2L).transact(xa).unsafeRunSync() shouldBe headers.take(3)
    dao.list(0, 100, "height", "DESC", 0L, 1000L).transact(xa).unsafeRunSync() shouldBe headers.reverse
  }
}
