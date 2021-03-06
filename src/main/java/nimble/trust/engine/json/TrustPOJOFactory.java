package nimble.trust.engine.json;



import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDDouble;

import nimble.trust.engine.model.expression.ExpressionBuilder;
import nimble.trust.engine.model.factory.TrustModelFactory;
import nimble.trust.engine.model.pojo.CertificateAuthorityAttribute;
import nimble.trust.engine.model.pojo.SecurityAttribute;
import nimble.trust.engine.model.pojo.SecurityGoal;
import nimble.trust.engine.model.pojo.SecurityMechanism;
import nimble.trust.engine.model.pojo.SecurityTechnology;
import nimble.trust.engine.model.pojo.TrustAttribute;
import nimble.trust.engine.model.pojo.TrustCriteria;
import nimble.trust.engine.model.types.USDLSecExpression;
import nimble.trust.engine.model.utils.TrustOntologyUtil;
import nimble.trust.engine.model.vocabulary.Trust;
import nimble.trust.util.uri.UIDGenerator;
import nimble.trust.web.dto.DtoUtil;


public class TrustPOJOFactory {
	
	
	final TrustModelFactory factory = new TrustModelFactory(UIDGenerator.instanceRequest);

	public TrustCriteria ofTrustCriteria(String json) {
		ObjectMapper m = new ObjectMapper();
		ExpressionBuilder builder = new ExpressionBuilder().startNewTrustCriteria();
		try {
			
			JsonNode rootNode = m.readTree(json);
			JsonNode attributesNode = rootNode.get("attributes");
			for (JsonNode attributeNode : attributesNode) {
				if (attributeNode.has("or") == false){
					TrustAttribute attribute = createTrustAttribute(attributeNode);
					builder = builder.attribute(attribute).and();
				}
				else{
					JsonNode disjuncted = attributeNode.get("or").get("attributes");
					JsonNode importance = attributeNode.get("or").get("importance");
					Double importanceOr = obtainImportance(importance);
					processDisjuncted(disjuncted,importanceOr, builder);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return builder.build();
	}

	private void processDisjuncted(JsonNode disjuncted, Double importanceOr, ExpressionBuilder builder) {
		builder = builder.openOrBracket(importanceOr);
		for (JsonNode attributeNode : disjuncted) {
			TrustAttribute attribute = createTrustAttribute(attributeNode);
			builder = builder.attribute(attribute).or();
		}
		builder = builder.closeOrBracket();
	}

	private TrustAttribute createTrustAttribute(JsonNode element) {

		JsonNode importance = element.get("importance");
		JsonNode weight = element.get("weight");
		JsonNode value = element.get("value");
		JsonNode minvalue = element.get("minValue");
		JsonNode maxvalue = element.get("maxValue");
		JsonNode type = element.get("type");

		TrustAttribute attr = null;
		Double d_importance = obtainImportance(importance);
		if (importance == null && obtainImportance(weight) != null) {
			d_importance = obtainImportance(weight);
		}
		Object o_value = obtainValue(value);
		Object o_valueMin = obtainValueMin(minvalue);
		Object o_valueMax = obtainValueMax(maxvalue);

		JsonNode expression = element.get("expression");
		if (obtainValue(expression) != null) {
			nimble.trust.engine.domain.TrustAttribute a = new nimble.trust.engine.domain.TrustAttribute();
			a = DtoUtil.resolveExpression(a, obtainValue(expression).toString());
			o_value = a.getValue();
			o_valueMin = a.getMinValue();
			o_valueMax = a.getMaxValue();
		}

		String sURI = type.asText();
		if (sURI.contains(Trust.NS) == false) {
			sURI = Trust.NS + sURI;
		}
		URI uri = URI.create(sURI);

		boolean isSecurity = TrustOntologyUtil.instance().isSubtype(sURI, Trust.SecurityAttribute.getURI());
		boolean isCertificate = sURI.equals(Trust.CertificateAuthorityAttribute.getURI());

		if (isSecurity && isCertificate == false) {
			attr = createPopulateSecurityAttribute(element);
			attr.setValueDatatype(USDLSecExpression.TYPE);
		} else if (isCertificate) {
			attr = createAndPopulateCertificateAuthorityAttribute(element);
		}
		else {
			attr = factory.createTrustAttibute();
			if (isNumeric(o_value)) {
				attr.setValueDatatype(XSDDouble.XSDdouble);
			} else {
				// FIXME Scale - Attribute link should be defined in a model /
				// kb
				if (uri.toASCIIString().equals(Trust.Reputation.getURI())) {
					attr.setValueDatatype(new BaseDatatype(Trust.ReputationScale.getURI()));
				} else if (uri.toASCIIString().equals(Trust.UserRating.getURI())) {
					attr.setValueDatatype(new BaseDatatype(Trust.RatingScale.getURI()));
				} else {
					attr.setValueDatatype(XSDDouble.XSDdouble);
				}
			}
		}
		attr.addType(uri);
		attr.setImportance(d_importance);
		attr.setValue(o_value);
		attr.setMinValue(o_valueMin);
		attr.setMaxValue(o_valueMax);
		// System.out.println(attr);
		return attr;
	}

	
	private TrustAttribute createAndPopulateCertificateAuthorityAttribute(JsonNode element) {
		CertificateAuthorityAttribute attr = factory.createCertificateAuthorityAttribute();
		
		JsonNode node = element.path("value").path("certificateauthority");
		if (node != null){
			for (JsonNode subnode : node) {
				attr.setCertificateAuthority(obtainType(subnode).toASCIIString());
			}
		}
		
		node = element.path("value").path("country");
		if (node != null){
			for (JsonNode subnode : node) {
				attr.setCountry(obtainType(subnode).toASCIIString());
			}
		}
		
		return attr;
	}

	private TrustAttribute createPopulateSecurityAttribute(JsonNode element) {
		SecurityAttribute attr = factory.createSecurityAttribute();
		
		JsonNode securityGoals = element.path("value").path("securitygoal");
		if (securityGoals!=null){
			for (JsonNode goalnode : securityGoals) {
				attr.addSecurityGoal(new SecurityGoal(obtainType(goalnode)));
			}
		}
		
		JsonNode implementedBy = element.path("value").get("securitymechanism");
		if (implementedBy != null) {
			for (JsonNode implementedByElement : implementedBy) {
				SecurityMechanism mechanism = factory.createSecurityMechanism();
				mechanism.addType(obtainType(implementedByElement));
				attr.addImplementedBy(mechanism);
			}
		}

		JsonNode realizedByTechnology = element.path("value").path("securitytechnology");
		if (realizedByTechnology!=null){
			for (JsonNode realizedByElement : realizedByTechnology) {
				SecurityTechnology technology = new SecurityTechnology(obtainType(realizedByElement));
				technology.addType(obtainType(realizedByElement));
				attr.addRealizedByTechnology(technology);
			}
		}
		
		return attr;
	}

	private URI obtainType(JsonNode node) {
		String sURI = node.path("type").asText();
		return URI.create(sURI);
	}

	private boolean isNumeric(Object o_value) {
		if (o_value ==null) return false;
		try {
			new Double(o_value.toString());
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private Object obtainValueMax(JsonNode maxvalue) {
		if (maxvalue !=null){
			return maxvalue.asText();
		}
		return null;
	}

	
	private Object obtainValueMin(JsonNode minvalue) {
		if (minvalue !=null){
			return minvalue.asText();
		}
		return null;
	}

	
	private Object obtainValue(JsonNode value) {
		if (value !=null){
			return value.asText();
		}
		return null;
	}

	private Double obtainImportance(JsonNode importance) {
		Double i =1D;
		if (importance !=null){
			 i = importance.asDouble();
		}
		return i;
	}
}
