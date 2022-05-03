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

sealed trait IncorporatedEntityDetailsMatching

case object DetailsNotFound extends IncorporatedEntityDetailsMatching

case object DetailsMatched extends IncorporatedEntityDetailsMatching

case object DetailsMismatch extends IncorporatedEntityDetailsMatching

case object DetailsNotProvided extends IncorporatedEntityDetailsMatching

object IncorporatedEntityDetailsMatching {

  val DetailsNotFoundKey: String = "DetailsNotFound"
  val DetailsMatchedKey: String = "DetailsMatched"
  val DetailsMismatchKey: String = "DetailsMismatch"
  val DetailsNotProvidedKey: String = "DetailsNotProvided"

  implicit val format: Format[IncorporatedEntityDetailsMatching] = new Format[IncorporatedEntityDetailsMatching] {

    override def reads(jsValue: JsValue): JsResult[IncorporatedEntityDetailsMatching] =
      jsValue.validate[String] match {
        case JsSuccess(identifiersMatchString, _) => identifiersMatchString match {
          case DetailsNotFoundKey => JsSuccess(DetailsNotFound)
          case DetailsMatchedKey => JsSuccess(DetailsMatched)
          case DetailsMismatchKey => JsSuccess(DetailsMismatch)
          case DetailsNotProvidedKey => JsSuccess(DetailsNotProvided)
          case _ => notSupportedJsError(jsValue)
        }
        case JsError(_) => notSupportedJsError(jsValue)
      }

    override def writes(validationResponse: IncorporatedEntityDetailsMatching): JsValue = {
      val validationResponseAsString: String = validationResponse match {
        case DetailsNotFound => DetailsNotFoundKey
        case DetailsMatched => DetailsMatchedKey
        case DetailsMismatch => DetailsMismatchKey
        case DetailsNotProvided => DetailsNotProvidedKey
      }

      JsString(validationResponseAsString)
    }

  }

  private def notSupportedJsError(jsValue: JsValue): JsError = JsError(s"$jsValue not supported as Incorporated Entity Details Matching result")

}
