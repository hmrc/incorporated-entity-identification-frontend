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

import play.api.libs.json.{Format, JsResult, JsString, JsValue, Json, OFormat}

sealed trait BusinessVerificationState

case object BvPass extends BusinessVerificationState
case object BvFail extends BusinessVerificationState
case object BvUnchallenged extends BusinessVerificationState

case object BusinessVerificationState {
  val BvPassKey = "PASS"
  val BvFailKey = "FAIL"
  val BvUnchallengedKey = "UNCHALLENGED"


  implicit val businessVerificationStateFormat: Format[BusinessVerificationState] = new Format[BusinessVerificationState] {
    override def writes(bvState: BusinessVerificationState): JsValue = bvState match {
      case BvPass => JsString(BvPassKey)
      case BvFail => JsString(BvFailKey)
      case BvUnchallenged => JsString(BvUnchallengedKey)
    }

    override def reads(json: JsValue): JsResult[BusinessVerificationState] =
      json.validate[String] map {
        case BvPassKey => BvPass
        case BvFailKey => BvFail
        case BvUnchallengedKey => BvUnchallenged
      }
  }


}


case class BusinessVerification(verificationStatus: BusinessVerificationState)

object BusinessVerification {
  implicit val format: OFormat[BusinessVerification] = Json.format[BusinessVerification]
}




