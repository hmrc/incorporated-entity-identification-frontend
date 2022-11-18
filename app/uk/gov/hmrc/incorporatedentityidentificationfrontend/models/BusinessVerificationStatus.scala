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

sealed trait BusinessVerificationStatus

case object BusinessVerificationPass extends BusinessVerificationStatus

case object BusinessVerificationFail extends BusinessVerificationStatus

case object BusinessVerificationNotEnoughInformationToChallenge extends BusinessVerificationStatus

case object BusinessVerificationNotEnoughInformationToCallBV extends BusinessVerificationStatus

case object CtEnrolled extends BusinessVerificationStatus

object BusinessVerificationStatus {

  val businessVerificationPassKey = "PASS"
  val businessVerificationFailKey = "FAIL"
  val businessVerificationNotEnoughInfoToChallengeKey = "NOT_ENOUGH_INFORMATION_TO_CHALLENGE"
  val businessVerificationNotEnoughInfoToCallBVKey = "NOT_ENOUGH_INFORMATION_TO_CALL_BV"
  val businessVerificationCtEnrolledKey = "CT_ENROLLED"
  val businessVerificationStatusKey = "verificationStatus"

  implicit val format: Format[BusinessVerificationStatus] = new Format[BusinessVerificationStatus] {
    override def writes(businessVerificationStatus: BusinessVerificationStatus): JsObject = {
      val businessVerificationStatusString = businessVerificationStatus match {
        case BusinessVerificationPass => businessVerificationPassKey
        case BusinessVerificationFail => businessVerificationFailKey
        case BusinessVerificationNotEnoughInformationToChallenge => businessVerificationNotEnoughInfoToChallengeKey
        case BusinessVerificationNotEnoughInformationToCallBV => businessVerificationNotEnoughInfoToCallBVKey
        case CtEnrolled => businessVerificationCtEnrolledKey
      }

      Json.obj(businessVerificationStatusKey -> businessVerificationStatusString)
    }

    override def reads(json: JsValue): JsResult[BusinessVerificationStatus] =
      (json \ businessVerificationStatusKey).validate[String].collect(JsonValidationError("Invalid business validation state")) {
        case `businessVerificationPassKey` => BusinessVerificationPass
        case `businessVerificationFailKey` => BusinessVerificationFail
        case `businessVerificationNotEnoughInfoToChallengeKey` => BusinessVerificationNotEnoughInformationToChallenge
        case `businessVerificationNotEnoughInfoToCallBVKey` => BusinessVerificationNotEnoughInformationToCallBV
        case `businessVerificationCtEnrolledKey` => CtEnrolled
      }
  }

}

case class JourneyCreated(redirectUri: String)

sealed trait JourneyCreationFailure

case object NotEnoughEvidence extends JourneyCreationFailure

case object UserLockedOut extends JourneyCreationFailure