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

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.DetailsMatched
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub, RegisterStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAuditDetail}

class RegistrationControllerISpec extends ComponentSpecHelper with AuthStub with IncorporatedEntityIdentificationStub with RegisterStub {

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  "GET /:journeyId/register" when {
    "the business entity is Limited Company" should {
      "redirect to continueUrl" when {
        "registration is successful and registration status is successfully stored" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)(status = OK, body = testSuccessfulRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testSuccessfulRegistration))

          lazy val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)
          verifyAuditDetail(
            testRegisterAuditEventJson(testCompanyNumber, isMatch = "true", testCtutr, verificationStatus = "success", registrationStatus = "success")
          )
        }

        "registration failed and registration status is successfully stored" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)(status = OK, body = testFailedRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, testFailedRegistration)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testFailedRegistration))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyLimitedCompanyRegister(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testFailedRegistration)
          verifyAuditDetail(
            testRegisterAuditEventJson(testCompanyNumber, isMatch = "true", testCtutr, verificationStatus = "success", registrationStatus ="fail")
          )
        }
      }

      "redirect to SignInPage" when {
        "the user is unauthorised" in {
          stubAuthFailure()
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fregister&origin=incorporated-entity-identification-frontend")
        }
      }

      "throw an exception" when {
        "business verification is in an invalid state" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationFailJson)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "company profile is missing" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = NOT_FOUND)
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationFailJson)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "ctutr is missing" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationFailJson)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "business verification status is missing" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = NOT_FOUND)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
    "the business entity is Registered Society" should {
      "redirect to continueUrl" when {
        "registration is successful and registration status is successfully stored" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)(status = OK, body = testSuccessfulRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testSuccessfulRegistration))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)
        }

        "registration failed and registration status is successfully stored" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)(status = OK, body = testFailedRegistrationJson)
          stubStoreRegistrationStatus(testJourneyId, testFailedRegistration)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testFailedRegistration))


          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisteredSocietyRegister(testCompanyNumber, testCtutr, testRegime)
          verifyStoreRegistrationStatus(testJourneyId, testFailedRegistration)
        }
      }

      "redirect to SignInPage" when {
        "the user is unauthorised" in {
          stubAuthFailure()
          stubAudit()

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fregister&origin=incorporated-entity-identification-frontend")
        }
      }

      "throw an exception" when {
        "business verification is in an invalid state" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, testBusinessVerificationFailJson)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "company profile is missing" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = NOT_FOUND)
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationFailJson)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "ctutr is missing" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationFailJson)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "business verification status is missing" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = NOT_FOUND)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
