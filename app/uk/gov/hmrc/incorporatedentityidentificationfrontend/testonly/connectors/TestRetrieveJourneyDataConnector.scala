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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.connectors

import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestRetrieveJourneyDataConnector @Inject()(httpClient: HttpClient,
                                                 appConfig: AppConfig
                                                )(implicit ec: ExecutionContext) {

  def retrieveJourneyData(journeyId: String)(implicit hc: HeaderCarrier): Future[String] =
    httpClient
      .GET[HttpResponse](appConfig.selfBaseUrl + routes.JourneyController.retrieveJourneyData(journeyId).url)
      .map(_.body)

}


