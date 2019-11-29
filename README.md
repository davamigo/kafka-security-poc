# Kafka Security PoC

## Configuration

### Certificates

You must add the JKS certificates to this folder: `<project-root>/src/main/resources/certs`

- `client-keystore.jks`
- `client-truststore.jks`

Optionally you can add different profile files to the same folder:

- `prod-client-keystore.jks`
- `prod-client-truststore.jks`
- `int-client-keystore.jks`
- `int-client-truststore.jks`
- (...)

### properties

You must to add the next properties to `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: "<your-broker1>:9093[, <your-broker2>:9093[, ...]]"

    ssl:
      key:
        password: "<your-key-password>"
      keystore:
        location: "certs/<your-client-keystore-file>.jks"
        password: "<your-keystore-password>"
      truststore:
        location: "certs/<your-client-truststore-file>.jks"
        password: "<your-truststore-password>"
```

### Profiles

Optionally you can create profile property files: `application-<profile>.yml`.
Example: `application-admin.yml`.
The  default profile is `admin`.

## Run the project

This program runs always from the command line. It doesn't have web interface.
It can only **produce** to Kafka o **consume** from Kafka.

```
> Go to project root folder
$ cd kafka-security

> Build the project
$ ./gradlew build

> Start the project
$ ./gradlew bootRun [--args='<arguments>']
```

The valid program arguments are:

- `--produce [num]` - Produces one or more messages to Kafka. `num` is optional and its default value is `1`.
- `--consume` - Starts the consumer. The programs doesn't end until the user press `Ctrl+C`.
- `--spring.profiles.active=<profile>` - To choose a different profile.

By default the program does nothing. You must set `--produce` or `--consume`.
And you can set both.

Examples:

- Produce 1 message and exit:
    ```bash
    $ ./gradlew bootRun --args='--produce'
    ```

- Produce 50 messages and exit:
    ```bash
    $ ./gradlew bootRun --args='--produce 50'
    ```

- Start the consumer:
    ```bash
    $ ./gradlew bootRun --args='--consume'
    ```

- Produce one message and start the consumer:
    ```bash
    $ ./gradlew bootRun --args='--produce --consume'
    ```

- Produce using the profile `producer`. This will load the file `application-producer.yml` is exists:
    ```bash
    $ ./gradlew bootRun --args='--spring.profiles.active=producer --produce'
    ```

- Consume using the profile `consumer`. This will load the file `application-consumer.yml` is exists:
    ```bash
    $ ./gradlew bootRun --args='--spring.profiles.active=consumer --consume'
    ```

## Annexes

### Quick test of certificates

You can do a quick test of your JKS certificate files using docker image **[bitnami/kafka](https://hub.docker.com/r/bitnami/kafka)**, one of the most user Apache Kafka container images:

- Go to the directory where the client keystore and truststore certificates are.

- Create `client-ssl.properties` file:
    ```
    security.protocol=SSL
    ssl.key.password=<your-key-password>

    ## KEYSTORE
    ssl.keystore.location=/certs/<your-client-keystore-file>.jks
    ssl.keystore.password=<your-keystore-password>

    ### TRUSTSTORE
    ssl.truststore.location=/certs/<your-client-truststore-file>.jks
    ssl.truststore.password=<your-truststore-password>
    ```

- Run this command:
    ```bash
    docker run -it --rm \
        -v $(pwd)/:/certs \
        bitnami/kafka kafka-topics.sh \
            --list \
            --bootstrap-server <your-broker1>:9093[, <your-broker2>:9093[, ...]] \
            --command-config /certs/client-ssl.properties
    ```

## Links
* [Security at Kafka Documentation](https://kafka.apache.org/documentation/#security)
* [Encryption and Authentication with SSL](https://docs.confluent.io/current/kafka/authentication_ssl.html)
