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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors

import javax.inject.{Inject, Singleton}
import play.api.http.Status._
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors.TestCreateJourneyConnector.journeyConfigWriter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestCreateJourneyConnector @Inject()(httpClient: HttpClient,
                                           appConfig: AppConfig
                                          )(implicit ec: ExecutionContext) {

  def createLtdCompanyJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + routes.JourneyController.createLtdCompanyJourney().url

    httpClient.POST(url, journeyConfig).map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
    }
  }

  def createRegisteredSocietyJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + routes.JourneyController.createRegisteredSocietyJourney().url

    httpClient.POST(url, journeyConfig).map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
    }
  }
}

object TestCreateJourneyConnector {
  implicit val journeyConfigWriter: Writes[JourneyConfig] = (journeyConfig: JourneyConfig) => Json.obj(
    continueUrlKey -> journeyConfig.continueUrl,
    optServiceNameKey -> journeyConfig.pageConfig.optServiceName,
    deskProServiceIdKey -> journeyConfig.pageConfig.deskProServiceId,
    signOutUrlKey -> journeyConfig.pageConfig.signOutUrl
  )
}

