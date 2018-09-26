package nimble.trust.engine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.AsyncEventBus;

import nimble.trust.engine.collector.ProfileCompletnessCollector;
import nimble.trust.engine.model.vocabulary.Trust;
import nimble.trust.web.dto.ChangeEvent;

@Service
public class ChangeEventHandlerService {

	@Autowired
	private AsyncEventBus asyncEventBus;

	@Autowired
	private ProfileCompletnessCollector profileCompletnessCollector;

	public void postChangeEvent(ChangeEvent changeEvent) throws Exception {
		asyncEventBus.post(changeEvent);
	}

	public void handleChangeEvent(ChangeEvent changeEvent) throws Exception {

		String type = changeEvent.getChangeType();

		if (type.equalsIgnoreCase("company_details")) {
			profileCompletnessCollector.obtainNewValueCompanyProfile(changeEvent.getCompanyIdentifier(),
					Trust.ProfileCompletnessDetails.getLocalName());
		}
		if (type.equalsIgnoreCase("company_description")) {
			profileCompletnessCollector.obtainNewValueCompanyProfile(changeEvent.getCompanyIdentifier(),
					Trust.ProfileCompletnessDescription.getLocalName());
		}
		if (type.equalsIgnoreCase("company_certificates")) {
			profileCompletnessCollector.obtainNewValueCompanyProfile(changeEvent.getCompanyIdentifier(),
					Trust.ProfileCompletnessCertificates.getLocalName());
		}
		if (type.equalsIgnoreCase("company_trade")) {
			profileCompletnessCollector.obtainNewValueCompanyProfile(changeEvent.getCompanyIdentifier(),
					Trust.ProfileCompletnessTrade.getLocalName());
		}
	}

}