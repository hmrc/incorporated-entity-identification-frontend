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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.config

import play.api.Configuration
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, CompaniesHouseStub, FeatureSwitching}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.asScalaBufferConverter

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig, config: Configuration) extends FeatureSwitching {

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")
  lazy val selfUrl: String = servicesConfig.getString("microservice.services.self.url")

  lazy val contactHost: String = servicesConfig.getString("contact-frontend.host")

  lazy val grsDeskProServiceId: String = "grs"

  lazy val companiesHouse: String = servicesConfig.getString("companies-house.url")

  private lazy val backendUrl: String = servicesConfig.baseUrl("incorporated-entity-identification")

  private lazy val incorporationInformationUrl = servicesConfig.baseUrl("incorporation-information")

  private lazy val businessVerificationUrl = servicesConfig.getString("microservice.services.business-verification.url")

  def incorporatedEntityInformationUrl(journeyId: String): String = s"$backendUrl/incorporated-entity-identification/journey/$journeyId"

  def createJourneyUrl: String = s"$backendUrl/incorporated-entity-identification/journey"

  def registerLimitedCompanyUrl: String = s"$backendUrl/incorporated-entity-identification/register-limited-company"

  def registerRegisteredSocietyUrl: String = s"$backendUrl/incorporated-entity-identification/register-registered-society"

  def getCompanyProfileUrl(companyNumber: String): String = {
    if (isEnabled(CompaniesHouseStub))
      s"$selfBaseUrl/identify-your-incorporated-business/test-only/$companyNumber/incorporated-company-profile"
    else
      s"$incorporationInformationUrl/incorporation-information/$companyNumber/incorporated-company-profile"
  }

  def createBusinessVerificationJourneyUrl: String = {
    if (isEnabled(BusinessVerificationStub))
      s"$selfBaseUrl/identify-your-incorporated-business/test-only/business-verification/journey"
    else
      s"$businessVerificationUrl/journey"
  }

  def getBusinessVerificationResultUrl(journeyId: String): String = {
    if (isEnabled(BusinessVerificationStub))
      s"$selfBaseUrl/identify-your-incorporated-business/test-only/business-verification/journey/$journeyId/status"
    else
      s"$businessVerificationUrl/journey/$journeyId/status"
  }

  lazy val validateIncorporatedEntityDetailsUrl: String = s"$backendUrl/incorporated-entity-identification/validate-details"

  lazy val defaultServiceName: String = servicesConfig.getString("defaultServiceName")

  lazy val timeToLiveSeconds: Long = servicesConfig.getString("mongodb.timeToLiveSeconds").toLong

  lazy val allowedHosts: Set[String] = config.underlying.getStringList("microservice.hosts.allowList").asScala.toSet

  private lazy val feedbackUrl: String = servicesConfig.getString("feedback.host")

  lazy val vatRegExitSurveyOrigin = "vat-registration"

  lazy val vatRegFeedbackUrl = s"$feedbackUrl/feedback/$vatRegExitSurveyOrigin"

  def betaFeedbackUrl(serviceIdentifier: String): String =
    s"$contactHost/contact/beta-feedback?service=$serviceIdentifier"

  lazy val timeout: Int = servicesConfig.getInt("timeout.timeout")
  lazy val countdown: Int = servicesConfig.getInt("timeout.countdown")
}
