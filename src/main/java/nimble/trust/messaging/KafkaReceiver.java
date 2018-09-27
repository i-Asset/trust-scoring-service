package nimble.trust.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import nimble.trust.engine.service.ChangeEventHandlerService;
import nimble.trust.web.controller.TrustController;

/**
 * Created by Johannes Innerbichler on 27.09.18.
 */
@Component
public class KafkaReceiver {
	
	

	private static Logger log = LoggerFactory.getLogger(KafkaReceiver.class);
	
	@Autowired
	private ChangeEventHandlerService eventHandlerService;

    @KafkaListener(topics = "${nimble.kafka.topics.companyUpdates}")
    public void receiveCompanyUpdates(ConsumerRecord<?, ?> consumerRecord) {
    	String bearerToken = "someToken"; //TODO consumerRecord.getToken
        String partyId = consumerRecord.value().toString();   
        log.info("Received updated for company with ID: " + partyId);
        
        try {
    		
    		final Authentication auth = new UsernamePasswordAuthenticationToken(bearerToken, null);
    		SecurityContextHolder.getContext().setAuthentication(auth);
    		eventHandlerService.postChangeEvent(partyId);
		} catch (Exception e) {
			log.error("notificationTrustDataChange failed ", e);
		}
    }
}
