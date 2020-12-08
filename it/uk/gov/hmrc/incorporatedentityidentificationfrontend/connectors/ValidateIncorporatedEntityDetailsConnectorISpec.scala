/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers.{BAD_REQUEST, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.{testCompanyNumber, testCtutr}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.ValidateIncorporatedEntityDetailsHttpParser.{DetailsMatched, DetailsMismatch, DetailsNotFound}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.IncorporatedEntityIdentificationStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class ValidateIncorporatedEntityDetailsConnectorISpec extends ComponentSpecHelper with IncorporatedEntityIdentificationStub {

  private val validateIncorporatedEntityDetailsConnector = app.injector.instanceOf[ValidateIncorporatedEntityDetailsConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "validateIncorporatedEntityDetails" should {
    "return DetailsMatched" in {
      stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> true))

      val result = await(validateIncorporatedEntityDetailsConnector.validateIncorporatedEntityDetails(testCompanyNumber, testCtutr))

      result mustBe DetailsMatched
    }
    "return DetailsNotFound" in {
      stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(BAD_REQUEST, body = Json.obj(
        "code" -> "NOT_FOUND",
        "reason" -> "The back end has indicated that CT UTR cannot be returned"
      ))

      val result = await(validateIncorporatedEntityDetailsConnector.validateIncorporatedEntityDetails(testCompanyNumber, testCtutr))

      result mustBe DetailsNotFound
    }
    "return DetailsMismatch" in {
      stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> false))

      val result = await(validateIncorporatedEntityDetailsConnector.validateIncorporatedEntityDetails(testCompanyNumber, testCtutr))

      result mustBe DetailsMismatch
    }
  }

}
