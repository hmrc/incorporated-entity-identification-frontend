package uk.gov.hmrc.incorporatedentityidentificationfrontend.utils

import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse

trait CustomMatchers {
  def httpStatus(expectedValue: Int): HavePropertyMatcher[WSResponse, Int] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.status == expectedValue,
      "httpStatus",
      expectedValue,
      response.status
    )

  def jsonBodyAs[T](expectedValue: T)(implicit reads: Reads[T]): HavePropertyMatcher[WSResponse, T] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.json.as[T] == expectedValue,
      "jsonBodyAs",
      expectedValue,
      response.json.as[T]
    )

  val emptyBody: HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.body == "",
      "emptyBody",
      "",
      response.body
    )

  def redirectUri(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val redirectLocation: Option[String] = response.header("Location")

      val matchCondition = redirectLocation.exists(_.contains(expectedValue))
      HavePropertyMatchResult(
        matchCondition,
        "redirectUri",
        expectedValue,
        redirectLocation.getOrElse("")
      )
    }
}
