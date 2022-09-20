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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors

import play.api.http.Status._
import play.api.libs.json.{Json, JsObject, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.JourneyController._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, JourneyLabels}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors.TestCreateJourneyConnector.journeyConfigWriter

import javax.inject.{Inject, Singleton}
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
      case response =>
        throw new InternalServerException(s"Invalid response from Limited Company: Status: ${response.status} Body: ${response.body}")
    }
  }

  def createRegisteredSocietyJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + routes.JourneyController.createRegisteredSocietyJourney().url

    httpClient.POST(url, journeyConfig).map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
      case response =>
        throw new InternalServerException(s"Invalid response from Registered Society: Status: ${response.status} Body: ${response.body}")
    }
  }

  def createCharitableIncorporatedOrganisationJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + routes.JourneyController.createCharitableIncorporatedOrganisationJourney().url

    httpClient.POST(url, journeyConfig).map {
      case response@HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
      case response =>
        throw new InternalServerException(s"Invalid response from Charitable Incorporated Organisation: Status: ${response.status} Body: ${response.body}")
    }
  }
}

object TestCreateJourneyConnector {
  implicit val journeyConfigWriter: Writes[JourneyConfig] = (journeyConfig: JourneyConfig) => Json.obj(
    continueUrlKey -> journeyConfig.continueUrl,
    deskProServiceIdKey -> journeyConfig.pageConfig.deskProServiceId,
    signOutUrlKey -> journeyConfig.pageConfig.signOutUrl,
    accessibilityUrlKey -> journeyConfig.pageConfig.accessibilityUrl,
    businessVerificationCheckKey -> journeyConfig.businessVerificationCheck,
    regimeKey -> journeyConfig.regime
  ) ++ labelsAsOptJsObject(journeyConfig.pageConfig.optLabels)

  private def labelsAsOptJsObject(optJourneyLabels: Option[JourneyLabels]): JsObject = {

    optJourneyLabels match {
      case Some(journeyLabels) => Json.obj(labelsKey -> Json.toJsObject(journeyLabels))
      case _ => Json.obj()
    }

  }
}
