/*
 * Copyright 2025 HM Revenue & Customs
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

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, NotFoundException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.JourneyConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories.JourneyConfigRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyService @Inject()(journeyConnector: JourneyConnector,
                               journeyConfigRepository: JourneyConfigRepository
                              )(implicit ec: ExecutionContext) {

  def createJourney(authInternalId: String, journeyConfig: JourneyConfig)(implicit headerCarrier: HeaderCarrier): Future[String] =
    for {
      journeyId <- journeyConnector.createJourney()
      insertJourneyConfigResult <- journeyConfigRepository.insertJourneyConfig(journeyId, authInternalId, journeyConfig)
    } yield if (insertJourneyConfigResult.wasAcknowledged())
      journeyId
    else
      throw new InternalServerException(s"Unable to create journey $journeyId")


  def getJourneyConfig(journeyId: String, authInternalId: String): Future[JourneyConfig] =
    journeyConfigRepository.findJourneyConfig(journeyId, authInternalId).map {
      case Some(journeyConfig) =>
        journeyConfig
      case _ =>
        throw new NotFoundException(s"Journey config was not found for journey ID $journeyId")
    }
}