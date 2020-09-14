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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.IncorporatedEntityIdentificationRetrievalHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, IncorporatedEntityInformation}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityIdentificationRetrievalConnector @Inject()(http: HttpClient,
                                                                   appConfig: AppConfig
                                                                  )(implicit ec: ExecutionContext) {

  def retrieveCompanyProfile(journeyId: String)(implicit hc: HeaderCarrier): Future[CompanyProfile] = {

    val dataKey = "company-profile"

    http.GET[JsValue](appConfig.backendStorageUrl(journeyId, optDataKey = Some(dataKey))).map {
      _.as[CompanyProfile](CompanyProfile.backendReads)
    }

  }

  def retrieveIncorporatedEntityInformation(journeyId: String)(implicit hc: HeaderCarrier): Future[IncorporatedEntityInformation] =
    http.GET[JsValue](appConfig.backendStorageUrl(journeyId, optDataKey = None)).map {
      _.as[IncorporatedEntityInformation]
    }

}
