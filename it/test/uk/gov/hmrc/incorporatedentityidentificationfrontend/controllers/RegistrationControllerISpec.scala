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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{DetailsMatched, RegistrationNotCalled}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus.format
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub, RegisterStub}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAuditDetail}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers._

class RegistrationControllerISpec extends ComponentSpecHelper with AuthStub with IncorporatedEntityIdentificationStub with RegisterStub {

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  private val registrationFailure = Json.arr(Json.obj(
    "code" -> "PARTY_TYPE_MISMATCH",
    "reason" -> "The remote endpoint has indicated there is Party Type mismatch"
  ))

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build()

  "GET /:journeyId/register" when {
    "the business entity is Limited Company" should {
      "redirect to continueUrl" when {
        "registration is successful" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(status = OK, body = testSuccessfulRegistrationJson)

          // Stub requests for audit data
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveChrn(testJourneyId)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testSuccessfulRegistration))

          lazy val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)

          verifyLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)

          verifyAuditDetail(
            testRegisterAuditEventJson(testCompanyNumber, isMatch = "true", testCtutr, verificationStatus = "success", registrationStatus = "success")
          )
        }

        "registration failed" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)(status = OK, body = testFailedRegistrationJson(registrationFailure))

          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveChrn(testJourneyId)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testFailedRegistration))

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)

          verifyLimitedCompanyRegister(testJourneyId, testLimitedCompanyJourneyConfig)

          verifyAuditDetail(
            testRegisterAuditEventJson(testCompanyNumber, isMatch = "true", testCtutr, verificationStatus = "success", registrationStatus = "fail")
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

    }

    "the business entity is Registered Society" should {
      "redirect to continueUrl" when {
        "registration is successful" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(status = OK, body = testSuccessfulRegistrationJson)

          // Stub requests for audit data
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveChrn(testJourneyId)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testSuccessfulRegistration))

          val result = get(s"$baseUrl/$testJourneyId/register")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)

          verifyRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)

          verifyAuditDetail(
            testRegisterAuditEventJson(testCompanyNumber, isMatch = "true", testCtutr, verificationStatus = "success", registrationStatus = "success")
          )
        }

        "registration failed" in {

          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)(status = OK, body = testFailedRegistrationJson(registrationFailure))

          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveChrn(testJourneyId)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(testFailedRegistration))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)

          verifyRegisteredSocietyRegister(testJourneyId, testRegisteredSocietyJourneyConfig)

          verifyAuditDetail(
            testRegisterAuditEventJson(testCompanyNumber, isMatch = "true", testCtutr, verificationStatus = "success", registrationStatus = "fail")
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
    }

    "the business entity is charitable incorporated organisation" should {
      "redirect to continue url" in {

        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()

        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)

        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        stubRetrieveCtutr(testJourneyId)(status = OK)
        stubRetrieveChrn(testJourneyId)(status = OK, body = testCHRN)
        stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = DetailsMatched)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationPassJson)
        stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = Json.toJson(RegistrationNotCalled)(format.writes(_)))

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)

        verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)

        verifyAuditDetail(
          testRegisterCIOAuditEventJson(testCompanyNumber, "unmatchable", testCHRN, "Not Enough information to call BV", "not called" )
        )

      }
    }

    "the response from auth does not contain an internal identifier return an internal server error" in {
        stubAuth(OK, emptyAuthResponse())
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")

        result.status mustBe INTERNAL_SERVER_ERROR
    }

  }
}
