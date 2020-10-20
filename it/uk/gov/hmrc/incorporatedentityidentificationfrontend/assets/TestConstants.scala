package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

import java.time.LocalDate
import java.util.UUID

import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationStatus, BusinessVerificationFail, BusinessVerificationPass, Registered, RegistrationFailed, RegistrationStatus}


object TestConstants {

  val testCompanyNumber = "12345678"
  val companyNameKey = "companyName"
  val companyNumberKey = "companyNumber"
  val testCompanyName = "Test Company Ltd"
  val testCtutr = "1234567890"
  val testJourneyId = "TestJourneyId"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val GGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testDefaultServiceName = "Entity Validation Service"
  val testCallingServiceName = "Test Service"
  val testContinueUrl = "/test"
  val testSafeId: String = UUID.randomUUID().toString
  val testSuccessfulRegistration: Registered = Registered(testSafeId)
  val testFailedRegistration: RegistrationStatus = RegistrationFailed
}
