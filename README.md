
# Incorporated-Entity-Identification-Frontend
This is a Scala/Play frontend to allow Limited Companies to provide their information to HMRC.

### How to run the service
1. Make sure any dependent services are running using the following service-manager command
`sm --start INCORPORATED_ENTITY_IDENTIFICATION_ALL -r` 

2. Stop the frontend in service manager using
 `sm --stop INCORPORATED_ENTITY_IDENTIFICATION_FRONTEND`
 
3. Run the frontend locally using
`sbt 'run 9718 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

# End-Points
## POST /journey
### Deprecated - use POST /ltd-company/journey instead

---
Creates a new journey, storing the journeyConfig against the journeyId.
#### Request:
Request body must contain the continueUrl and deskProServiceId fields. If nothing is provided for the optional service name, ```Entity Validation Service``` will be used.

```
{
  "continueUrl" : "/testUrl",
  "optServiceName" : "Service Name",
  "deskProServiceId" : "DeskProServiceId",
}
```

#### Response:
Status: **Created(201)**

Example Response body: 

```
{“journeyStartUrl” : "/testUrl"}
```
## POST /ltd-company/journey

---
Creates a new journey for Ltd Company, storing the journeyConfig against the journeyId.
#### Request:
Request body must contain the continueUrl and deskProServiceId fields. If nothing is provided for the optional service name, ```Entity Validation Service``` will be used.

```
{
  "continueUrl" : "/testUrl",
  "optServiceName" : "Service Name",
  "deskProServiceId" : "DeskProServiceId",
}
```

#### Response:
Status: **Created(201)**

Example Response body: 

```
{“journeyStartUrl” : "/testUrl"}
```

## GET /journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.
#### Request:
A valid journeyId must be sent in the URI

#### Response:
Status:

| Expected Response                       | Reason  
|-----------------------------------------|------------------------------
| ```OK(200)```                           |  ```JourneyId exists```       
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```

Example response body:
```
{
  "companyProfile": {
    "companyName":"TestCompanyLtd”,
    “companyNumber":"01234567",
    "dateOfIncorporation":"2020-01-01",
    "unsanitisedCHROAddress": {
      "address_line_1":"testLine1",
      "address_line_2":"test town",
      "care_of":"test name",
      "country":"United Kingdom",
      "locality":"test city",
      "po_box":"123",
      "postal_code":"AA11AA",
      "premises":"1",
      "region":"test region"
    }
  },
  "ctutr":"1234567890",
  "identifiersMatch":true,
  "businessVerification": {
    "verificationStatus":"PASS"
  },
  "registration": {
     "registrationStatus":"REGISTERED",
     "registeredBusinessPartnerId":"X00000123456789"
  }
}
```
## Test End-Points
### GET test-only/feature-switches

---
Shows all feature switches:
1. Incorporated Entity Identification Frontend
    
    - Companies House API stub
    - Business Verification Stub
2. Incorporated Entity Identification
    
    - Get CT reference stub
    - DES stub
    
### GET/POST test-only/create-journey

---
This is a test entry point which simulates a service making the initial call to setup a journey.

1. ContinueURL(Required)

    - Where to redirect the user after the journey has been completed 
    
2. Service Name (Optional)

    - Service Name to use throughout the service
    - If nothing is entered, ```Entity Validation Service``` will be used
    
### GET test-only/:companyNumber/incorporated-company-profile

---
Stubs retrieving the Company Profile from Companies House. The Companies House API stub feature switch will need to be enabled.

##### Request:
A valid company Number must be sent in the URI

##### Response:
Status:

| Expected Response                       | Reason                              | Example
|-----------------------------------------|-------------------------------------|-------------------------------------
| ```OK(200)```                           |  ```Company Number exists```        | ```Any other valid Company Number```
| ```NOT_FOUND(404)```                    | ```Company Number doesn't exist```  | ```"00000001"```

Example response body:
```
{
  "companyProfile": {
    "companyName":"TestCompanyLtd”,
    “companyNumber":"01234567",
    "dateOfIncorporation":"2020-01-01",
    "unsanitisedCHROAddress": {
      "address_line_1":"testLine1",
      "address_line_2":"test town",
      "care_of":"test name",
      "country":"United Kingdom",
      "locality":"test city",
      "po_box":"123",
      "postal_code":"AA11AA",
      "premises":"1",
      "region":"test region"
    }
  }
}
```

### GET test-only/retrieve-journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.

##### Request:
A valid journeyId must be sent in the URI

##### Response:
Status:

| Expected Response                       | Reason  
|-----------------------------------------|------------------------------
| ```OK(200)```                           |  ```JourneyId exists```       
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist```

Example response body:
```
{
  "companyProfile": {
    "companyName":"TestCompanyLtd”,
    “companyNumber":"01234567",
    "dateOfIncorporation":"2020-01-01",
    "unsanitisedCHROAddress": {
      "address_line_1":"testLine1",
      "address_line_2":"test town",
      "care_of":"test name",
      "country":"United Kingdom",
      "locality":"test city",
      "po_box":"123",
      "postal_code":"AA11AA",
      "premises":"1",
      "region":"test region"
    }
  },
  "ctutr":"1234567890",
  "identifiersMatch":true,
  "businessVerification": {
    "verificationStatus":"PASS"
  },
  "registration": {
    "registrationStatus":"REGISTERED",
    "registeredBusinessPartnerId":"X00000123456789"
  }
}
```

### POST test-only/verification-question/journey

---
Stubs creating a Business Verification journey. The Business Verification Stub Feature Switch will need to be enabled. 

##### Request:
No body is required for this request

##### Response:
Status: **Created(201)**

Example Response body: 

```
{“redirectUri” : "/testUrl?journeyId=<businessVerificationJourneyId>"}
```

### GET  test-only/verification-question/journey/:journeyId/status

---
Stubs retrieving the result from the Business Verification Service. The Business Verification Stub feature switch will need to be enabled.

##### Request:
A valid Business Verification journeyId must be sent in the URI

##### Response:
Status: **OK(200)**

Example Response body: 
```
{
  "journeyType": "BUSINESS_VERIFICATION",
  "origin": vat,
  "identifier": {
    "ctUtr" -> "1234567890"
  },
  "verificationStatus" -> "PASS"
}
```
 
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
