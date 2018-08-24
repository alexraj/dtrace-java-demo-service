# Distributed Tracing Demo Application in Java

Example distributed tracing application in Java using Spring sleuth cloud and Zipkin as tracing server. The services are numbered and they get called in ascending random sequence, forming a random call chain there by simulating service dependencies. For example; if you deploy four instances, and hit the first instance, it will randomly call 2 or 3 or 4. Similarly the second instance will call 3 or 4. To simulate real time behaviour, one in ten calls will have a random delay of 100 to 200 milliseconds, and one in hundred calls will result in error.

## Getting Started

git clone https://github.com/alexraj/dtrace-java-demo-service.git

### Prerequisites

- Java 1.8  
- Maven 3  
- [Jaeger](https://github.com/jaegertracing/jaeger) or [Zipkin](https://zipkin.io/pages/quickstart)
  
If you are running the Jaeger, start it with following command  
```
docker run -d -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp   -p5778:5778 -p80:16686 -p14268:14268 -p9411:9411  jaegertracing/all-in-one:latest
```

### Packaging

```
mvn clean package
docker build -t <docker_repo_url>/dtrace-java-demo-service .
```

## Deployment

The demo application can be run both in a localhost and in container environment

### Running Locally

#### Set Environment Variables
ZIPKIN_HOST = \<IP of Zipkin\>  # Default 127.0.0.1  
ZIPKIN_PORT = \<Port of Zipkin\> # Default 9411  
LOCAL_SERVICE_IP=\<IP of localhost\>  
SERVICE_PORT=\<Start port number\> #Default 9090  
  
To start four services  

```
java -jar -DSERVICE_ID=1 -DMAX_SERVICES=4 target/dtrace-java-demo-service-0.0.1-SNAPSHOT.jar
java -jar -DSERVICE_ID=2 -DMAX_SERVICES=4 target/dtrace-java-demo-service-0.0.1-SNAPSHOT.jar
java -jar -DSERVICE_ID=3 -DMAX_SERVICES=4 target/dtrace-java-demo-service-0.0.1-SNAPSHOT.jar
java -jar -DSERVICE_ID=4 -DMAX_SERVICES=4 target/dtrace-java-demo-service-0.0.1-SNAPSHOT.jar

```
This will start four services, they will listen on ports 9091, 9092, 9093, 9094

### Running as Container
TODO

## Verifying
```
curl http://localhost:9091/zipkin
```
will return one of the following  
  
service-1 => service-2 => service-3 => service-4  
service-1 => service-2 => service-4  
service-1 => service-3 => service-4  
service-1 => service-4  

You may get an error too with a probability of 1/100.

Go to http://\<zipkin host\> to see the traces and dependencies.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

