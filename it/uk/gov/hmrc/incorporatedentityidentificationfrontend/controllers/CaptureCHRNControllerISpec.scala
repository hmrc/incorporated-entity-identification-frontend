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

import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCHRNumberViewTests


class CaptureCHRNControllerISpec extends ComponentSpecHelper
  with CaptureCHRNumberViewTests
  with IncorporatedEntityIdentificationStub
  with AuthStub {

  "GET /chrn" should {
    "return OK when the user is authorised" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
      )
      )
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

      result.status mustBe OK
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        testCaptureCHRNView(result, authStub, insertConfig)
        testServiceName(testDefaultServiceName, result, authStub, insertConfig)
      }

      "there is a serviceName passed in the journeyConfig" should {
        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = JourneyConfig(
            continueUrl = testContinueUrl,
            pageConfig = PageConfig(
              optServiceName = Some(testCallingServiceName),
              deskProServiceId = testDeskProServiceId,
              signOutUrl = testSignOutUrl,
              accessibilityUrl = testAccessibilityUrl
            ),
            businessEntity = LimitedCompany,
            businessVerificationCheck = true,
            regime = testRegime
          )
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        testCaptureCHRNView(result, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }
    }

    "redirect to sign in page" when {
      "the user is not authorized" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        )
        stubAuthFailure()
        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fchrn&origin=incorporated-entity-identification-frontend")
      }
    }

    "return NOT_FOUND" when {
      "the journeyId does not match what is stored in the journey config database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        result.status mustBe NOT_FOUND
      }

      "the auth internal ID does not match what is stored in the journey config database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        result.status mustBe NOT_FOUND
      }

      "neither the journey ID or auth internal ID are found in the journey config database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        result.status mustBe NOT_FOUND
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"$baseUrl/$testJourneyId/chrn")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /chrn" when {
    "the user is authorized" when {
      "a valid chrn is submitted" should {
        "store chrn and redirect to Check Your Answers page" in {
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubStoreCHRN(testJourneyId, testCHRN)(status = OK)

          val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> testCHRN)

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
          )

        }
      }

      "no chrn is entered" should {
        "return a bad request" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> "")

          result.status mustBe BAD_REQUEST
        }

        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> "")

        testCaptureCHRNErrorMessagesNotEntered(result, authStub, insertConfig)
      }

      "a chrn of invalid length is submitted" should {
        "return a bad request" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> "AA1123456")

          result.status mustBe BAD_REQUEST
        }

        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> "AA123456")

        testCaptureCHRNErrorMessagesInvalidLength(result, authStub, insertConfig)
      }

      "a CHRN in an invalid format is submitted" should {
        "return a bad request" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> "123456")

          result.status mustBe BAD_REQUEST
        }

        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> "123456")

        testCaptureCHRNErrorMessagesInvalidFormat(result, authStub, insertConfig)
      }
    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> testCHRN)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fchrn&origin=incorporated-entity-identification-frontend")
        )
      }
    }
  }

  "GET /no-chrn" when {
    "the user is authorized" should {
      "redirect to CYA page" when {
        "the chrn is successfully removed" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(NO_CONTENT)

          val result = get(s"$baseUrl/$testJourneyId/no-chrn")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
          )
        }
      }

      "throw an exception" when {
        "the backend returns a failure" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRemoveCHRN(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

          val result = get(s"$baseUrl/$testJourneyId/no-chrn")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

      "throw an Internal Server Exception" when {
        "the user does not have an internal ID" in {
          stubAuth(OK, successfulAuthResponse(None))

          lazy val result = get(s"$baseUrl/$testJourneyId/no-chrn")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user is not authorized" should {

      "redirect the user to the login page" in {

        stubAuthFailure()

        val result = post(s"$baseUrl/$testJourneyId/chrn")("chrn" -> testCHRN)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fchrn&origin=incorporated-entity-identification-frontend")
        )
      }

    }
  }

}
