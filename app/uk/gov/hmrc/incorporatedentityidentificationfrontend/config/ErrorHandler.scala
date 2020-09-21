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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.NotFound
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.{Configuration, Environment, Logging}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.templates.error_template
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(view: error_template,
                             val messagesApi: MessagesApi,
                             val config: Configuration,
                             val env: Environment
                            ) extends FrontendErrorHandler with AuthRedirects with Logging {

  override def standardErrorTemplate(pageTitle: String,
                                     heading: String,
                                     message: String
                                    )(implicit request: Request[_]): Html = view(pageTitle, heading, message)

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case _: AuthorisationException => Future.successful(resolveError(request, exception))
      case _ => super.onServerError(request, exception)
    }
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = {
    ex match {
      case _: AuthorisationException =>
        logger.debug("[AuthenticationPredicate][async] Unauthorised request. Redirect to Sign In.")
        toGGLogin(rh.path)
      case _: NotFoundException =>
        NotFound(notFoundTemplate(Request(rh, "")))
      case _ =>
        super.resolveError(rh, ex)
    }
  }
}
