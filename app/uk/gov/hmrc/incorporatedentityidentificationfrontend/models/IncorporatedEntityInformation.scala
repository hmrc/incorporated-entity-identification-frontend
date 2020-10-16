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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.models

import play.api.libs.json._


case class IncorporatedEntityInformation(companyProfile: CompanyProfile,
                                         ctutr: String,
                                         identifiersMatch: Boolean,
                                         businessVerification: BusinessVerificationStatus,
                                         registration: RegistrationStatus
                                        )

object IncorporatedEntityInformation {

  val companyProfileKey = "companyProfile"
  val ctutrKey = "ctutr"
  val identifiersMatchKey = "identifiersMatch"
  val businessVerificationKey = "businessVerification"
  val verificationStatusKey = "verificationStatus"
  val registrationKey = "registration"

  implicit val format: OFormat[IncorporatedEntityInformation] = new OFormat[IncorporatedEntityInformation] {
    override def reads(json: JsValue): JsResult[IncorporatedEntityInformation] =
      for {
        companyProfile <- (json \ companyProfileKey).validate[CompanyProfile]
        ctutr <- (json \ ctutrKey).validate[String]
        identifiersMatch <- (json \ identifiersMatchKey).validate[Boolean]
        businessVerification <- (json \ businessVerificationKey \ verificationStatusKey).validate[BusinessVerificationStatus]
        registrationStatus <- (json \ registrationKey).validate[RegistrationStatus]
      } yield {
        IncorporatedEntityInformation(companyProfile, ctutr, identifiersMatch, businessVerification, registrationStatus)
      }

    override def writes(incorporatedEntityInformation: IncorporatedEntityInformation): JsObject =
      Json.obj(
        companyProfileKey -> incorporatedEntityInformation.companyProfile,
        ctutrKey -> incorporatedEntityInformation.ctutr,
        identifiersMatchKey -> incorporatedEntityInformation.identifiersMatch,
        businessVerificationKey -> Json.obj(
          verificationStatusKey -> incorporatedEntityInformation.businessVerification
        ),
        registrationKey -> incorporatedEntityInformation.registration
      )
  }

}
