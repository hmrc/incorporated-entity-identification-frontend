# Incorporated-Entity-Identification-Frontend

This is a Scala/Play frontend to allow Limited Companies to provide their information to HMRC.

### How to run the service

1. Make sure any dependent services are running using the following service-manager command
   `sm --start INCORPORATED_ENTITY_IDENTIFICATION_ALL -r`

2. Stop the frontend in service manager using
   `sm --stop INCORPORATED_ENTITY_IDENTIFICATION_FRONTEND`

3. Run the frontend locally using
   `sbt 'run 9718 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

## Testing

---
See [TestREADME](TestREADME.md) for more information about test data and endpoints

# End-Points

## POST /journey

### Deprecated - use POST /ltd-company/journey instead

---
Creates a new journey, storing the journeyConfig against the journeyId.

#### Request:

Request body must contain the continueUrl and deskProServiceId fields. If nothing is provided for the optional service
name, ```Entity Validation Service``` will be used.

The field businessVerificationCheck enables calling services to bypass business verification. If the field is set to "
false" and there is a successful match the entity will be registered. If not provided the default value used for this
field is "true".

All URLs provided must be relative, apart from locally, where localhost is allowed. All absolute urls will fail.

```
{
  "continueUrl" : "/testUrl",
  "businessVerificationCheck": false,
  "optServiceName" : "Service Name",
  "deskProServiceId" : "DeskProServiceId",
  "signOutUrl" : "/testSignOutUrl",
  "regime" : "VATC",
  "accessibilityUrl" : "/accessibility-statement/my-service"
}
```

#### Response:

Status: **Created(201)**

Example Response body:

```
{“journeyStartUrl” : "<protocol>://<host>:<port number>/identify-your-incorporated-business/<journey id>/company-number"}
```

where protocol, host and port number are set to the values for the appropriate environment and journey id is used to
identify the specific user journey.

## POST /limited-company-journey

---
Creates a new journey for Ltd Company, storing the journeyConfig against the journeyId.

#### Request:

Request body must contain the continueUrl and deskProServiceId fields. If nothing is provided for the optional service
name, ```Entity Validation Service``` will be used.

The field businessVerificationCheck enables calling services to bypass business verification. If the field is set to "
false" and there is a successful match the entity will be registered. If not provided the default value used for this
field is "true".

All URLs provided must be relative, apart from locally, where localhost is allowed. All absolute urls will fail.

```
{
  "continueUrl" : "/testUrl",
  "businessVerificationCheck": false,
  "optServiceName" : "Service Name",
  "deskProServiceId" : "DeskProServiceId",
  "signOutUrl" : "/testSignOutUrl",
  "regime" : "VATC",
  "accessibilityUrl" : "/accessibility-statement/my-service"
}
```

#### Response:

Status: **Created(201)**

Example Response body:

```
{“journeyStartUrl” : "<protocol>://<host>:<port number>/identify-your-incorporated-business/<journey id>/company-number"}
```

where protocol, host and port number are set to the values for the appropriate environment and journey id is used to
identify the specific user journey

## POST /registered-society-journey

---
Creates a new journey for Registered Society, storing the journeyConfig against the journeyId.

#### Request:

Request body must contain the continueUrl and deskProServiceId fields. If nothing is provided for the optional service
name, ```Entity Validation Service``` will be used.

The field businessVerificationCheck enables calling services to bypass business verification. If the field is set to "
false" and there is a successful match the entity will be registered. If not provided the default value used for this
field is "true".

All URLs provided must be relative, apart from locally, where localhost is allowed. All absolute urls will fail.

```
{
  "continueUrl" : "/testUrl",
  "businessVerificationCheck": false,
  "optServiceName" : "Service Name",
  "deskProServiceId" : "DeskProServiceId",
  "signOutUrl" : "/testSignOutUrl",
  "regime" : "VATC",
  "accessibilityUrl" : "/accessibility-statement/my-service"
}
```

#### Response:

Status: **Created(201)**

Example Response body:

```
{“journeyStartUrl” : "<protocol>://<host>:<port number>/identify-your-incorporated-business/<journey id>/company-number"}
```

where protocol, host and port number are set to the values for the appropriate environment and journey id is used to
identify the specific user journey

## POST /charitable-incorporated-organisation-journey

---
Creates a new journey for Charitable Incorporated Organisation, storing the journeyConfig against the journeyId.

#### Request:

Request body must contain the continueUrl and deskProServiceId fields. If nothing is provided for the optional service
name, ```Entity Validation Service``` will be used.

The field businessVerificationCheck enables calling services to bypass business verification. If the field is set to "
false" and there is a successful match the entity will be registered. If not provided the default value used for this
field is "true".

All URLs provided must be relative, apart from locally, where localhost is allowed. All absolute urls will fail.

```
{
  "continueUrl" : "/testUrl",
  "businessVerificationCheck": false,
  "optServiceName" : "Service Name",
  "deskProServiceId" : "DeskProServiceId",
  "signOutUrl" : "/testSignOutUrl",
  "regime" : "VATC",
  "accessibilityUrl" : "/accessibility-statement/my-service"
}
```

#### Response:

Status: **Created(201)**

Example Response body:

```
{“journeyStartUrl” : "<protocol>://<host>:<port number>/identify-your-incorporated-business/<journey id>/company-number"}
```

where protocol, host and port number are set to the values for the appropriate environment and journey id is used to
identify the specific user journey

## GET /journey/:journeyId

---
Retrieves all the journey data that is stored against a specific journeyID.

#### Request:

A valid journeyId must be sent in the URI

#### Response:

Status:

| Expected Response                       | Reason                        |
|-----------------------------------------|-------------------------------|
| ```OK(200)```                           | ```JourneyId exists```        |
| ```NOT_FOUND(404)```                    | ```JourneyId doesn't exist``` |

Example response bodies:

---
Limited Company/Registered Society when the Registration is successful:

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

Limited Company/Registered Society when the Registration is failed:

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
        "registrationStatus":"REGISTRATION_FAILED",
        "failures": [
            {
                "code": "PARTY_TYPE_MISMATCH",
                "reason": "The remote endpoint has indicated there is Party Type mismatch"
            }
        ]
  }
}

```
---
Charitable Incorporated Organisation:

```
{
   "identifiersMatch":false,
   "companyProfile":{
      "companyName":"Test Charity",
      "companyNumber":"CE123456",
      "dateOfIncorporation":"",
      "unsanitisedCHROAddress":{

      }
   },
   "businessVerification":{
      "verificationStatus":"UNCHALLENGED"
   },
   "registration":{
      "registrationStatus":"REGISTRATION_NOT_CALLED"
   }
}
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
