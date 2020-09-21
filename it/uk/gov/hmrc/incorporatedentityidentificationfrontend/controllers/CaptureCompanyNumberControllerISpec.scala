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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{CompaniesHouseStub, FeatureSwitching}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompanyProfile
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, CompaniesHouseApiStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCompanyNumberTests
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}


class CaptureCompanyNumberControllerISpec extends ComponentSpecHelper
  with CaptureCompanyNumberTests with CompaniesHouseApiStub with IncorporatedEntityIdentificationStub with FeatureSwitching with AuthStub {

  "GET /company-number" should {
    "return OK" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result: WSResponse = get(s"/$testJourneyId/company-number")

      result.status mustBe OK
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/$testJourneyId/company-number")

        result.status mustBe SEE_OTHER
      }
    }

    "return a view which" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result: WSResponse = get(s"/$testJourneyId/company-number")

      testCaptureCompanyNumberView(result, authStub)
    }
  }

  "POST /company-number" when {
    "the feature switch is enabled" should {
      "retrieve companies house profile from the stub" when {
        "the company number is correct" should {
          "store companies house profile and redirect to the Confirm Business Name page" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            enable(CompaniesHouseStub)
            stubRetrieveCompanyProfileFromStub(testCompanyNumber)(status = OK, body = companyProfileJson(testCompanyNumber, testCompanyName))
            stubStoreCompanyProfile(testJourneyId, CompanyProfile(testCompanyName, testCompanyNumber))(status = OK)

            lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

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
            stubRetrieveCompanyProfileFromCoHo(testCompanyNumber)(status = OK, body = companyProfileJson(testCompanyNumber, testCompanyName))
            stubStoreCompanyProfile(testJourneyId, CompanyProfile(testCompanyName, testCompanyNumber))(status = OK)

            lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

            result must have(
              httpStatus(SEE_OTHER),
              redirectUri(routes.ConfirmBusinessNameController.show(testJourneyId).url)
            )
          }
        }

        "the company number is missing" should {
          "return a bad request" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> "")

            result.status mustBe BAD_REQUEST
          }

          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> "")

          testCaptureCompanyNumberEmpty(result, authStub)
        }

        "the company number is not found" should {
          "redirect to the Company Number not found error page" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            stubRetrieveCompanyProfileFromCoHo(testCompanyNumber)(status = NOT_FOUND)
            lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> testCompanyNumber)

            result.status mustBe SEE_OTHER
            redirectUri(errorRoutes.CompanyNumberNotFoundController.show(testJourneyId).url)
          }
        }

        "the company number has more than 8 characters" should {
          "return a bad request" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> "0123456789")

            result.status mustBe BAD_REQUEST
          }

          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> "0123456789")

          testCaptureCompanyNumberWrongLength(result, authStub)
        }

        "company number is not in the correct format" should {
          "return a bad request" in {
            stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
            lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> "13E!!!%")

            result.status mustBe BAD_REQUEST
          }

          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val result = post(s"/$testJourneyId/company-number")(companyNumberKey -> "13E!!!%")

          testCaptureCompanyNumberWrongFormat(result, authStub)
        }

      }
    }
  }

}
