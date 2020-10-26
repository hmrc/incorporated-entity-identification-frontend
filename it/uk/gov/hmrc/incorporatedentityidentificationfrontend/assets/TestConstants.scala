
package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

import java.time.LocalDate
import java.util.UUID

import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{Registered, RegistrationFailed, RegistrationStatus}


object TestConstants {

  val testCompanyNumber: String = "12345678"
  val companyNameKey: String = "companyName"
  val companyNumberKey: String = "companyNumber"
  val testCompanyName: String = "Test Company Ltd"
  val testCtutr: String = "1234567890"
  val testJourneyId: String = "TestJourneyId"
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
}
