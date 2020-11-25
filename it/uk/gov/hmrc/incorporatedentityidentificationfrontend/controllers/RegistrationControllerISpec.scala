
package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass, CompanyProfile}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub, RegisterStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class RegistrationControllerISpec extends ComponentSpecHelper with AuthStub with IncorporatedEntityIdentificationStub with RegisterStub {

  "GET /:journeyId/register" should {
    "redirect to continueUrl" when {
      "registration is successful and registration status is successfully stored" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation)))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
        stubRegister(testCompanyNumber, testCtutr)(status = OK, body = testSuccessfulRegistration)
        stubStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)(status = OK)

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyRegister(testCompanyNumber, testCtutr)
        verifyStoreRegistrationStatus(testJourneyId, testSuccessfulRegistration)
      }

      "registration failed and registration status is successfully stored" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation)))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
        stubRegister(testCompanyNumber, testCtutr)(status = OK, body = testFailedRegistration)
        stubStoreRegistrationStatus(testJourneyId, testFailedRegistration)(status = OK)

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyRegister(testCompanyNumber, testCtutr)
        verifyStoreRegistrationStatus(testJourneyId, testFailedRegistration)
      }
    }

    "redirect to SignInPage" when {
      "the user is unauthorised" in {
        stubAuthFailure()

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some("/gg/sign-in?continue=%2Fidentify-your-incorporated-business%2FTestJourneyId%2Fregister&origin=incorporated-entity-identification-frontend")

      }
    }

    "throw an exception" when {
      "business verification is in an invalid state" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation)))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "company profile is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = NOT_FOUND)
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "ctutr is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation)))
        stubRetrieveCtutr(testJourneyId)(status = NOT_FOUND)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationFail))

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "business verification status is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation)))
        stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = NOT_FOUND)

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
