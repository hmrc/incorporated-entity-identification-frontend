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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.models

import play.api.libs.json._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.IncorporatedEntityDetailsMatching.DetailsMatchedKey
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.StorageService.IdentifiersMatchKey


case class IncorporatedEntityInformation(companyProfile: CompanyProfile,
                                         optCtutr: Option[String],
                                         optChrn: Option[String],
                                         identifiersMatch: IncorporatedEntityDetailsMatching,
                                         businessVerification: Option[BusinessVerificationStatus],
                                         registration: RegistrationStatus
                                        )

object IncorporatedEntityInformation {

  val companyProfileKey = "companyProfile"
  val ctutrKey = "ctutr"
  val chrnKey = "chrn"
  val identifiersMatchKey = "identifiersMatch"
  val businessVerificationKey = "businessVerification"
  val verificationStatusKey = "verificationStatus"
  val registrationKey = "registration"
  val businessVerificationUnchallengedKey = "UNCHALLENGED"

  implicit val format: OFormat[IncorporatedEntityInformation] = new OFormat[IncorporatedEntityInformation] {
    override def reads(json: JsValue): JsResult[IncorporatedEntityInformation] =
      for {
        companyProfile <- (json \ companyProfileKey).validate[CompanyProfile]
        optCtutr <- (json \ ctutrKey).validateOpt[String]
        optChrn <- (json \ chrnKey).validateOpt[String]
        identifiersMatch <- (json \ identifiersMatchKey).validate[IncorporatedEntityDetailsMatching]
        businessVerification <- (json \ businessVerificationKey).validateOpt[BusinessVerificationStatus]
        registrationStatus <- (json \ registrationKey).validate[RegistrationStatus]
      } yield {
        IncorporatedEntityInformation(companyProfile, optCtutr, optChrn, identifiersMatch, businessVerification, registrationStatus)
      }

    override def writes(incorporatedEntityInformation: IncorporatedEntityInformation): JsObject =
      Json.obj(
        companyProfileKey -> incorporatedEntityInformation.companyProfile,
        identifiersMatchKey -> incorporatedEntityInformation.identifiersMatch.toString,
        registrationKey -> incorporatedEntityInformation.registration
      ) ++ {
        incorporatedEntityInformation.optCtutr match {
          case Some(ctutr) => Json.obj(ctutrKey -> ctutr)
          case None => Json.obj()
        }
      } ++ {
        incorporatedEntityInformation.optChrn match {
          case Some(chrn) => Json.obj(chrnKey -> chrn.toUpperCase)
          case None => Json.obj()
        }
      } ++ {
        incorporatedEntityInformation.businessVerification match {
          case Some(businessVerification) => Json.obj(businessVerificationKey -> businessVerification)
          case None => Json.obj()
        }
      }
  }

  val jsonWriterForCallingServices: Writes[IncorporatedEntityInformation] = (incorporatedEntityInformation: IncorporatedEntityInformation) =>
    format.writes(incorporatedEntityInformation) ++ {
      incorporatedEntityInformation.businessVerification
        .map(businessVerification => {
          val businessVerificationStatusForCallingServices: String = businessVerification match {
            case BusinessVerificationNotEnoughInformationToCallBV |
                 BusinessVerificationNotEnoughInformationToChallenge => businessVerificationUnchallengedKey
            case BusinessVerificationPass => businessVerificationPassKey
            case BusinessVerificationFail => businessVerificationFailKey
            case CtEnrolled => businessVerificationCtEnrolledKey
          }
          Json.obj(businessVerificationKey -> Json.obj(businessVerificationStatusKey -> businessVerificationStatusForCallingServices))
        })
        .getOrElse(Json.obj())
    } ++ {
      Json.obj(IdentifiersMatchKey -> incorporatedEntityInformation.identifiersMatch.toString.equals(DetailsMatchedKey))
    }

}
