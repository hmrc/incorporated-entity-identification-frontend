/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import play.api.http.Status._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestCreateJourneyConnector @Inject()(httpClient: HttpClient,
                                          appConfig: AppConfig
                                          )(implicit ec: ExecutionContext) {
  def createJourney(journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier): Future[String] = {
    val url = appConfig.selfBaseUrl + routes.JourneyController.createJourney().url

    httpClient.POST(url, journeyConfig).map{
      case response @ HttpResponse(CREATED, _, _) =>
        (response.json \ "journeyStartUrl").as[String]
    }
  }


}
