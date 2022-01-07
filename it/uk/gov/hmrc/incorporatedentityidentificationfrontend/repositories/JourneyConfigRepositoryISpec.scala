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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.repositories

import org.scalatest.concurrent.{AbstractPatienceConfiguration, Eventually}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyConfigRepositoryISpec extends ComponentSpecHelper with AbstractPatienceConfiguration with Eventually {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
    .configure("mongodb.timeToLiveSeconds" -> "10")
    .build

  val repo: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repo.drop)
  }

  "documents" should {
    "successfully insert a new document" in {
      await(repo.insertJourneyConfig(testJourneyId, testInternalId, testLimitedCompanyJourneyConfig))
      await(repo.count) mustBe 1
    }

    "successfully insert journeyConfig" in {
      await(repo.insertJourneyConfig(testJourneyId, testInternalId, testLimitedCompanyJourneyConfig))
      await(repo.findJourneyConfig(testJourneyId, testInternalId)) must contain(testLimitedCompanyJourneyConfig)
    }

    "successfully delete all documents" in {
      await(repo.insertJourneyConfig(testJourneyId, testInternalId, testLimitedCompanyJourneyConfig))
      await(repo.drop)
      await(repo.count) mustBe 0
    }

    "successfully delete one document" in {
      await(repo.insertJourneyConfig(testJourneyId, testInternalId, testLimitedCompanyJourneyConfig))
      await(repo.insertJourneyConfig(testJourneyId + 1, testInternalId, testLimitedCompanyJourneyConfig))
      await(repo.remove("_id" -> (testJourneyId + 1), "authInternalId" -> testInternalId))
      await(repo.count) mustBe 1
    }

    "successfully find journey config" when {
      "regime key is present" in {
        await(repo.insertJourneyConfig(testJourneyId, testInternalId, testLimitedCompanyJourneyConfig))
        await(repo.findJourneyConfig(testJourneyId, testInternalId)) must contain(testLimitedCompanyJourneyConfig)
      }
      "regime key is missing" in {
        await(repo.insertJourneyConfig(testJourneyId, testInternalId, testLimitedCompanyJourneyConfig))
        await(repo.removeById("regime"))
        await(repo.findJourneyConfig(testJourneyId, testInternalId)) must contain(testLimitedCompanyJourneyConfig)
      }
    }
  }
}
