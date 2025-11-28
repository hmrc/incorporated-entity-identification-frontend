/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package httpparsers

import utils.UnitSpec
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.ValidateIncorporatedEntityDetailsHttpParser
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{DetailsMatched, DetailsMismatch, DetailsNotFound, IncorporatedEntityDetailsMatching}

class ValidateIncorporatedEntityDetailsHttpParserSpec extends UnitSpec {

  private val reads = ValidateIncorporatedEntityDetailsHttpParser.ValidateIncorporatedEntityDetailsHttpReads

  "ValidateIncorporatedEntityDetailsHttpReads" should {
    "return DetailsMatched when 200 and matched=true" in {
      val resp = HttpResponse(status = OK, json = Json.obj("matched" -> true), headers = Map.empty)
      reads.read("POST", "/url", resp) mustBe DetailsMatched
    }

    "return DetailsMismatch when 200 and matched=false" in {
      val resp = HttpResponse(status = OK, json = Json.obj("matched" -> false), headers = Map.empty)
      reads.read("POST", "/url", resp) mustBe DetailsMismatch
    }

    "throw when 200 but matched missing or invalid" in {
      val resp = HttpResponse(status = OK, json = Json.obj("m" -> 1), headers = Map.empty)
      val ex = intercept[InternalServerException] { reads.read("POST", "/url", resp) }
      ex.getMessage must include ("Invalid response from validate incorporated entity details")
    }

    "return DetailsNotFound when 400 and code NOT_FOUND" in {
      val resp = HttpResponse(status = BAD_REQUEST, json = Json.obj("code" -> "NOT_FOUND"), headers = Map.empty)
      reads.read("POST", "/url", resp) mustBe DetailsNotFound
    }

    "throw when 400 with other code" in {
      val resp = HttpResponse(status = BAD_REQUEST, json = Json.obj("code" -> "OTHER"), headers = Map.empty)
      val ex = intercept[InternalServerException] { reads.read("POST", "/url", resp) }
      ex.getMessage must include ("Invalid response from validate incorporated entity details")
    }

    "throw when non-200/400 status" in {
      val resp = HttpResponse(status = BAD_GATEWAY, body = "oops", headers = Map.empty)
      val ex = intercept[InternalServerException] { reads.read("POST", "/url", resp) }
      ex.getMessage must include ("Invalid response from validate incorporated entity details")
    }
  }
}
