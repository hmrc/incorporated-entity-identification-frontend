package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

import java.time.LocalDate
import java.util.UUID

import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BvPass



object TestConstants {

  val testCompanyNumber = "12345678"
  val companyNameKey = "companyName"
  val companyNumberKey = "companyNumber"
  val testCompanyName = "Test Company Ltd"
  val testCtutr = "1234567890"
  val testJourneyId = "TestJourneyId"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testDateOfIncorporation = LocalDate.now().toString
  val testCredentialId = UUID.randomUUID().toString
  val GGProviderId = UUID.randomUUID().toString
  val testGroupId = UUID.randomUUID().toString
  val testInternalId = UUID.randomUUID().toString
  val testDefaultServiceName = "Entity Validation Service"
  val testCallingServiceName = "Test Service"
  val testContinueUrl = "/test"
  val testPassStatus = BvPass
}
