/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(storageService: StorageService,
                                                 registrationConnector: RegistrationConnector
                                                )(implicit ec: ExecutionContext) {

  def register(journeyId: String, journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- journeyConfig.businessEntity match {
              case LimitedCompany => registrationConnector.registerLimitedCompany(journeyId, journeyConfig)
              case RegisteredSociety => registrationConnector.registerRegisteredSociety(journeyId, journeyConfig)
              case CharitableIncorporatedOrganisation => Future.successful(RegistrationNotCalled) //Not currently registered
            }
    _ <- storageService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield registrationStatus
}
