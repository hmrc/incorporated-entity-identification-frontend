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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.JourneyService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes.CaptureCompanyNumberController
import scala.concurrent.ExecutionContext

class JourneyController @Inject()(controllerComponents: MessagesControllerComponents,
                                  journeyService: JourneyService)(implicit ec: ExecutionContext) extends FrontendController(controllerComponents) {
  def createJourney(): Action[JourneyConfig] = Action.async(parse.json[JourneyConfig]) {
    implicit req =>
      journeyService.createJourney(req.body).map(
        journeyId =>
          Created(Json.obj(
            "journeyStartUrl" -> CaptureCompanyNumberController.show(journeyId).absoluteURL()
          ))
      )
  }
}
