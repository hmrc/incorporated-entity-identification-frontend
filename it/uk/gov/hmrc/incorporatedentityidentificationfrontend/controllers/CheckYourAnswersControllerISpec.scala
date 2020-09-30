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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.IncorporatedEntityInformation
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CheckYourAnswersViewTests
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}

import scala.concurrent.ExecutionContext.Implicits.global


class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with IncorporatedEntityIdentificationStub
  with AuthStub {

  override def afterEach(): Unit = {
    super.afterEach()
    journeyConfigRepository.drop
  }

  "GET /check-your-answers-business" should {
    "return OK" in {
      await(insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveIncorporatedEntityInformation(testJourneyId)(
        status = OK,
        body = Json.toJsObject(
          IncorporatedEntityInformation(
            companyNumber = testCompanyNumber,
            companyName = testCompanyName,
            ctutr = testCtutr,
            dateOfIncorporation = testDateOfIncorporation
          )
        )
      )
      lazy val result: WSResponse = get(s"/$testJourneyId/check-your-answers-business")

      result.status mustBe OK
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        await(insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None))
        stubAuthFailure()
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyNumber = testCompanyNumber,
              companyName = testCompanyName,
              ctutr = testCtutr,
              dateOfIncorporation = testDateOfIncorporation
            )
          )
        )
        lazy val result: WSResponse = get(s"/$testJourneyId/check-your-answers-business")

        result.status mustBe SEE_OTHER
      }
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None)
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val stub = stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyNumber = testCompanyNumber,
              companyName = testCompanyName,
              ctutr = testCtutr,
              dateOfIncorporation = testDateOfIncorporation
            )
          )
        )
        lazy val result = get(s"/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersView(testJourneyId)(result, stub, authStub, insertConfig)
        testServiceName(testDefaultServiceName, result, authStub, insertConfig)
      }

      "there is a serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = Some(testCallingServiceName))
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val stub = stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyNumber = testCompanyNumber,
              companyName = testCompanyName,
              ctutr = testCtutr,
              dateOfIncorporation = testDateOfIncorporation
            )
          )
        )
        lazy val result = get(s"/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersView(testJourneyId)(result, stub, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }
    }
  }

  "POST /check-your-answers-business" when {
    "the company details are successfully matched" should {
      "return a redirect to the stored continue URL from the client service" in {
        await(insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyNumber = testCompanyNumber,
              companyName = testCompanyName,
              ctutr = testCtutr,
              dateOfIncorporation = testDateOfIncorporation
            )
          )
        )
        stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> true))

        lazy val result = post(s"/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(testContinueUrl)
      }
    }

    "the company details do not match" should {
      "redirect to ctutr mismatch page" in {
        await(insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyNumber = testCompanyNumber,
              companyName = testCompanyName,
              ctutr = testCtutr,
              dateOfIncorporation = testDateOfIncorporation
            )
          )
        )
        stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> false))

        lazy val result = post(s"/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(errorRoutes.CtutrMismatchController.show(testJourneyId).url)

      }
    }

    "the company details do not exist" should {
      "throw an exception" in { //TODO - handle this in the case of entities without corporation tax
        await(insertJourneyConfig(journeyId = testJourneyId, continueUrl = testContinueUrl, optServiceName = None))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyNumber = testCompanyNumber,
              companyName = testCompanyName,
              ctutr = testCtutr,
              dateOfIncorporation = testDateOfIncorporation
            )
          )
        )
        stubValidateIncorporatedEntityDetails(
          testCompanyNumber,
          testCtutr
        )(
          status = NOT_FOUND,
          body = Json.obj(
            "code" -> "NOT_FOUND",
            "reason" -> "The back end has indicated that CT UTR cannot be returned"
          )
        )

        lazy val result = post(s"/$testJourneyId/check-your-answers-business")()

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

}
