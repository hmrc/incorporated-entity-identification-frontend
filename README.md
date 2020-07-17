
# incorporated-entity-identification-frontend
This is a Scala/Play frontend to allow Limited Companies to provide their information to HMRC.

### How to run the service
1. Make sure any dependent services are running using the following service-manager command
`sm --start INCORPORATED_ENTITY_IDENTIFICATION_ALL`

2. Stop the frontend in service manager using
 `sm --stop INCORPORATED_ENTITY_IDENTIFICATION_FRONTEND`
 
3. Run the frontend locally using
`sbt 'run 9718 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
