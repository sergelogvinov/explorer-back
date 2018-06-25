package org.ergoplatform.explorer.http.directives.handlers

import akka.http.scaladsl.model.StatusCodes
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.handlers.SearchHandler
import org.ergoplatform.explorer.http.protocol.{MinerInfo, SearchBlock}
import org.ergoplatform.explorer.services.{AddressesService, BlockService, TransactionsService}
import org.mockito.Mockito._

class SearchHandlerSpec extends HandlerSpec {

  val blockService = mock[BlockService[IO]]
  val transactionService = mock[TransactionsService[IO]]
  val addressService = mock[AddressesService[IO]]

  private def response(blocks: List[SearchBlock], transactionIds: List[String], addressIds: List[String]) =
    Json.obj(
      "blocks" -> blocks.asJson,
      "transactions" -> transactionIds.asJson,
      "addresses" -> addressIds.asJson
    )

  val prefix = "/search?query="
  val queryNoMatch = "0123456789ABCDEF"
  val querySingleMatch = "11111"

  val miner = MinerInfo("mock.miner.address", "Mock miner")
  val blockSingleMatch = List(SearchBlock("123123" + querySingleMatch, 1, System.currentTimeMillis(), 42, miner, 100))
  val transactionSingleMatch = List("fff123" + querySingleMatch)
  val addressSingleMatch = List("add123" +  querySingleMatch)

  when(blockService.searchById(queryNoMatch)).thenReturn(IO(Nil))
  when(transactionService.searchById(queryNoMatch)).thenReturn(IO(Nil))
  when(addressService.searchById(queryNoMatch)).thenReturn(IO(Nil))

  when(blockService.searchById(querySingleMatch)).thenReturn(IO(blockSingleMatch))
  when(transactionService.searchById(querySingleMatch)).thenReturn(IO(transactionSingleMatch))
  when(addressService.searchById(querySingleMatch)).thenReturn(IO(addressSingleMatch))


  val route = new SearchHandler(blockService, transactionService, addressService).route

  it should "return result" in {
    Get(prefix + querySingleMatch) ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe response(blockSingleMatch, transactionSingleMatch, addressSingleMatch)
    }
  }

  it should "not fail when no results found" in {
    Get(prefix + queryNoMatch) ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe response(Nil, Nil, Nil)
    }
  }

}
