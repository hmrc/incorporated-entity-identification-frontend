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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.ValidateIncorporatedEntityDetailsHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.IncorporatedEntityDetailsMatching

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateIncorporatedEntityDetailsConnector @Inject()(http: HttpClientV2,
                                                           appConfig: AppConfig
                                                          )(implicit ec: ExecutionContext) {

  def validateIncorporatedEntityDetails(companyNumber: String, ctutr: Option[String])(implicit hc: HeaderCarrier): Future[IncorporatedEntityDetailsMatching] = {
    val jsonBody: JsObject =
      Json.obj(
        "companyNumber" -> companyNumber,
        "ctutr" -> ctutr
      )
    http.post(url"${appConfig.validateIncorporatedEntityDetailsUrl}").withBody(jsonBody)
      .execute[IncorporatedEntityDetailsMatching](ValidateIncorporatedEntityDetailsHttpReads, ec)
  }
}

