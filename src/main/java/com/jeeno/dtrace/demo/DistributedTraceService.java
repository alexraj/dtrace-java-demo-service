package com.jeeno.dtrace.demo;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.zipkin2.ZipkinProperties;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.cloud.sleuth.sampler.SamplerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class DistributedTraceService {
	@Autowired
	private ZipkinProperties zipkinProperties;

    @Value("${ZIPKIN_HOST:127.0.0.1}")
    private String host;

    @Value("${ZIPKIN_PORT:9411}")
    private String port;
    
    @Value("${SERVICE_ID}")
    private String serviceID;

    	
	@PostConstruct
    public void init() {
		zipkinProperties.setBaseUrl("http://" + host + ":" + port + "/");
		ZipkinProperties.Service svc = new ZipkinProperties.Service();
		String service = Constants.SERVICE_PREFIX + serviceID;
		svc.setName(service);
		zipkinProperties.setService(svc);
    }

	@Bean
	public ProbabilityBasedSampler getSampler() {
		SamplerProperties props = new SamplerProperties();
		props.setProbability(1.0f);
		return new ProbabilityBasedSampler(props);
	}
}