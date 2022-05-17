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
    val title = "This is a new service – your feedback will help us to improve it."
  }

  object CaptureCompanyNumber {
    val title = "What is the company registration number?"
    val line_1 = "You can search Companies House for the company registration number (opens in a new tab)"
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

  object CaptureCHRN {
    val title = "What is the charity’s HMRC reference number?"
    val heading = "What is the charity’s HMRC reference number?"
    val insetText = "If the charity has registered for Gift Aid then their HMRC reference number will be the same as their Gift Aid number. This is not the same as the charity number available on the charity register."
    val hintText = "This could be up to 7 characters and must begin with either one or two letters at the beginning followed by 1-5 numbers. For example, A999 or AB99999."
    val labelText = "HMRC reference number"
    val noChrnLink = "The charity does not have a HMRC reference number"

    object Error {
      val noChrnEntered = "Enter the HMRC reference number"
      val invalidChrnEntered = "Enter the HMRC reference number in the correct format"
      val invalidLengthChrnEntered = "Enter a HMRC reference number that is 7 characters or less"
    }

  }

  object CaptureCtutr {
    val title = "What is the company’s Unique Taxpayer Reference?"
    val heading = "What is the company’s Unique Taxpayer Reference?"
    val line = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Corporation Tax. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val lostUtr = "Lost the company’s UTR number"
    val registered_society_title = "What is your registered society’s Unique Taxpayer Reference?"
    val registered_society_heading = "What is your registered society’s Unique Taxpayer Reference?"
    val noCtutr = "The business does not have a Unique Taxpayer Reference"
    val dropdown_line_1 = "The UTR helps us identify your business"
    val dropdown_link_1 = "I have lost the businesses UTR number"
    val dropdown_link_2 = "My business does not have a UTR"

    object Error {
      val noCtutrEntered = "Enter your company’s Unique Taxpayer Reference"
      val invalidCtutrEntered = "Unique Taxpayer Reference number must be 10 numbers"
    }

  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"
    val ctutr = "Unique Taxpayers Reference number"
    val noCtutr = "The business does not have a UTR"
    val chrn = "HMRC reference number"
    val noChrn = "The charity does not have a HMRC reference number"
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

  object CtutrNotFound {
    val title = "The details you entered did not match our records"
    val heading = "The details you entered did not match our records"
    val line1 = "We could not match the details you entered with records held by HMRC."
    val line2 = "If you used the correct details, you cannot continue to register using this online service."
    val line3a = "You need to"
    val line3Link = " contact the Corporation Tax team (opens in a new tab)"
    val line3b = " and tell them there is an issue with your Corporation Tax Unique Tax Reference."
    val line4 = "If you used the wrong details, you can"
    val line4Link = " try again using different details."
  }

}
