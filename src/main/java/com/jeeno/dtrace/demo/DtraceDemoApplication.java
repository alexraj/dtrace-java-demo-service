package com.jeeno.dtrace.demo;

import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.cloud.sleuth.SpanAdjuster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jeeno.dtrace.DistributedTraceService;

@SpringBootApplication
@ComponentScan({"com.jeeno.dtrace.demo"})
public class DtraceDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DtraceDemoApplication.class, args);
	}
}

@Configuration
class ServletConfig {
    @Value("${SERVICE_ID}")
    private String serviceID;

    @Value("${SERVICE_PORT:9090}")
    private String servicePort;

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return (container -> {
            container.setPort(Integer.parseInt(servicePort) + Integer.parseInt(serviceID));
        });
    }
}

@RestController
class DtraceDemoController{
	
	public String getDNS(int serviceID) {
		if (localServiceIP != null && localServiceIP.length() != 0) {
			int basePort = Integer.parseInt(servicePort);
			return localServiceIP + ":" + (basePort + serviceID);
		}
		return dnsPrefix + Constants.SERVICE_PREFIX + serviceID + dnsSuffix;
	}
	
    @Value("${SERVICE_PORT:9090}")
    private String servicePort;
	
    @Value("${SERVICE_ID}")
    private String serviceID;
    
    @Value("${DNS_PREFIX:}")
    private String dnsPrefix;
    
    @Value("${DNS_SUFFIX:}")
    private String dnsSuffix;
    
    @Value("${MAX_SERVICES:4}")
    private String maxServices;
    
    @Value("${LOCAL_SERVICE_IP:}")
    private String localServiceIP;

    @Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	DistributedTraceService distributedTraceService;

	@Bean
	SpanAdjuster customSpanAdjuster() {
	    return span -> span.toBuilder().putTag("env", "dev")
	    		.putTag("service", distributedTraceService.getServiceName())
	    		.putTag("vm", distributedTraceService.getVm()).build();
	}
	
	private static final Logger LOG = Logger.getLogger(DtraceDemoController.class.getName());
	
	/**
	 * One in ten calls will have random delay
	 * One in hundred calls will have failure
	 * @return
	 */
	@GetMapping(value="/zipkin")
	public ResponseEntity<String> zipkinService1() {
		Random r = new Random();
		if (r.nextInt(10) == 0) {
			int delay = r.nextInt(100) + 100;
			LOG.info("Request delayed by " + delay + " msecs.");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (r.nextInt(100) == 0) {
			LOG.warn("Request failed once in 100 times");
			throw new RuntimeException("Fail the call once in 100 times");
		}

		String service = "service-" + serviceID;
		int sid = Integer.parseInt(serviceID);
		int nServices = Integer.parseInt(maxServices);
		if (sid == nServices) {
			return ResponseEntity.ok(service);
		}

		int downSid = r.nextInt(nServices - sid) + sid + 1;	// Id greater than current
		String resp = restTemplate.exchange("http://" + getDNS(downSid) + "/zipkin", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
        }).getBody();
		return ResponseEntity.ok(service + " => " + resp);
	}
	
	@GetMapping(value="/health")
	public ResponseEntity<String> checkHealth() {
		return ResponseEntity.ok("success");
	}
}