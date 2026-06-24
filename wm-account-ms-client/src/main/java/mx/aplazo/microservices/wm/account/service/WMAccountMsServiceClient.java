package mx.aplazo.microservices.wm.account.service;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import mx.aplazo.microservices.wm.account.model.response.WMAccountMsResponse;
import mx.aplazo.microservices.wm.account.model.request.WMAccountMsRequest;

/**
 * @author Aplazo
 *
 * TODO, This is a service of your microservice and it's
 * impemented on a controller.
 * 
 * 
 */

@FeignClient(name="WMAccountMs",url="${aplazo.url.api.group.apiname:http://localhost}")
public interface WMAccountMsServiceClient{
	
	@GetMapping("/api/v1")
	public String operation1();
		
	@PostMapping("/api/v1")
	public WMAccountMsResponse operation2(WMAccountMsRequest request);
	
}
