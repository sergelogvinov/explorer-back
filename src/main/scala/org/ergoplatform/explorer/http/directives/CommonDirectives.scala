package org.ergoplatform.explorer.http.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import cats.data._
import cats.instances.parallel._
import cats.syntax.parallel._
import org.ergoplatform.explorer.utils.SortOrder
import scorex.crypto.encode.Base16

trait CommonDirectives {

  import CommonDirectives._

  val base16Segment: Directive1[String] = pathPrefix(Segment).flatMap(v =>
    v.forall(Base16.Alphabet.toSet.contains) match {
      case true => provide(v)
      case false => reject(base16ValidationError)
    }
  )

  val paging: Directive[(Int, Int)] = parameters(("offset".as[Int] ? 0, "limit".as[Int] ? 20))

  def sorting(fieldMappings: NonEmptyMap[String, String],
              defaultSortBy: Option[String] = None): Directive[(String, SortOrder)] = {
    val mappingKeys = fieldMappings.keys.toNonEmptyList

    def checkSortOrder(s: String): Param[SortOrder] = {
      SortOrder.fromString(s) match {
        case Some(order) => Right(order)
        case None => Left(NonEmptyList.one(malformedSortDirectionParameter(s)))
      }
    }

    def checkSortBy(s: String): Param[String] = {
      fieldMappings.lookup(s.trim.toLowerCase) match {
        case Some(sortBy) => Right(sortBy)
        case None => Left(NonEmptyList.one(malformedSortByParameter(s, mappingKeys)))
      }
    }

    val defaultSortField = defaultSortBy.getOrElse(fieldMappings.head._1)

    parameters(("sortBy" ? defaultSortField, "sortDirection" ? "asc"))
      .tflatMap { case (field: String, order: String) =>
        (checkSortOrder(order), checkSortBy(field)).parMapN { case (order, field) => field -> order } match {
          case Left(rjs) => reject(rjs.toList: _*)
          case Right(params) => tprovide(params)
        }
      }
  }

  val duration: Directive1[Int] = parameters("timespan" ? "all")
    .flatMap{ v => stringToDaysBack(v) match {
      case Some(d) =>
        provide(d)
      case None =>
        reject(malformedTimespanParameter)

      }
    }

  val startEndDate: Directive[(Option[Long], Option[Long])] =
    parameters(("startDate".as[Long].?, "endDate".as[Long].?)).tflatMap { case (s, e) =>

      val check = (for {
        start <- s
        end <- e
      } yield start > end).getOrElse(false)

      if (check) {
        reject(malformedStartEndDateParam)
      } else {
        tprovide((s, e))
      }
    }

}

object CommonDirectives {

  type Param[A] = EitherNel[MalformedQueryParamRejection, A]

  val malformedTimespanParameter = MalformedQueryParamRejection(
    "timespan",
    s"This param should have one of this values 'all', '7days', '30days', '60days', '180days', '1year', '2years'",
    None
  )

  val malformedStartEndDateParam = MalformedQueryParamRejection(
    "startDate",
    s"Start Date can't be greater than End Date",
    None
  )

  val base16ValidationError = ValidationRejection("String isn't a Base16 representation")

  def malformedSortDirectionParameter(value: String) = MalformedQueryParamRejection(
    "sortDirection",
    s"This param could be asc or desc, but got $value",
    None
  )

  def malformedSortByParameter(value: String, availableValues: NonEmptyList[String]) = MalformedQueryParamRejection(
    "sortBy",
    s"This param could be one of ${availableValues.toList.mkString(", ")}, but got $value",
    None
  )

  def stringToDaysBack(s: String): Option[Int] = s.trim.toLowerCase match {
    case "all" => Some(-1)
    case "1day" => Some(1)
    case "7days" => Some(7)
    case "30days" => Some(30)
    case "60days" => Some(60)
    case "180days" => Some(180)
    case "1year" => Some(365)
    case "2years" => Some(730)
    case _ => None
  }
}
