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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.services

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsString
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors.IncorporatedEntityInformationConnector
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.IncorporatedEntityInformationService._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncorporatedEntityInformationService @Inject()(connector: IncorporatedEntityInformationConnector
                                                    )(implicit ec: ExecutionContext) {

  def storeCompanyProfile(journeyId: String,
                          companyProfile: CompanyProfile
                         )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[CompanyProfile](journeyId, companyProfileKey, companyProfile)

  def storeCtutr(journeyId: String,
                 ctutr: String
                )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[String](journeyId, ctutrKey, ctutr)

  def storeBusinessVerificationStatus(journeyId: String,
                                      businessVerification: BusinessVerificationStatus
                                     )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[BusinessVerificationStatus](journeyId, verificationStatusKey, businessVerification)


  def storeIdentifiersMatch(journeyId: String,
                            identifiersMatch: Boolean
                           )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[Boolean](journeyId, identifiersMatchKey, identifiersMatch)

  def storeRegistrationStatus(journeyId: String,
                              registrationStatus: RegistrationStatus
                             )(implicit hc: HeaderCarrier): Future[StorageResult] =
    connector.storeData[RegistrationStatus](journeyId, registrationKey, registrationStatus)

  def retrieveCompanyProfile(journeyId: String
                            )(implicit hc: HeaderCarrier): Future[Option[CompanyProfile]] =
    connector.retrieveIncorporatedEntityInformation[CompanyProfile](journeyId, companyProfileKey)

  def retrieveBusinessVerificationStatus(journeyId: String
                                        )(implicit hc: HeaderCarrier): Future[Option[BusinessVerificationStatus]] =
    connector.retrieveIncorporatedEntityInformation[BusinessVerificationStatus](journeyId, verificationStatusKey)

  def retrieveCtutr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveIncorporatedEntityInformation[JsString](journeyId, ctutrKey).map {
      case Some(jsString) => Some(jsString.value)
      case None => None
    }

  def retrieveIncorporatedEntityInformation(journeyId: String
                                           )(implicit hc: HeaderCarrier): Future[Option[IncorporatedEntityInformation]] =
    connector.retrieveIncorporatedEntityInformation(journeyId)
}

object IncorporatedEntityInformationService {
  val companyProfileKey: String = "companyProfile"
  val ctutrKey: String = "ctutr"
  val identifiersMatchKey: String = "identifiersMatch"
  val verificationStatusKey: String = "businessVerification"
  val registrationKey: String = "registration"
}
