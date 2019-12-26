[![Build Status](https://travis-ci.org/xm-online/tmf-ms-document.svg?branch=master)](https://travis-ci.org/xm-online/tmf-ms-document) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?&metric=sqale_index&branch=master&project=xm-online:tmf-ms-document)](https://sonarcloud.io/dashboard/index/xm-online:tmf-ms-document) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?&metric=ncloc&branch=master&project=xm-online:tmf-ms-document)](https://sonarcloud.io/dashboard/index/xm-online:tmf-ms-document) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?&metric=coverage&branch=master&project=xm-online:tmf-ms-document)](https://sonarcloud.io/dashboard/index/xm-online:tmf-ms-document)

# document

This application was generated using JHipster 6.5.1, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v6.5.1](https://www.jhipster.tech/documentation-archive/v6.5.1).

This is a "microservice" application intended to be part of a microservice architecture, please refer to the [Doing microservices with JHipster][] page of the documentation for more information.

This application is configured for Service Discovery and Configuration with Consul. On launch, it will refuse to start if it is not able to connect to Consul at [http://localhost:8500](http://localhost:8500). For more information, read our documentation on [Service Discovery and Configuration with Consul][].

## Development

To start your application in the dev profile, simply run:

    ./gradlew

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

### Doing API-First development using openapi-generator

[OpenAPI-Generator]() is configured for this application. You can generate API code from the `src/main/resources/swagger/api.yml` definition file by running:

```bash
./gradlew openApiGenerateDocument
```

Then implements the generated delegate classes with `@Service` classes.

To edit the `api.yml` definition file, you can use a tool such as [Swagger-Editor](). Start a local instance of the swagger-editor using docker by running: `docker-compose -f src/main/docker/swagger-editor.yml up -d`. The editor will then be reachable at [http://localhost:7742](http://localhost:7742).

Refer to [Doing API-First development][] for more details.

## Building for production

### Packaging as jar

To build the final jar and optimize the documents application for production, run:

    ./gradlew -Pprod clean bootJar

To ensure everything worked, run:

    java -jar build/libs/*.jar

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

    ./gradlew -Pprod -Pwar clean bootWar

## Testing

To launch your application's tests, run:

    ./gradlew test integrationTest jacocoTestReport

For more information, refer to the [Running tests page][].

### Code quality

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker-compose -f src/main/docker/sonar.yml up -d
```

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the gradle plugin.

Then, run a Sonar analysis:

```
./gradlew -Pprod clean check jacocoTestReport sonarqube
```

For more information, refer to the [Code quality page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.

For example, to start a postgresql database in a docker container, run:

    docker-compose -f src/main/docker/postgresql.yml up -d

To stop it and remove the container, run:

    docker-compose -f src/main/docker/postgresql.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    ./gradlew bootJar -Pprod jibDockerBuild

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`jhipster docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 6.5.1 archive]: https://www.jhipster.tech/documentation-archive/v6.5.1
[doing microservices with jhipster]: https://www.jhipster.tech/documentation-archive/v6.5.1/microservices-architecture/
[using jhipster in development]: https://www.jhipster.tech/documentation-archive/v6.5.1/development/
[service discovery and configuration with consul]: https://www.jhipster.tech/documentation-archive/v6.5.1/microservices-architecture/#consul
[using docker and docker-compose]: https://www.jhipster.tech/documentation-archive/v6.5.1/docker-compose
[using jhipster in production]: https://www.jhipster.tech/documentation-archive/v6.5.1/production/
[running tests page]: https://www.jhipster.tech/documentation-archive/v6.5.1/running-tests/
[code quality page]: https://www.jhipster.tech/documentation-archive/v6.5.1/code-quality/
[setting up continuous integration]: https://www.jhipster.tech/documentation-archive/v6.5.1/setting-up-ci/

[openapi-generator]: https://openapi-generator.tech
[swagger-editor]: http://editor.swagger.io
[doing api-first development]: https://www.jhipster.tech/documentation-archive/v5.7.2/doing-api-first-development/

## Document generation

### MS config document folder structure
```
<TENANT>
├── document
│   ├── documents.yml -> document specifications config file
│   ├── lep
│   │   └── mapper
│   │       └── DocumentContextMapping$$TEST_DOCUMENT$$around.groovy -> document context mapping LEP
│   └── templates
│       └── jasper
│           └── test_document.jrxml -> JasperReports template
```

To generates document, describe document specification in the YAML file (_documents.yml_):
Example:
```yaml
TEST_DOCUMENT:
  allowedDocumentMimeTypes: [application/pdf, text/xml]
  defaultDocumentMimeType: application/pdf
  renderer: JASPER_REPORTS
```
where:
* `TEST_DOCUMENT` - unique key of the document specification used to specify which type of a document to generate
* `allowedDocumentMimeTypes` - list of the allowed document formats specified as MIME types (_application/pdf, text/xml etc._)
* `defaultDocumentMimeType` - default document format to use if no is specified
* `renderer` - type of the renderer to use for document generation. Currently available (with supported mime types): 
    * JASPER_REPORTS (_application/pdf_, _application/xml_)

### How to generate documents with [JasperReports](https://community.jaspersoft.com/project/jasperreports-library)

1. Describe document specification in the documents YAML file with `renderer: JASPER_REPORTS`
2. Add JasperReports `.jrxml` file to `templates/jasper` with document specification key in lower case as a filename (e.g. key = _TEST_DOCUMENT_ - file = _test_document.jrxml_).

    You can create and edit JasperReports template with [Jaspersoft Studio](https://community.jaspersoft.com/project/jaspersoft-studio)
3. (Optional) Add LEP script for mapping input document context to renderer specific model:
    * location: `lep/mapper`
    * script name format: `DocumentContextMapping$${DOCUMENT_KEY}$$around.groovy` 
        - where _{DOCUMENT_KEY}_ - key of the document specification
        
    **Mapping script example:**
    ```groovy
    import static com.icthh.xm.tmf.ms.document.service.generation.util.DocumentContextMappingUtils.joinNullSafe
    
    def context = lepContext.inArgs.context
    
    Map<String, Closure> mappingFunctions = [
            'назва': { ctx ->
                ctx.article.name
            },
            'опис' : { ctx ->
                joinNullSafe(', ', ctx.article.first, ctx.article.second)
            }
    ]
    
    return mappingFunctions.collectEntries(new HashMap<String, String>(), {
        field, func -> [field, func(context)]
    })
    ```
    This example shows how you can create a simple map with friendly field names as keys 
    from a complex document context object. Further this map will be passed to Jasper template 
    where you can declare fields by map's keys (ex. _$F{назва}_).
4. Generate documents by REST endpoint: 
    
    `POST /api/documentManagement/binaryDocument/generate`

    **Request body:**
    ```json
    {
        "key": "TEST_DOCUMENT",
        "documentContext": {
            "article": {
                "name": "Назва статті",
                "first": "перша частина опису",
                "second": "друга частина опису"
            }
        },
        "documentMimeType": "application/pdf"
    }
    ```
    where:
    * `key` - document specification key
    * `documentContext` - arbitrary document context
    * (Optional) `documentMimeType` - document mime type
    
    **Response:** document file with expected mime type and with document specification key 
    in lower case as a filename (e.g. key = _TEST_DOCUMENT_ - filename = _test_document_)
    
**FONTS ISSUE:**
There are several fonts that can be used in documents for all environments, see [Default Fonts in JasperReports](https://community.jaspersoft.com/documentation/jasperreports-server-user-guide/using-default-fonts-jasperreports-server).
For other fonts you need to explicitly add them to classpath of the application:
1. Find required fonts.
2. Export them as `.jar` via [Jaspersoft Studio](https://community.jaspersoft.com/documentation/tibco-jaspersoft-studio-user-guide/v640/working-font-extensions).
3. Add jar file to classpath of the application.
4. Now you can use them in your documents.
