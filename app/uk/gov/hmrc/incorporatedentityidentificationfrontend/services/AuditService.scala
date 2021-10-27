/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{CharitableIncorporatedOrganisation, LimitedCompany}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationFail, RegistrationNotCalled}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector,
                             journeyService: JourneyService,
                             appConfig: AppConfig,
                             storageService: StorageService) {

  def auditJourney(journeyId: String, authInternalId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = for {
    journeyConfig <- journeyService.getJourneyConfig(journeyId, authInternalId)
    companyNumber <- storageService.retrieveCompanyNumber(journeyId)
    optCtutr <- storageService.retrieveCtutr(journeyId)
    optIdentifiersMatch <- storageService.retrieveIdentifiersMatch(journeyId)
    optBusinessVerificationStatus <- storageService.retrieveBusinessVerificationStatus(journeyId)
    optRegistrationStatus <- storageService.retrieveRegistrationStatus(journeyId)
  } yield {
    val ctutrBlock =
      optCtutr match {
        case Some(ctutr) => Json.obj("CTUTR" -> ctutr)
        case _ => Json.obj()
      }
    val businessVerificationStatusBlock =
      optBusinessVerificationStatus match {
        case Some(bvStatus) => Json.obj("VerificationStatus" -> bvStatus)
        case _ => Json.obj("VerificationStatus" -> BusinessVerificationFail)
      }
    val registrationStatusBlock =
      optRegistrationStatus match {
        case Some(regStatus) => Json.obj("RegisterApiStatus" -> regStatus)
        case _ => Json.obj("RegisterApiStatus" -> RegistrationNotCalled)
      }
    journeyConfig.businessEntity match {
      case LimitedCompany =>
        val auditJson = Json.obj(
          "callingService" -> JsString(journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)),
          "businessType" -> "UK Company",
          "companyNumber" -> companyNumber,
          "isMatch" -> optIdentifiersMatch
        ) ++ ctutrBlock ++ businessVerificationStatusBlock ++ registrationStatusBlock

        auditConnector.sendExplicitAudit(
          auditType = "IncorporatedEntityRegistration",
          detail = auditJson)
      case CharitableIncorporatedOrganisation =>
        val auditJson = Json.obj(
          "callingService" -> JsString(journeyConfig.pageConfig.optServiceName.getOrElse(appConfig.defaultServiceName)),
          "businessType" -> "CIO",
          "companyNumber" -> companyNumber,
          "identifiersMatch" -> optIdentifiersMatch,
        ) ++ businessVerificationStatusBlock ++ registrationStatusBlock

        auditConnector.sendExplicitAudit(
          auditType = "CIOEntityRegistration",
          detail = auditJson)
      case _ =>
    }
  }

}
