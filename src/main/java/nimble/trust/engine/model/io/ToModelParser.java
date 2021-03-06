package nimble.trust.engine.model.io;



import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import nimble.trust.common.Const;
import nimble.trust.engine.model.io.ext.SecGuaranteeToModel;
import nimble.trust.engine.model.pojo.Agent;
import nimble.trust.engine.model.pojo.CertificateAuthorityAttribute;
import nimble.trust.engine.model.pojo.Metric;
import nimble.trust.engine.model.pojo.MetricValue;
import nimble.trust.engine.model.pojo.SecurityAttribute;
import nimble.trust.engine.model.pojo.SecurityGuarantee;
import nimble.trust.engine.model.pojo.SecurityRequirment;
import nimble.trust.engine.model.pojo.TResource;
import nimble.trust.engine.model.pojo.TrustAttribute;
import nimble.trust.engine.model.pojo.TrustProfile;
import nimble.trust.engine.model.types.USDLSecExpression;
import nimble.trust.engine.model.vocabulary.ModelEnum;
import nimble.trust.engine.model.vocabulary.Trust;

public class ToModelParser {

	private static final Logger log = LoggerFactory.getLogger(ToModelParser.class);
	private HashMap<String, Object> specificParsers = Maps.newHashMap();

	/**
	 * 
	 * Takes Jena Rdf Model and converts it into POJO
	 * 
	 * @param model
	 *            jena rdf model
	 * @return TrustProfile as Java objects
	 */
	public TrustProfile parse(Model model) throws Exception {
		TrustProfile tp = null;
		log.debug("transforming " + model + " into Triples");
		OntModel oModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM, model);
		// oModel.add(model);
		Iterator<Individual> iterator = oModel.listIndividuals(Trust.TrustProfile);
		while (iterator.hasNext()) {
			Individual individual = (Individual) iterator.next();
			if (tp == null) {
				tp = new TrustProfile(URI.create(individual.getURI()));
				Resource r = findAgentURI(oModel);
				Agent agent = new Agent(URI.create(r.getURI()));
				populateCOMPOSE_ID(agent, r);
				tp.setAgent(agent);
			}
			parseAttributes(tp, individual);
			log.debug(individual.toString());
		}

		return tp;
	}

	private void populateCOMPOSE_ID(Agent agent, Resource resource) {
		Resource r1 = resource.getPropertyResourceValue((ModelFactory.createDefaultModel().createProperty(Trust.NS+"nimbleId")));
//		System.err.println(v);
		if (r1!=null){
			agent.setCompose_ID(URI.create(r1.getURI()));
		}
		Resource r2 = resource.getPropertyResourceValue((ModelFactory.createDefaultModel().createProperty(Trust.NS+"inputUID")));
		
		if (r2 == null){
			r2 =  ResourceFactory.createResource(agent.getUri().toString());
			resource.getModel().add(resource,(ModelFactory.createDefaultModel().createProperty(Trust.NS+"inputUID")),r2);
		}
		
		if (r2!=null){
			agent.setInputUID(URI.create(r2.getURI()));
		}
	}

	private Resource findAgentURI(OntModel oModel) throws Exception {
		ResIterator iterator = oModel.listSubjectsWithProperty(Trust.hasProfile);
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			log.error("Given trust profile is not assigned to any resource");
			throw new Exception("trust profile is not assigned to any resource");
		}
	}

	private void parseAttributes(TrustProfile tp, Individual tpIndividual) {

		NodeIterator nodeIterator = tpIndividual.listPropertyValues(Trust.hasAttribute);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = (RDFNode) nodeIterator.next();
			if (rdfNode.canAs(Individual.class)) {
				TrustAttribute attribute = null;
				Individual individual = rdfNode.as(Individual.class);
				Iterator<Resource> types = individual.listRDFTypes(true);

				// FIXME it needs better design, more generic type checking
				if (attribute == null) {
					
					if (isOfType(individual, Trust.CertificateAuthorityAttribute.getURI())){
						
						attribute = new CertificateAuthorityAttribute(URI.create(individual.getURI()));
						attribute = parseCertificateDetail(individual, (CertificateAuthorityAttribute) attribute);
						
					}
					else if (isOfType(individual, Trust.SecurityGuarantee.getURI())) {
						
						attribute = new SecurityGuarantee(URI.create(individual.getURI()));
						attribute = parseSecurityAttribute(individual, (SecurityAttribute) attribute);
						
					} else if (isOfType(individual, Trust.SecurityRequirment.getURI())) {
						
						attribute = new SecurityRequirment(URI.create(individual.getURI()));
						attribute = parseSecurityAttribute(individual, (SecurityAttribute) attribute);
						
					} else {
						
						attribute = new TrustAttribute(URI.create(individual.getURI()));
						parseAttributeValue(attribute, individual);
						
					}
				}

				while (types.hasNext()) {
					Resource type = (Resource) types.next();
					if (attribute != null) {
						attribute.addType(new nimble.trust.engine.model.pojo.TResource(URI.create(type.getURI())));
					}
				}
				tp.addAttribute(attribute);
			}
		}

	}

	
	private TrustAttribute parseCertificateDetail(Individual individual, CertificateAuthorityAttribute attribute) {
	
		RDFNode node = individual.getProperty(Trust.hasCertificateDetail).getObject();
		
		//FIXME - hasCertificateAuthority hard-coded
		RDFNode nodeCA = node.as(Individual.class).getPropertyValue(ModelFactory.createDefaultModel()
				.createProperty(ModelEnum.SecurityOntology.getURI()+"#hasCertificateAuthority"));
		
		//FIXME - hasCountry hard-coded
		RDFNode nodeCountry = node.as(Individual.class).getPropertyValue(ModelFactory.createDefaultModel()
				.createProperty(ModelEnum.SecurityOntology.getURI()+"#hasCountry"));
		
		if (nodeCA != null) 
			attribute.setCertificateAuthority(nodeCA.asNode().getURI());
		
		if (nodeCountry != null) 
			attribute.setCountry(nodeCountry.asNode().getURI());
			
		return attribute;
	}

	private boolean isOfType(Individual individual, String typeURI) {
		Iterator<Resource> types = individual.listRDFTypes(true);
		while (types.hasNext()) {
			Resource type = (Resource) types.next();
			if (type.getURI().equalsIgnoreCase(typeURI)) {
				return true;
			}
		}
		return false;
	}

	private void parseAttributeValue(TrustAttribute attribute, Individual individual) {
		RDFNode valueNode = individual.getPropertyValue(Trust.hasValue);
		Literal literal = valueNode.asLiteral();
		Object value = literal.getLexicalForm();
		attribute.setValue(value);
		attribute.setValueDatatype(literal.getDatatype());
	}
	
	
	private TrustAttribute parseSecurityAttribute(Individual individual, SecurityAttribute attribute) {
		
		SecGuaranteeToModel parser = (SecGuaranteeToModel) specificParsers.get(Const.parserNameSecurityGuarantee);
		
		parser.parse(individual, attribute);
		
		attribute.setValueDatatype(USDLSecExpression.TYPE);
		return attribute;
	}

	//i'm not using this one any more - as trust model has changed
	
//	private TrustAttribute parseSecurityAttributeHasValueUSDL(Individual individual, SecurityAttribute attribute) {
//		
//		System.out.println(individual);
//		
//		final RDFNode individualValue = individual.getPropertyValue(Trust.hasValue);
//		final RDFDatatype datype = individualValue.asLiteral().getDatatype();
//		final String lexicalForm = individualValue.asLiteral().getLexicalForm();
//		
//		//TODO needs better design/implementation. Not supported when sec description is not comming from predefined profile, but it in fact an expression
//		if (datype.getURI().equals(USDLSecExpression.TYPE.getURI())  && lexicalForm.startsWith(ModelEnum.SecurityProfiles.getURI())) {
//			SecProfileExpressionToModel parser = (SecProfileExpressionToModel) specificParsers.get(Const.parserNameSecurityProfileAsUSDLSecExpression);
//			if (parser == null) {
//				log.error("A parser to parse " + individualValue + " into java objects is not registered. Use registerSpecificParser(Object parser, String name)");
//			}
//			parser.parse(lexicalForm, attribute);
//		} else {
//			log.error("A parser to parse " + individualValue + " into java objects not supported / implemented");
//			throw new UnsupportedOperationException();
//		}
//
//		attribute.setValueDatatype(USDLSecExpression.TYPE);
//		return attribute;
//	}

	/**
	 * 
	 * @param parser
	 *            additional parser
	 * @param name
	 *            name of the additional parser
	 */
	public void registerSpecificParser(Object parser, String name) {
		specificParsers.put(name, parser);
	}

	public Metric parseMetric(Resource resource) {
		final Metric metric = new Metric(URI.create(resource.getURI()));
		metric.addType(new TResource(URI.create(Trust.Metric.getURI())));
		StmtIterator it = resource.listProperties(Trust.hasMetricValue);
		while (it.hasNext()) {
			Statement statement = (Statement) it.next();
			Resource resourceMetricValue = statement.getObject().asResource();
			MetricValue metricValue = getOrCreateMetricValue(metric, resourceMetricValue);
			if (resourceMetricValue.getProperty(Trust.next) != null) {
				Resource next = resourceMetricValue.getProperty(Trust.next).getObject().asResource();
				MetricValue metricValueNext = getOrCreateMetricValue(metric, next);
				metricValue.setNext(metricValueNext);
			}
		}
		return metric;
	}

	private MetricValue getOrCreateMetricValue(Metric metric, Resource resourceMetricValue) {
		List<MetricValue> list = metric.getMetricValues();
		for (MetricValue metricValue : list) {
			if (metricValue.getUri().toASCIIString().compareTo(resourceMetricValue.getURI()) == 0) {
				return metricValue;
			}
		}
		MetricValue metricValue = new MetricValue(URI.create(resourceMetricValue.getURI()));
		metricValue.addType(new TResource(URI.create(Trust.MetricValue.getURI())));
		if (resourceMetricValue.getProperty(Trust.rank) != null) {
			String rank = resourceMetricValue.getProperty(Trust.rank).getObject().asLiteral().getLexicalForm();
			metricValue.setRank(Double.valueOf(rank));
		}
		metric.addMetricValue(metricValue);
		return metricValue;
	}

}
