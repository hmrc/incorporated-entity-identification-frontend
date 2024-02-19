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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logging
import uk.gov.hmrc.http._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.CompanyProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyProfileConnector @Inject()(http: HttpClient,
                                        appConfig: AppConfig
                                       )(implicit ec: ExecutionContext) {

  import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.CompanyProfileHttpParser._

  def getCompanyProfile(companyNumber: String)(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    http.GET[Option[CompanyProfile]](appConfig.getCompanyProfileUrl(companyNumber.toUpperCase))(
      CompanyProfileHttpReads,
      hc,
      ec
    )
}

object CompanyProfileHttpParser extends Logging {
  private val companyNameKey = "company_name"
  private val companyNumberKey = "company_number"
  private val dateOfIncorporationKey = "date_of_creation"
  private val registeredOfficeAddressKey = "registered_office_address"

  private val companiesHouseReads: Reads[CompanyProfile] = (
    (__ \ companyNameKey).read[String] and
      (__ \ companyNumberKey).read[String] and
      (__ \ dateOfIncorporationKey).readNullable[String] and
      (__ \ registeredOfficeAddressKey).read[JsObject]
    ) (CompanyProfile.apply _)

  implicit object CompanyProfileHttpReads extends HttpReads[Option[CompanyProfile]] {
    override def read(method: String, url: String, response: HttpResponse): Option[CompanyProfile] = {
      response.status match {
        case OK =>
          response.json.validate[CompanyProfile](companiesHouseReads) match {
            case JsSuccess(companyProfile, _) =>
              if (companyProfile.dateOfIncorporation.isEmpty)
                logger.warn(s"[GG-7101] Companies House returned a missing date_of_creation field, company number was ${companyProfile.companyNumber}, response payload was ${response.body}")
              Some(companyProfile)
            case JsError(errors) =>
              throw new InternalServerException(s"Companies House API returned malformed JSON with errors: $errors")
          }
        case NOT_FOUND =>
          logger.info(s"[GG-7101] Companies House returned a 404/NotFound, url was $url")
          None
        case status =>
          logger.info(s"[GG-7101] Companies House responded with a $status, returning 500/InternalServerError, url was $url")
          throw new InternalServerException(s"Companies House API failed with status: $status")
      }
    }
  }

}
