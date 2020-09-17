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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers

import play.api.http.Status.CREATED
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{IncorporatedEntityInformation, JourneyConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{IncorporatedEntityIdentificationStub, JourneyStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes.CaptureCompanyNumberController
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyControllerISpec extends ComponentSpecHelper with JourneyStub with IncorporatedEntityIdentificationStub {
  "POST /api/journey" should {
    "return a created journey" in {
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      val testJourneyConfig = JourneyConfig(
        continueUrl = "/testContinueUrl"
      )

      lazy val result = post("/api/journey", Json.toJson(testJourneyConfig))

      (result.json \ "journeyStartUrl").as[String] must include(CaptureCompanyNumberController.show(testJourneyId).url)

      await(journeyConfigRepository.findById(testJourneyId)) mustBe Some(testJourneyConfig)
    }
  }
  "GET /api/journey/:journeyId" should {
    "return captured data" when {
      "the journeyId exists" in {
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(IncorporatedEntityInformation(companyNumber = testCompanyNumber, companyName = testCompanyName, ctutr = testCtutr))
        )

        lazy val result = get(s"/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.obj("ctutr" -> testCtutr,
          "companyNumber" -> testCompanyNumber,
          "companyName" -> testCompanyName)
      }
    }
    "return not found" when {
      "the journey Id does not exist" in {
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = NOT_FOUND
        )

        lazy val result = get(s"/api/journey/$testJourneyId")

        result.status mustBe NOT_FOUND
      }
    }
  }
}
