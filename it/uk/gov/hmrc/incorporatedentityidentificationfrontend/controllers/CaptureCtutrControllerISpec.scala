/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, JourneyLabels, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCtutrViewTests


class CaptureCtutrControllerISpec extends ComponentSpecHelper
  with CaptureCtutrViewTests
  with IncorporatedEntityIdentificationStub
  with AuthStub {

  private val basGatewaySignInUrl =
    s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fct-utr&origin=incorporated-entity-identification-frontend"

  "GET /ct-utr" when {
    "the Business Entity is LimitedCompany" should {
      "return OK" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

        result.status mustBe OK
      }

      "return a view" when {
        "there is no serviceName passed in the journeyConfig" should {
          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          testCaptureCtutrView(result, authStub, insertConfig)
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
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          testCaptureCtutrView(result, authStub, insertConfig)
          testServiceName(testCallingServiceName, result, authStub, insertConfig)
        }

        "there is a serviceName passed in the journeyConfig labels object" should {
          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = JourneyConfig(
              continueUrl = testContinueUrl,
              pageConfig = PageConfig(
                optServiceName = Some(testCallingServiceName),
                deskProServiceId = testDeskProServiceId,
                signOutUrl = testSignOutUrl,
                accessibilityUrl = testAccessibilityUrl,
                optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
              ),
              businessEntity = LimitedCompany,
              businessVerificationCheck = true,
              regime = testRegime
            )
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          testCaptureCtutrView(result, authStub, insertConfig)
          testServiceName(testCallingServiceNameFromLabels, result, authStub, insertConfig)
        }
      }

      "redirect to sign in page" when {
        "the user is not logged in" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          )
          )
          stubAuthFailure()
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(basGatewaySignInUrl)
        }
      }

      "return NOT_FOUND" when {
        "the journeyId does not match what is stored in the journey config database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId + "1",
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe NOT_FOUND
        }

        "the auth internal ID does not match what is stored in the journey config database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId + "1",
            journeyConfig = testLimitedCompanyJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe NOT_FOUND
        }

        "neither the journey ID or auth internal ID are found in the journey config database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId + "1",
            authInternalId = testInternalId + "1",
            journeyConfig = testLimitedCompanyJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe NOT_FOUND
        }
      }

      "throw an Internal Server Exception" when {
        "the user does not have an internal ID" in {
          stubAuth(OK, successfulAuthResponse(None))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
    "the Business Entity is RegisteredSociety" should {
      "return OK" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testRegisteredSocietyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

        result.status mustBe OK
      }

      "return a view" when {
        "there is no serviceName passed in the journeyConfig" should {
          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          testCaptureOptionalCtutrView(result, authStub, insertConfig)
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
              businessEntity = RegisteredSociety,
              businessVerificationCheck = true,
              regime = testRegime
            )
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          testCaptureOptionalCtutrView(result, authStub, insertConfig)
          testServiceName(testCallingServiceName, result, authStub, insertConfig)
        }

        "there is a serviceName passed in the journeyConfig labels object" should {
          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = JourneyConfig(
              continueUrl = testContinueUrl,
              pageConfig = PageConfig(
                optServiceName = Some(testCallingServiceName),
                deskProServiceId = testDeskProServiceId,
                signOutUrl = testSignOutUrl,
                accessibilityUrl = testAccessibilityUrl,
                optLabels = Some(JourneyLabels(None, Some(testCallingServiceNameFromLabels)))
              ),
              businessEntity = RegisteredSociety,
              businessVerificationCheck = true,
              regime = testRegime
            )
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          testCaptureOptionalCtutrView(result, authStub, insertConfig)
          testServiceName(testCallingServiceNameFromLabels, result, authStub, insertConfig)
        }
      }

      "redirect to sign in page" when {
        "the user is not logged in" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          )
          )
          stubAuthFailure()
          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(basGatewaySignInUrl)
        }
      }

      "return NOT_FOUND" when {
        "the journeyId does not match what is stored in the journey config database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId + "1",
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe NOT_FOUND
        }

        "the auth internal ID does not match what is stored in the journey config database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId + "1",
            journeyConfig = testRegisteredSocietyJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe NOT_FOUND
        }

        "neither the journey ID or auth internal ID are found in the journey config database" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId + "1",
            authInternalId = testInternalId + "1",
            journeyConfig = testRegisteredSocietyJourneyConfig
          )
          )
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe NOT_FOUND
        }
      }

      "throw an Internal Server Exception" when {
        "the user does not have an internal ID" in {
          stubAuth(OK, successfulAuthResponse(None))

          lazy val result = get(s"$baseUrl/$testJourneyId/ct-utr")

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "POST /ct-utr" when {
    "a valid ctutr is submitted" should {
      "store ctutr and redirect to Check Your Answers page" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubStoreCtutr(testJourneyId, testCtutr)(status = OK)

        val result = post(s"$baseUrl/$testJourneyId/ct-utr")("ctutr" -> testCtutr)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )

      }
    }

    "no ctutr is submitted" should {
      "return a bad request" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"$baseUrl/$testJourneyId/ct-utr")("ctutr" -> "")

        result.status mustBe BAD_REQUEST
      }

      lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testLimitedCompanyJourneyConfig
      )
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result = post(s"$baseUrl/$testJourneyId/ct-utr")("ctutr" -> "")

      testCaptureCtutrErrorMessagesNoCtutr(result, authStub, insertConfig)
    }

    "an invalid ctutr is submitted" should {
      "return a bad request" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"$baseUrl/$testJourneyId/ct-utr")("ctutr" -> "123456789")

        result.status mustBe BAD_REQUEST
      }

      lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testLimitedCompanyJourneyConfig
      )
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result = post(s"$baseUrl/$testJourneyId/ct-utr")("ctutr" -> "123456789")

      testCaptureCtutrErrorMessagesInvalidCtutr(result, authStub, insertConfig)
    }
  }

  "GET /no-ct-utr" should {
    "redirect to CYA page" when {
      "the ctutr is successfully removed" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testRegisteredSocietyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRemoveCtutr(testJourneyId)(NO_CONTENT)

        val result = get(s"$baseUrl/$testJourneyId/no-ct-utr")

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
          journeyConfig = testRegisteredSocietyJourneyConfig
        )
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRemoveCtutr(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        val result = get(s"$baseUrl/$testJourneyId/no-ct-utr")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"$baseUrl/$testJourneyId/no-ct-utr")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
