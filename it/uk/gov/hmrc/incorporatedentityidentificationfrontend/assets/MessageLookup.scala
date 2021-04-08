/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val change = "Change"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"
    val tryAgain = "Try Again"
    val back = "Back"


    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }
  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service - your feedback (opens in new tab) will help us to improve it."
  }

  object CaptureCompanyNumber {
    val title = "What is the company registration number?"
    val link = "search Companies House for the company registration number (opens in new tab)"
    val line_1 = s"You can $link."
    val hint = "For example, 01234567"


    object Error {
      val emptyCompanyNumber = "Enter a Company Registration number"
      val lengthCompanyNumber = "Enter a Company Registration number using 8 characters or fewer"
      val formatCompanyNumber = "Enter a Company Number in the correct format"
    }

  }

  object ConfirmBusinessName {
    val title = "Confirm the company name"
    val heading = "Confirm the company name"
    val change_company = "Change company"
  }

  object CaptureCtutr {
    val title = "What is the company’s Unique Taxpayer Reference?"
    val heading = "What is the company’s Unique Taxpayer Reference?"
    val line = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Corporation Tax. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val lostUtr = "Lost the company’s UTR number"

    object Error {
      val noCtutrEntered = "Enter your company’s Unique Taxpayer Reference"
      val invalidCtutrEntered = "Unique Taxpayer Reference number must be 10 numbers"
    }

  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"
    val ctutr = "Unique Taxpayers Reference number"
    val companyNumber = "Company number"
  }

  object CtutrMismatch {
    val title: String = "We could not confirm your business"
    val heading: String = title
    val paragraph: String = "The information you provided does not match the details we have about your business."
  }

  object CompanyNumberNotFound {
    val title = "We could not confirm your company"
    val heading = "We could not confirm your company"
    val line = "The company number you entered is not on our system."

  }

}
