package nimble.trust.engine.json;



import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nimble.trust.common.CompositionIdentifier;
import nimble.trust.util.tuple.Tuple2;

public class ProduceJSON {

	public String ofRankingResult(List<Tuple2<URI, Double>> list) {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ObjectNode rootNode = jacksonMapper.createObjectNode();
		rootNode.put("success", "true");
		ArrayNode arrayNode = rootNode.putArray("result");
		int i = 1;
		for (Tuple2<URI, Double> t : list) {
			ObjectNode node = jacksonMapper.createObjectNode();
			node.put("resourceURI", t.getT1().toASCIIString());
			node.put("index", t.getT2());
			node.put("rank", i++);
			arrayNode.add(node);
		}
		return rootNode.toString();
	}
	
	public String ofFilteringResult(List<URI> list) {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ObjectNode rootNode = jacksonMapper.createObjectNode();
		rootNode.put("success", "true");
		ArrayNode arrayNode = rootNode.putArray("result");
		for (URI t : list) {
			ObjectNode node = jacksonMapper.createObjectNode();
			node.put("resourceURI", t.toASCIIString());
			arrayNode.add(node);
		}
		return rootNode.toString();
	}
	
	
	public String ofError(Exception e) {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ObjectNode rootNode = jacksonMapper.createObjectNode();
		rootNode.put("success", "false");
		rootNode.put("message", e.getLocalizedMessage());
		return rootNode.toString();
	}
	
	public String ofErrorSimpleMessage(String message) {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ObjectNode rootNode = jacksonMapper.createObjectNode();
		rootNode.put("success", "false");
		rootNode.put("message", message);
		return rootNode.toString();
	}
	
	public static void main(String[] args) {
//		JsonObject jo = new JsonObject();
//		JsonArray array = new JsonArray();
//		JsonObject e= new JsonObject();
//		e.putString("resourceUri", "http/something");
//		e.putNumber("index", 5);
//		e.putNumber("rank", 1);
//		array.addElement(e);
//		array.addElement(e);
//		jo.putArray("result", array);
//		System.out.println(jo.encode());
//		System.out.println(jo.encodePrettily());
//		List<Tuple2<URI, Double>> l = Lists.newArrayList();
//		l.add(new Tuple2<URI, Double>(URI.create("http://localhost"), 1D));
//	
//		System.out.println(new MakeJson().ofError(new Exception("tes")));
	}

	
	public String ofFilteringCompositionsResult(List<CompositionIdentifier> filtered) {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ObjectNode rootNode = jacksonMapper.createObjectNode();
		rootNode.put("success", "true");
		ArrayNode arrayNode = rootNode.putArray("result");
		for (CompositionIdentifier t : filtered) {
			ObjectNode node = jacksonMapper.createObjectNode();
			node.put("compositionID", t.getId());
			arrayNode.add(node);
		}
		return rootNode.toString();
	}

	public String ofRankingCompositionsResult(List<Tuple2<CompositionIdentifier, Double>> scored) {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ObjectNode rootNode = jacksonMapper.createObjectNode();
		rootNode.put("success", "true");
		ArrayNode arrayNode = rootNode.putArray("result");
		int i = 1;
		for (Tuple2<CompositionIdentifier, Double> t : scored) {
			ObjectNode node = jacksonMapper.createObjectNode();
			node.put("compositionID", t.getT1().getId());
			node.put("score", t.getT2());
			node.put("rank", i++);
			arrayNode.add(node);
		}
		return rootNode.toString();
	}

}
