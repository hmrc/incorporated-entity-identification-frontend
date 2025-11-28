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
import uk.gov.hmrc.http.HttpResponse
import play.api.http.Status._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.RemoveIncorporatedEntityDetailsHttpParser
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.RemoveIncorporatedEntityDetailsHttpParser.SuccessfullyRemoved

class RemoveIncorporatedEntityDetailsHttpParserSpec extends UnitSpec {

  private val reads = RemoveIncorporatedEntityDetailsHttpParser.RemoveIncorporatedEntityDetailsHttpReads

  "RemoveIncorporatedEntityDetailsHttpReads" should {
    "return SuccessfullyRemoved when status is 204" in {
      val resp = HttpResponse(status = NO_CONTENT, body = "", headers = Map.empty)
      val out = reads.read("DELETE", "/url", resp)
      out mustBe SuccessfullyRemoved
    }

    "throw InternalServerException otherwise and include status and body" in {
      val resp = HttpResponse(status = INTERNAL_SERVER_ERROR, body = "\"Failed to remove field\"", headers = Map.empty)
      val ex = intercept[InternalServerException] {
        reads.read("DELETE", "/url", resp)
      }
      ex.getMessage must include ("Status - 500")
      ex.getMessage must include ("Failed to remove field")
    }
  }
}
