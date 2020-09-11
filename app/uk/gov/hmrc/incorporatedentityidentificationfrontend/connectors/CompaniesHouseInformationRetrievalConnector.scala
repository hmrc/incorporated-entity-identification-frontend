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
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.GetCompaniesHouseProfileHttpParser._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompaniesHouseProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetCompaniesHouseProfileConnector @Inject()(http: HttpClient,
                                                  appConfig: AppConfig
                                                 )(implicit ec: ExecutionContext) {

  def getCompaniesHouseProfile(companyNumber: String)(implicit hc: HeaderCarrier): Future[CompaniesHouseProfile] =
    http.GET[CompaniesHouseProfile](appConfig.retrieveCompanyInformationUrl(companyNumber))

}

object GetCompaniesHouseProfileHttpParser {

  implicit object GetCompaniesHouseProfileHttpReads extends HttpReads[CompaniesHouseProfile] {
    override def read(method: String, url: String, response: HttpResponse): CompaniesHouseProfile = {
      response.status match {
        case OK =>
          response.json.validate[CompaniesHouseProfile] match {
            case JsSuccess(companiesHouseProfile, _) =>
              companiesHouseProfile
            case JsError(errors) =>
              throw new InternalServerException(s"Companies House API returned malformed JSON with errors: $errors")
          }
        case status =>
          throw new InternalServerException(s"Companies House API failed with status: $status")
      }
    }
  }

}
