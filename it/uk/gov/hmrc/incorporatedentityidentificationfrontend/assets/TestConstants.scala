
package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testCompanyNumber: String = "12345678"
  val companyNameKey: String = "companyName"
  val companyNumberKey: String = "companyNumber"
  val testCompanyName: String = "Test Company Ltd"
  val testCtutr: String = "1234567890"
  val testNoCtutr: String = "1234567890"
  val testJourneyId: String = UUID.randomUUID().toString
  val testSignOutUrl: String = "signOutUrl"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val GGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testDefaultServiceName: String = "Entity Validation Service"
  val testCallingServiceName: String = "Test Service"
  val testContinueUrl: String = "/test"
  val testSafeId: String = UUID.randomUUID().toString
  val testSuccessfulRegistration: Registered = Registered(testSafeId)
  val testFailedRegistration: RegistrationStatus = RegistrationFailed
  val testDeskProServiceId: String = "vrs"
  val IRCTEnrolmentKey = "IR-CT"
  val IRCTReferenceKey = "UTR"
  val testAddress: JsObject = Json.obj(
    "address_line_1" -> "testLine1",
    "address_line_2" -> "test town",
    "care_of" -> "test name",
    "country" -> "United Kingdom",
    "locality" -> "test city",
    "po_box" -> "123",
    "postal_code" -> "AA11AA",
    "premises" -> "1",
    "region" -> "test region"
  )
  val testCompanyProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation, testAddress)
  val testCioProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, "", Json.obj())
  val testLimitedCompanyJourneyConfig: JourneyConfig = createTestJourneyConfig(LimitedCompany)
  val testRegisteredSocietyJourneyConfig: JourneyConfig = createTestJourneyConfig(RegisteredSociety)
  val testCharitableIncorporatedOrganisationJourneyConfig: JourneyConfig = createTestJourneyConfig(CharitableIncorporatedOrganisation)

  private def createTestJourneyConfig(entityType: BusinessEntity) =
    JourneyConfig(testContinueUrl, PageConfig(None, testDeskProServiceId, testSignOutUrl), entityType)

  val testIncorporatedEntityFullJourneyDataJson: JsObject = {
    Json.obj(
      "companyProfile" -> testCompanyProfile,
      "ctutr" -> testCtutr,
      "identifiersMatch" -> true,
      "businessVerification" -> BusinessVerificationPass,
      "registration" -> testSuccessfulRegistration
    )
  }

  def testRegisterAuditEventJson(companyNumber: String,
                                 isMatch: Boolean,
                                 ctUtr: String,
                                 verificationStatus: BusinessVerificationStatus,
                                 registrationStatus: RegistrationStatus): JsObject = {
    Json.obj(
      "callingService" -> "Entity Validation Service",
      "businessType" -> "UK Company",
      "companyNumber" -> companyNumber,
      "isMatch" -> isMatch,
      "CTUTR" -> ctUtr,
      "VerificationStatus" -> verificationStatus,
      "RegisterApiStatus" -> registrationStatus
    )
  }

}
