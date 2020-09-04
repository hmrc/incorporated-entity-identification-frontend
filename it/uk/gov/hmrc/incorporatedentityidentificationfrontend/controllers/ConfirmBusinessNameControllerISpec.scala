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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.IncorporatedEntityIdentificationStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.ConfirmBusinessNameViewTests

class ConfirmBusinessNameControllerISpec extends ComponentSpecHelper with ConfirmBusinessNameViewTests with IncorporatedEntityIdentificationStub {

  "GET /confirm-business-name" when {
    "the company exists in Companies House" should {
      "return ok" in {
        stubRetrieveCompanyName(testJourneyId)(status = OK, body = Json.obj(companyNameKey -> testCompanyName))

        lazy val result: WSResponse = get("/confirm-business-name")

        result.status mustBe OK
      }

      "return a view which" should {
        lazy val stub = stubRetrieveCompanyName(testJourneyId)(status = OK, body = Json.obj(companyNameKey -> testCompanyName))
        lazy val result: WSResponse = get("/confirm-business-name")

        testConfirmBusinessNameView(result, stub, testCompanyName)
      }
    }

//    "the company doesn't exist in the backend database" should {
//      "show technical difficulties page" in {
//        stubRetrieveCompanyName(testJourneyId)(status = NOT_FOUND)
//
//        lazy val result: WSResponse = get("/confirm-business-name")
//
//        result.status mustBe INTERNAL_SERVER_ERROR
//      }
//    } TODO uncomment when backend storage API is built as currently it defaults to a success.
  }

  "POST /confirm-business-name" should {
    "should store company name and redirect to Capture CTUTR Page" in {
      lazy val result = post("/confirm-business-name")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureCtutrController.show().url)
      )
    }
  }

}

