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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.frontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.frontend.models.FeatureSwitchProvider
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FeatureSwitchProviderConfig @Inject()(configuration: Configuration) {

  val servicesConfig = new ServicesConfig(configuration)

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")

  lazy val selfFeatureSwitchUrl = s"$selfBaseUrl/identify-your-incorporated-business/test-only/api/feature-switches"

  lazy val incorporatedEntityIdentificationFeatureSwitchUrl =
    s"${servicesConfig.baseUrl("incorporated-entity-identification")}/incorporated-entity-identification/test-only/api/feature-switches"

  lazy val selfFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "incorporated-entity-identification-frontend",
    appName = "Incorporated Entity Identification Frontend",
    url = selfFeatureSwitchUrl
  )

  lazy val incorporatedEntityIdentificationFeatureSwitchProvider: FeatureSwitchProvider = FeatureSwitchProvider(
    id = "incorporated-entity-identification",
    appName = "Incorporated Entity Identification",
    url = incorporatedEntityIdentificationFeatureSwitchUrl
  )

  lazy val featureSwitchProviders: Seq[FeatureSwitchProvider] =
    Seq(selfFeatureSwitchProvider, incorporatedEntityIdentificationFeatureSwitchProvider)

}
