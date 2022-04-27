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

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, CtEnrolled, DetailsMatched, JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.ConfirmBusinessNameViewTests


class ConfirmBusinessNameControllerISpec extends ComponentSpecHelper
  with ConfirmBusinessNameViewTests
  with IncorporatedEntityIdentificationStub
  with AuthStub
  with FeatureSwitching {

  "GET /confirm-business-name" when {
    "the company exists in Companies House" should {
      "return OK" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        val jsonBody = Json.toJsObject(testCompanyProfile)
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = jsonBody)

        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/confirm-business-name")

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
          lazy val stub = stubRetrieveCompanyProfileFromBE(testJourneyId)(
            status = OK,
            body = Json.toJsObject(testCompanyProfile)
          )
          lazy val result = get(s"$baseUrl/$testJourneyId/confirm-business-name")

          testConfirmBusinessNameView(result, stub, authStub, insertConfig, testCompanyName)
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
          lazy val stub = stubRetrieveCompanyProfileFromBE(testJourneyId)(
            status = OK,
            body = Json.toJsObject(testCompanyProfile)
          )
          lazy val result = get(s"$baseUrl/$testJourneyId/confirm-business-name")

          testConfirmBusinessNameView(result, stub, authStub, insertConfig, testCompanyName)
          testServiceName(testCallingServiceName, result, authStub, insertConfig)
        }
      }
    }

    "the company doesn't exist in the backend database" should {
      "throw an Internal Server Exception" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = NOT_FOUND)

        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/confirm-business-name")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        stubAuthFailure()
        val jsonBody = Json.toJsObject(testCompanyProfile)
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = jsonBody)

        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/confirm-business-name")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fconfirm-business-name&origin=incorporated-entity-identification-frontend")
      }
    }

    "return NOT_FOUND" when {
      "the journeyId does not match what is stored in the journey config database" in {

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/confirm-business-name")

        result.status mustBe NOT_FOUND
      }

      "the auth internal ID does not match what is stored in the journey config database" in {

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId + "1",
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/confirm-business-name")

        result.status mustBe NOT_FOUND
      }

      "neither the journey ID or auth internal ID are found in the journey config database" in {

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId + "1",
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/confirm-business-name")

        result.status mustBe NOT_FOUND
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"$baseUrl/$testJourneyId/confirm-business-name")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

  "POST /confirm-business-name" should {
    "redirect to Capture CTUTR Page" when {
      "the user has no IR_CT Enrolment" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        lazy val result = post(s"$baseUrl/$testJourneyId/confirm-business-name")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCtutrController.show(testJourneyId).url)
        )
      }

      "the user has an IR_CT Enrolment but the ctutr does not match the ctutr on the enrolment" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId), irctEnrolment))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        val testMismatchCompanyNumber = "11111111"
        val jsonBody = Json.toJsObject(CompanyProfile(testCompanyName, testMismatchCompanyNumber, testDateOfIncorporation, testAddress))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = jsonBody)
        stubValidateIncorporatedEntityDetails(testMismatchCompanyNumber, Some(testCtutr))(OK, Json.obj("matched" -> false))

        lazy val result = post(s"$baseUrl/$testJourneyId/confirm-business-name")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCtutrController.show(testJourneyId).url)
        )
      }
    }

    "redirect to the Registration Controller" when {
      "the user has an IR-CT enrolment and the ctutr matches the ctutr on the enrolment" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId), irctEnrolment))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))

        val jsonBody = Json.toJsObject(testCompanyProfile)
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = jsonBody)
        stubValidateIncorporatedEntityDetails(testCompanyNumber, Some(testCtutr))(OK, Json.obj("matched" -> true))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = DetailsMatched)(status = OK)
        stubStoreCtutr(testJourneyId, testCtutr)(status = OK)
        stubStoreBusinessVerificationStatus(testJourneyId, CtEnrolled)(status = OK)

        lazy val result = post(s"$baseUrl/$testJourneyId/confirm-business-name")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.RegistrationController.register(testJourneyId).url)
        )
      }
      "the user has an IR-CT enrolment and the ctutr matches the ctutr on the enrolment but businessVerificationCheck is disabled" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId), irctEnrolment))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig.copy(businessVerificationCheck = false)
        ))

        val jsonBody = Json.toJsObject(testCompanyProfile)
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = jsonBody)
        stubValidateIncorporatedEntityDetails(testCompanyNumber, Some(testCtutr))(OK, Json.obj("matched" -> true))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = DetailsMatched)(status = OK)
        stubStoreCtutr(testJourneyId, testCtutr)(status = OK)

        lazy val result = post(s"$baseUrl/$testJourneyId/confirm-business-name")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.RegistrationController.register(testJourneyId).url)
        )
      }
    }
    "redirect to the CHRN page" when {
      "the user is identifying a CIO" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        ))

        lazy val result = post(s"$baseUrl/$testJourneyId/confirm-business-name")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureCHRNController.show(testJourneyId).url)
        )
      }
    }
  }

}

