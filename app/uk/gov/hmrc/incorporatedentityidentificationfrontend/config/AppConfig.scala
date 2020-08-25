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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  private lazy val contactBaseUrl: String = servicesConfig.baseUrl("contact-frontend")

  private lazy val assetsUrl: String = servicesConfig.getString("assets.url")
  private lazy val serviceIdentifier: String = "MyService"

  lazy val assetsPrefix: String = assetsUrl + servicesConfig.getString("assets.version")
  lazy val analyticsToken: String = servicesConfig.getString(s"google-analytics.token")
  lazy val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")

  lazy val reportAProblemPartialUrl: String = s"$contactBaseUrl/contact/problem_reports_ajax?service=$serviceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"$contactBaseUrl/contact/problem_reports_nonjs?service=$serviceIdentifier"

  lazy val cookies: String = servicesConfig.getString("urls.footer.cookies")
  lazy val privacy: String = servicesConfig.getString("urls.footer.privacy")
  lazy val termsConditions: String = servicesConfig.getString("urls.footer.termsConditions")
  lazy val govukHelp: String = servicesConfig.getString("urls.footer.govukHelp")
}
