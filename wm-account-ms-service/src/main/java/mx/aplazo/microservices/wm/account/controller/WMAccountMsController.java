package mx.aplazo.microservices.wm.account.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import mx.aplazo.microservices.wm.account.model.request.WMAccountMsRequest;
import mx.aplazo.microservices.wm.account.model.response.WMAccountMsResponse;
import mx.aplazo.microservices.wm.account.service.WMAccountMsServiceClient;

/**
 * @author Aplazo
 */
@Slf4j
@Tag(name="WMAccountMs")
@RestController
@RequestMapping( produces = MediaType.APPLICATION_JSON_VALUE)
public class WMAccountMsController implements WMAccountMsServiceClient {

	public String operation1() {
		return "Hola";
	}
	
	public WMAccountMsResponse operation2(WMAccountMsRequest request){
		return null;
	}

}
