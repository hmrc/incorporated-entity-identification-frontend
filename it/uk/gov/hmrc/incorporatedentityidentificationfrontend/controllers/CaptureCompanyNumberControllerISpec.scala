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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{CompaniesHouseStub, FeatureSwitching}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, CompaniesHouseApiStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCompanyNumberTests


class CaptureCompanyNumberControllerISpec extends ComponentSpecHelper
  with CaptureCompanyNumberTests
  with CompaniesHouseApiStub
  with IncorporatedEntityIdentificationStub
  with FeatureSwitching
  with AuthStub {

  "GET /company-number" should {
    "return OK" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId =  testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testLimitedCompanyJourneyConfig
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/company-number")

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
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/company-number")

        testCaptureCompanyNumberView(result, authStub, insertConfig)
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
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/company-number")

        testCaptureCompanyNumberView(result, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/company-number")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fcompany-number&origin=incorporated-entity-identification-frontend")
      }
    }

    "return NOT_FOUND" when {
      "the journeyId does not match what is stored in the journey config database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig)
        )
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"$baseUrl/$testJourneyId/company-number")

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

        lazy val result = get(s"$baseUrl/$testJourneyId/company-number")

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

        lazy val result = get(s"$baseUrl/$testJourneyId/company-number")

        result.status mustBe NOT_FOUND
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"$baseUrl/$testJourneyId/company-number")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

  "POST /company-number" when {
    "the feature switch is enabled" should {
      "retrieve companies house profile from the stub" when {
        "the company number is correct" should {
          "store companies house profile and redirect to the Confirm Business Name page" in {
            enable(CompaniesHouseStub)
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubRetrieveCompanyProfileFromStub(testCompanyNumber)(
              status = OK,
              body = companyProfileJson(testCompanyNumber, testCompanyName, testDateOfIncorporation, testAddress)
            )
            stubStoreCompanyProfile(testJourneyId, testCompanyProfile)(status = OK)

            lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.ConfirmBusinessNameController.show(testJourneyId).url)
            )
          }
        }
      }
    }

    "the feature switch is disabled" should {
      "retrieve companies house profile from coho" when {
        "the company number is correct" should {
          "store companies house profile and redirect to the Confirm Business Name page" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            disable(CompaniesHouseStub)
            stubRetrieveCompanyProfileFromCoHo(testCompanyNumber)(
              status = OK,
              body = companyProfileJson(testCompanyNumber, testCompanyName, testDateOfIncorporation, testAddress)
            )
            stubStoreCompanyProfile(testJourneyId, testCompanyProfile)(status = OK)

            lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.ConfirmBusinessNameController.show(testJourneyId).url)
            )
          }
        }

        "the company number is missing" should {
          "return a bad request" in {
            await(journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testLimitedCompanyJourneyConfig)
            )
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> "")

            result.status mustBe BAD_REQUEST
          }

          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testLimitedCompanyJourneyConfig
            )

          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> "")

          testCaptureCompanyNumberEmpty(result, authStub, insertConfig)
        }

        "the company number is not found" should {
          "redirect to the Company Number not found error page" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubRetrieveCompanyProfileFromCoHo(testCompanyNumber)(status = NOT_FOUND)
            lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

            result.status mustBe SEE_OTHER
            redirectUri(errorRoutes.CompanyNumberNotFoundController.show(testJourneyId).url)
          }
        }

        "the company number has more than 8 characters" should {
          "return a bad request" in {
             await(journeyConfigRepository.insertJourneyConfig(
               journeyId = testJourneyId,
               authInternalId = testInternalId,
               journeyConfig = testLimitedCompanyJourneyConfig
               )
             )
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> "0123456789")

            result.status mustBe BAD_REQUEST
          }

          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testLimitedCompanyJourneyConfig
            )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> "0123456789")

          testCaptureCompanyNumberWrongLength(result, authStub, insertConfig)
        }

        "company number is not in the correct format" should {
          "return a bad request" in {
            await(journeyConfigRepository.insertJourneyConfig(
                journeyId = testJourneyId,
                authInternalId = testInternalId,
                journeyConfig = testLimitedCompanyJourneyConfig
              )
            )
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> "13E!!!%")

            result.status mustBe BAD_REQUEST
          }

          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
              journeyId = testJourneyId,
              authInternalId = testInternalId,
              journeyConfig = testLimitedCompanyJourneyConfig
            )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> "13E!!!%")

          testCaptureCompanyNumberWrongFormat(result, authStub, insertConfig)
        }
      }

      "throw an Internal Server Exception" when {
        "the user does not have an internal ID" in {
          stubAuth(OK, successfulAuthResponse(None))

          lazy val result = post(s"$baseUrl/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

}
