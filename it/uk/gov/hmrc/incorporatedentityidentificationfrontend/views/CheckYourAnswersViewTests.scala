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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CheckYourAnswers => messages}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper._

import scala.collection.JavaConverters._
import scala.concurrent.Future


trait CheckYourAnswersViewTests {
  this: ComponentSpecHelper =>

  def testCheckYourAnswersView(journeyId: String)
                              (result: => WSResponse,
                               companyNumberStub: => StubMapping,
                               ctutrStub: => StubMapping,
                               chrnStub: => StubMapping,
                               authStub: => StubMapping,
                               insertJourneyConfig: => Future[WriteResult],
                               auditStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      auditStub
      companyNumberStub
      Some(ctutrStub)
      chrnStub
      Jsoup.parse(result.body)
    }

    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have a sign out link that redirects to correct page" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      if (doc.getServiceName.text.equals("Entity Validation Service")){
        doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
      } else {
        doc.title mustBe s"${messages.title} - $testCallingServiceName - GOV.UK"
      }

    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a company number row" in {
        val companyNumberRow = summaryListRows.head

        companyNumberRow.getSummaryListQuestion mustBe messages.companyNumber
        companyNumberRow.getSummaryListAnswer mustBe testCompanyNumber
        companyNumberRow.getSummaryListChangeLink mustBe routes.CaptureCompanyNumberController.show(journeyId).url
        companyNumberRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.companyNumber}"
      }

      "have a ctutr row" in {
        val ctutrRow = summaryListRows.last

        ctutrRow.getSummaryListQuestion mustBe messages.ctutr
        ctutrRow.getSummaryListAnswer mustBe testCtutr
        ctutrRow.getSummaryListChangeLink mustBe routes.CaptureCtutrController.show(journeyId).url
        ctutrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.ctutr}"
      }

      "have a continue and confirm button" in {
        doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
      }

      "have a back link" in {
        val backLinks: Elements = doc.getBackLinks

        backLinks.size mustBe 1

        backLinks.first.text mustBe Base.back
      }

      "have a link to the service's accessibility statement" in {
        val footerLinks: Elements = doc.getFooterLinks

        footerLinks.size() must be >= 2

        footerLinks.eq(1).attr("href") mustBe testAccessibilityUrl
      }
    }

  }

  def testCheckYourAnswersNoCtutrView(journeyId: String)
                                     (result: => WSResponse,
                                      companyNumberStub: => StubMapping,
                                      authStub: => StubMapping,
                                      insertJourneyConfig: => Future[WriteResult],
                                      chrnStub: => StubMapping,
                                      auditStub: => StubMapping,
                                      retrieveCtutrStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      auditStub
      retrieveCtutrStub
      companyNumberStub
      chrnStub
      Jsoup.parse(result.body)
    }

    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have a sign out link that redirects to correct page" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      if (doc.getServiceName.text.equals("Entity Validation Service")){
        doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
      } else {
        doc.title mustBe s"${messages.title} - $testCallingServiceName - GOV.UK"
      }

    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a company number row" in {
        val companyNumberRow = summaryListRows.head

        companyNumberRow.getSummaryListQuestion mustBe messages.companyNumber
        companyNumberRow.getSummaryListAnswer mustBe testCompanyNumber
        companyNumberRow.getSummaryListChangeLink mustBe routes.CaptureCompanyNumberController.show(journeyId).url
        companyNumberRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.companyNumber}"
      }

      "have a ctutr row" in {
        val ctutrRow = summaryListRows.last

        ctutrRow.getSummaryListQuestion mustBe messages.ctutr
        ctutrRow.getSummaryListAnswer mustBe messages.noCtutr
        ctutrRow.getSummaryListChangeLink mustBe routes.CaptureCtutrController.show(journeyId).url
        ctutrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.ctutr}"
      }

      "have a continue and confirm button" in {
        doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
      }

      "have a back link" in {
        val backLinks: Elements = doc.getBackLinks

        backLinks.size mustBe 1

        backLinks.first.text mustBe Base.back
      }
    }

  }

  def testCheckYourAnswersCIOView(journeyId: String)
                                 (result: => WSResponse,
                                  companyNumberStub: => StubMapping,
                                  authStub: => StubMapping,
                                  insertJourneyConfig: => Future[WriteResult],
                                  auditStub: => StubMapping,
                                  retrieveChrnStub: => StubMapping,
                                  retrieveCtUtrStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      auditStub
      retrieveChrnStub
      companyNumberStub
      retrieveCtUtrStub
      Jsoup.parse(result.body)
    }

    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have a sign out link that redirects to correct page" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      if (doc.getServiceName.text.equals("Entity Validation Service")){
        doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
      } else {
        doc.title mustBe s"${messages.title} - $testCallingServiceName - GOV.UK"
      }

    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a company number row" in {
        val companyNumberRow = summaryListRows.head

        companyNumberRow.getSummaryListQuestion mustBe messages.companyNumber
        companyNumberRow.getSummaryListAnswer mustBe testCompanyNumber
        companyNumberRow.getSummaryListChangeLink mustBe routes.CaptureCompanyNumberController.show(journeyId).url
        companyNumberRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.companyNumber}"
      }

      "have a CHRN row" in {
        val chrnRow = summaryListRows.last

        chrnRow.getSummaryListQuestion mustBe messages.chrn
        chrnRow.getSummaryListAnswer mustBe testCHRN
        chrnRow.getSummaryListChangeLink mustBe routes.CaptureCHRNController.show(journeyId).url
        chrnRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.chrn}"
      }

      "have a continue and confirm button" in {
        doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
      }

      "have a back link" in {
        val backLinks: Elements = doc.getBackLinks

        backLinks.size mustBe 1

        backLinks.first.text mustBe Base.back
      }
    }

  }

  def testCheckYourAnswersOnlyCRNCIOView(journeyId: String)
                                 (result: => WSResponse,
                                  companyNumberStub: => StubMapping,
                                  authStub: => StubMapping,
                                  insertJourneyConfig: => Future[WriteResult],
                                  auditStub: => StubMapping,
                                  retrieveChrnStub: => StubMapping,
                                  retrieveCtutrStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      auditStub
      retrieveChrnStub
      companyNumberStub
      retrieveCtutrStub
      Jsoup.parse(result.body)
    }

    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have a sign out link that redirects to correct page" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      if (doc.getServiceName.text.equals("Entity Validation Service")){
        doc.title mustBe s"${messages.title} - $testDefaultServiceName - GOV.UK"
      } else {
        doc.title mustBe s"${messages.title} - $testCallingServiceName - GOV.UK"
      }

    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().asScala.toList

      "have 2 rows" in {
        summaryListRows.size mustBe 2
      }

      "have a company number row" in {
        val companyNumberRow = summaryListRows.head

        companyNumberRow.getSummaryListQuestion mustBe messages.companyNumber
        companyNumberRow.getSummaryListAnswer mustBe testCompanyNumber
        companyNumberRow.getSummaryListChangeLink mustBe routes.CaptureCompanyNumberController.show(journeyId).url
        companyNumberRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.companyNumber}"
      }

      "have a CHRN row" in {
        val chrnRow = summaryListRows.last

        chrnRow.getSummaryListQuestion mustBe messages.chrn
        chrnRow.getSummaryListAnswer mustBe messages.noChrn
        chrnRow.getSummaryListChangeLink mustBe routes.CaptureCHRNController.show(journeyId).url
        chrnRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.chrn}"
      }

      "have a continue and confirm button" in {
        doc.getSubmitButton.first.text mustBe Base.confirmAndContinue
      }

      "have a back link" in {
        val backLinks: Elements = doc.getBackLinks

        backLinks.size mustBe 1

        backLinks.first.text mustBe Base.back
      }
    }

  }

  def testServiceName(serviceName: String,
                      result: => WSResponse,
                      authStub: => StubMapping,
                      insertJourneyConfig: => Future[WriteResult]): Unit = {

    lazy val doc: Document = {
      await(insertJourneyConfig)
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the service name" in {
      doc.getServiceName.text mustBe serviceName
    }

  }

}
