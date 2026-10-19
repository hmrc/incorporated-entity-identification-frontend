/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.utils

import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative, RedirectUrl}

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

@Singleton
class UrlHelper @Inject()(appConfig: AppConfig) {

  def areRelativeOrAcceptedUrls(urlToBeValidated: List[String]): Boolean = {
    val areValidUrls = urlToBeValidated.map(url => Try(RedirectUrl(url)) match {
      case Failure(_: IllegalArgumentException) =>
        false
      case Success(url) =>
        url.getEither(OnlyRelative | AbsoluteWithHostnameFromAllowlist(appConfig.allowedHosts)) match {
          case Right(_) => true
          case Left(_) => false
        }
      case _ => throw new IllegalArgumentException("Invalid URL")
    })
    !areValidUrls.contains(false)
  }

  def withUseServiceNavigation(url: String): String = {
    val (urlWithoutFragment, fragment) = url.span(_ != '#')
    val queryStartIndex = urlWithoutFragment.indexOf('?')

    val queryParameters =
      if (queryStartIndex < 0) Seq.empty
      else urlWithoutFragment.substring(queryStartIndex + 1).split('&').toSeq

    if (queryParameters.exists(_.takeWhile(_ != '=') == "useServiceNavigation")) {
      url
    } else {
      val separator =
        if (queryStartIndex < 0) "?"
        else if (urlWithoutFragment.endsWith("?") || urlWithoutFragment.endsWith("&")) ""
        else "&"

      s"$urlWithoutFragment${separator}useServiceNavigation$fragment"
    }
  }
}