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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.stubs.controllers

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, InjectedController}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => appRoutes}

import scala.concurrent.Future

@Singleton
class BusinessVerificationStubController @Inject()(appConfig: AppConfig) extends InjectedController {

  private val origin = "vat"
  private val businessVerificationJourneyId = UUID.randomUUID.toString

  def createJourney(journeyId: String): Action[AnyContent] = Action.async {

    val continueUrl: String = appConfig.selfUrl + appRoutes.CaptureBusinessVerificationResultController.show(journeyId).url

    Future.successful {
      Ok(Json.obj(
        "redirectUri" -> s"/verification-questions-frontend/journey/$businessVerificationJourneyId?continueUrl=$continueUrl&origin=$origin"
      ))
    }
  }

}

