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
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.IncorporatedEntityIdentificationStorageHttpParser
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.SuccessfullyStored

class IncorporatedEntityIdentificationStorageHttpParserSpec extends UnitSpec {

  private val reads = IncorporatedEntityIdentificationStorageHttpParser.IncorporatedEntityIdentificationStorageHttpReads

  "IncorporatedEntityIdentificationStorageHttpReads" should {
    "return SuccessfullyStored when 200" in {
      val resp = HttpResponse(status = OK, body = "", headers = Map.empty)
      reads.read("POST", "/url", resp) mustBe SuccessfullyStored
    }
    "throw InternalServerException otherwise" in {
      val resp = HttpResponse(status = BAD_GATEWAY, body = "oops", headers = Map.empty)
      val ex = intercept[InternalServerException] { reads.read("POST", "/url", resp) }
      ex.getMessage must include ("Storage in Incorporated Entity Identification failed with status")
    }
  }
}
