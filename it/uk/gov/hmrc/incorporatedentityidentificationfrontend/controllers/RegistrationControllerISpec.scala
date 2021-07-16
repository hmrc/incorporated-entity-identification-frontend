
package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub, RegisterStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class RegistrationControllerISpec extends ComponentSpecHelper with AuthStub with IncorporatedEntityIdentificationStub with RegisterStub {

  "GET /:journeyId/register" when {
    "the business entity is Limited Company" should {
      "redirect to continueUrl" when {
        "registration is successful and registration status is successfully stored" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = LimitedCompany
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
          stubLimitedCompanyRegister(testCompanyNumber, testCtutr)(status = OK, body = testSuccessfulRegistration)
          stubStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)(status = OK)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyLimitedCompanyRegister(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)
        }

        "registration failed and registration status is successfully stored" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = LimitedCompany
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
          stubLimitedCompanyRegister(testCompanyNumber, testCtutr)(status = OK, body = testFailedRegistration)
          stubStoreRegistrationStatus(testJourneyId, testFailedRegistration)(status = OK)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyLimitedCompanyRegister(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, testFailedRegistration)
        }
      }

      "redirect to SignInPage" when {
        "the user is unauthorised" in {
          stubAuthFailure()

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fregister&origin=incorporated-entity-identification-frontend")
        }
      }

      "throw an exception" when {
        "business verification is in an invalid state" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = LimitedCompany
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "company profile is missing" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = LimitedCompany
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = NOT_FOUND)
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "ctutr is missing" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = LimitedCompany
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "business verification status is missing" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = LimitedCompany
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
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
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
          stubRegisteredSocietyRegister(testCompanyNumber, testCtutr)(status = OK, body = testSuccessfulRegistration)
          stubStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)(status = OK)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisteredSocietyRegister(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)
        }

        "registration failed and registration status is successfully stored" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
          stubRegisteredSocietyRegister(testCompanyNumber, testCtutr)(status = OK, body = testFailedRegistration)
          stubStoreRegistrationStatus(testJourneyId, testFailedRegistration)(status = OK)

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          verifyRegisteredSocietyRegister(testCompanyNumber, testCtutr)
          verifyStoreRegistrationStatus(testJourneyId, testFailedRegistration)
        }
      }

      "redirect to SignInPage" when {
        "the user is unauthorised" in {
          stubAuthFailure()

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fregister&origin=incorporated-entity-identification-frontend")
        }
      }

      "throw an exception" when {
        "business verification is in an invalid state" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "company profile is missing" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = NOT_FOUND)
          stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "ctutr is missing" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
          stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND)
          stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

          val result = get(s"$baseUrl/$testJourneyId/register")
          result.status mustBe INTERNAL_SERVER_ERROR
        }

        "business verification status is missing" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          ))
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
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
