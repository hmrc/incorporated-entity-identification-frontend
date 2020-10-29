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

import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat}

case class JourneyConfig(continueUrl: String, pageConfig: PageConfig)

object JourneyConfig {
  private val continueUrlKey = "continueUrl"
  private val optServiceNameKey = "optServiceName"
  private val deskProServiceIdKey = "deskProServiceId"

  implicit val format: OFormat[JourneyConfig] = new OFormat[JourneyConfig] {
    override def reads(json: JsValue): JsResult[JourneyConfig] = for {
      continueUrl <- (json \ continueUrlKey).validate[String]
      optServiceName <- (json \ optServiceNameKey).validateOpt[String]
      deskProServiceId <- (json \ deskProServiceIdKey).validate[String]
    } yield JourneyConfig(continueUrl, PageConfig(optServiceName, deskProServiceId))

    override def writes(journeyConfig: JourneyConfig): JsObject = Json.obj(
      continueUrlKey -> journeyConfig.continueUrl,
      optServiceNameKey -> journeyConfig.pageConfig.optServiceName,
      deskProServiceIdKey -> journeyConfig.pageConfig.deskProServiceId
    )
  }
}
