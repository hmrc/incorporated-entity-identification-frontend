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
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus.businessVerificationNotEnoughInfoToChallengeKey
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WiremockHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CheckYourAnswersViewTests

class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with IncorporatedEntityIdentificationStub
  with BusinessVerificationStub
  with AuthStub {

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  "GET /check-your-answers-business" should {
    "return OK" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testLimitedCompanyJourneyConfig
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubAudit()
      stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
      stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

      lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe OK
      verifyAudit()
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val auditStub = stubAudit()

        lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(
          status = OK,
          body = Json.toJsObject(testCompanyProfile)
        )
        lazy val ctutrStub = stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersView(testJourneyId)(result, companyNumberStub, ctutrStub, authStub, insertConfig, auditStub)
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
              accessibilityUrl = testAccessibilityUrl),
            businessEntity = LimitedCompany,
            businessVerificationCheck = true,
            regime = testRegime
          ))
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val auditStub = stubAudit()
        lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(
          status = OK,
          body = Json.toJsObject(testCompanyProfile)
        )
        lazy val ctutrStub = stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersView(testJourneyId)(result, companyNumberStub, ctutrStub, authStub, insertConfig, auditStub)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }

      "the applicant does not have a CTUTR" should {
        "return OK" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND, body = "No data")
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))

          lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")
          result.status mustBe OK
          verifyAudit()
        }

        "return a view which" should {
          lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val auditStub = stubAudit()
          lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          lazy val retrieveCtutrStub = stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND, body = "No data")
          lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

          testCheckYourAnswersNoCtutrView(testJourneyId)(result, companyNumberStub, authStub, insertConfig, auditStub, retrieveCtutrStub)
        }
      }
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))
        stubAuthFailure()
        stubAudit()
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fcheck-your-answers-business&origin=incorporated-entity-identification-frontend")
        verifyAudit()
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
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        result.status mustBe NOT_FOUND
        verifyAudit()
      }

      "the auth internal ID does not match what is stored in the journey config database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId + "1",
          journeyConfig = testLimitedCompanyJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        result.status mustBe NOT_FOUND
        verifyAudit()
      }

      "neither the journey ID or auth internal ID are found in the journey config database" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId + "1",
          authInternalId = testInternalId + "1",
          journeyConfig = testLimitedCompanyJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        result.status mustBe NOT_FOUND
        verifyAudit()
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubAudit()

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        result.status mustBe INTERNAL_SERVER_ERROR
        verifyAudit()
      }
    }
  }

  "POST /check-your-answers-business" when {
    "the limited company details are successfully matched" should {
      "return a redirect to the Business Verification Result page" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testLimitedCompanyJourneyConfig
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> true))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(status = OK)
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(status = CREATED)

        lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.BusinessVerificationController.startBusinessVerificationJourney(testJourneyId).url)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        verifyAudit()
      }

      "return a redirect to Registration Controller" when {
        "the business verification check is disabled" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig.copy(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> true))
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(status = OK)

          lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.RegistrationController.register(testJourneyId).url)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyAudit()
        }
      }
    }

    "the company details do not match" should {
      "redirect to ctutr mismatch page" when {
        "the business verification check is enabled" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> false))
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationFailJson)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

          lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(errorRoutes.CtutrMismatchController.show(testJourneyId).url)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAuditDetail(testRegisterAuditEventJson(testCompanyNumber, isMatch = false, testCtutr, BusinessVerificationFail, "not called"))
        }
        "the business verification check is disabled" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig.copy(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> false))
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

          lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(errorRoutes.CtutrMismatchController.show(testJourneyId).url)

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
          verifyAuditDetail(testRegisterAuditEventJson(testCompanyNumber, isMatch = false, testCtutr, BusinessVerificationFail, "not called"))
        }
      }
    }

    "the ctutr details do not exist" should {
      "redirect to ctutr not found page" when { //TODO - handle this in the case of entities without corporation tax
        "the business verification check is enabled" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubValidateIncorporatedEntityDetails(
            testCompanyNumber,
            testCtutr
          )(
            status = BAD_REQUEST,
            body = Json.obj(
              "code" -> "NOT_FOUND",
              "reason" -> "The back end has indicated that CT UTR cannot be returned"
            )
          )
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(status = OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)

          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationJson(value = businessVerificationNotEnoughInfoToChallengeKey))
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

          lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(errorRoutes.CtutrNotFoundController.show(testJourneyId).url)
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyAudit()
        }
        "the business verification check is disabled" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testLimitedCompanyJourneyConfig.copy(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubValidateIncorporatedEntityDetails(
            testCompanyNumber,
            testCtutr
          )(
            status = BAD_REQUEST,
            body = Json.obj(
              "code" -> "NOT_FOUND",
              "reason" -> "The back end has indicated that CT UTR cannot be returned"
            )
          )
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(NOT_FOUND)
          stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
          stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

          lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(errorRoutes.CtutrNotFoundController.show(testJourneyId).url)
          verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
          verifyAudit()
        }
      }
    }

    "the Registered Society has provided ctutr and crn" should {
      "redirect to Business Verification" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testRegisteredSocietyJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()

        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> true))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(status = OK)
        stubCreateBusinessVerificationJourney(testCtutr, testJourneyId)(status = CREATED)

        lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.BusinessVerificationController.startBusinessVerificationJourney(testJourneyId).url)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        verifyAudit()
      }
    }
    "the Registered Society has provided only crn" should {
      "redirect to the journey redirect controller" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testRegisteredSocietyJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
        stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(status = OK)
        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)

        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationJson(value = businessVerificationNotEnoughInfoToChallengeKey))
        stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
        stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND, body = "No data")
        stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

        lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        verifyAudit()
      }

      "redirect to the journey redirect controller when businessVerificationCheck is disabled" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testRegisteredSocietyJourneyConfig.copy(businessVerificationCheck = false)
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationJson(value = businessVerificationNotEnoughInfoToChallengeKey))
        stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
        stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND, body = "No data")
        stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

        lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        verifyAudit()
      }

      "redirect to the Registration Controller" when {
        "the business verification check is disabled" in {
          await(journeyConfigRepository.insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            journeyConfig = testRegisteredSocietyJourneyConfig.copy(businessVerificationCheck = false)
          ))

          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubAudit()
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubValidateIncorporatedEntityDetails(testCompanyNumber, testCtutr)(OK, Json.obj("matched" -> true))
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(status = OK)

          lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.RegistrationController.register(testJourneyId).url)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
          verifyAudit()
        }
      }
    }

    "the Charitable Incorporated Organisation has provided only crn" should {
      "redirect to Business Verification" in {
        await(journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        ))

        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit()

        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCioProfile))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(status = OK)
        stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationNotEnoughInformationToCallBV)(status = OK)
        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(status = OK)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = testBusinessVerificationJson(value = businessVerificationNotEnoughInfoToChallengeKey))
        stubRetrieveIdentifiersMatch(testJourneyId)(status = OK, body = false)
        stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND, body = "No data")
        stubRetrieveRegistrationStatus(testJourneyId)(status = OK, body = testRegistrationNotCalledJson)

        lazy val result = post(s"$baseUrl/$testJourneyId/check-your-answers-business")()

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
        verifyStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)
        verifyAudit()
      }
      "return a view which" should {
        lazy val insertConfig = journeyConfigRepository.insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          journeyConfig = testCharitableIncorporatedOrganisationJourneyConfig
        )

        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val auditStub = stubAudit()
        lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
        lazy val retrieveCtutrStub = stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND, body = "No data")
        lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersNoCtutrCIOView(testJourneyId)(result, companyNumberStub, authStub, insertConfig, auditStub, retrieveCtutrStub)
      }
    }
  }
}
