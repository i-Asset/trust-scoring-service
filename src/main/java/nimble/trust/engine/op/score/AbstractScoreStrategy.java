package nimble.trust.engine.op.score;



import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import nimble.trust.engine.model.expression.Element;
import nimble.trust.engine.model.expression.SingleElement;
import nimble.trust.engine.model.pojo.Agent;
import nimble.trust.engine.model.pojo.TrustAttribute;
import nimble.trust.engine.model.utils.TrustOntologyUtil;
import nimble.trust.util.tuple.Tuple2;

public abstract class AbstractScoreStrategy {
	
	protected List<TrustAttribute> attributeList = Lists.newArrayList();
	protected List<Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>>> dataSet;
	protected Map<TrustAttribute, Double> maxValues = Maps.newHashMap();
	protected Map<TrustAttribute, Double> minValues =  Maps.newHashMap();
	
	protected double weightsSum =  0;
	
	public AbstractScoreStrategy(List<SingleElement> listCriteria, final List<Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>>> dataSet) {

		List<SingleElement> elements = listCriteria;
		for (Element element : elements) {
			this.attributeList.add(((SingleElement)element).getAttribute());
		}
		this.dataSet = Lists.newArrayList(dataSet);
		sumAllWeights(attributeList);
		init();
	}
	
	public abstract Double getScore(Agent agent) ;
	
	protected abstract void init() ;
	
	
	protected void sumAllWeights(List<TrustAttribute> attributesToConsider){
		double sum = 0;
		for (TrustAttribute attr : attributesToConsider) {
			sum = sum + attr.getImportance();
		}
		weightsSum = sum;
	}
	
	/**
	 * 
	 * @param dataSet
	 * @param attributesToConsider
	 * @return
	 */
	protected Map<TrustAttribute, Double> identifyMaxValues(List<Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>>> dataSet, List<TrustAttribute> attributesToConsider) {
		for (TrustAttribute attr : attributesToConsider) {
			Double max = Double.valueOf(0);
			for (Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>> ta : dataSet) {
				for (Tuple2<TrustAttribute, Double> t : ta.getT2()) {
					if (TrustOntologyUtil.instance().sameURI(attr, t.getT1())
							&& (max < t.getT2())) {
						max = t.getT2();
					}
				}
			}
			maxValues.put(attr, max);
		}
		return maxValues;
	}
	
	/**
	 * 
	 * @param dataSet
	 * @param attributesToConsider
	 * @return
	 */
	protected Map<TrustAttribute, Double> identifyMinValues(List<Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>>> dataSet, List<TrustAttribute> attributesToConsider) {
		for (TrustAttribute attr : attributesToConsider) {
			Double min = 1000D;
			for (Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>> ta : dataSet) {
				for (Tuple2<TrustAttribute, Double> t : ta.getT2()) {
					if (TrustOntologyUtil.instance().sameURI(attr, t.getT1())
							&& (min > t.getT2())) {
						min = t.getT2();
					}
				}
			}
			minValues.put(attr, min);
		}
		return minValues;
	}
	
	
	protected List<Tuple2<TrustAttribute, Double>> obtainDataSetForAgent(Agent agent) {
		for (Tuple2<Agent, List<Tuple2<TrustAttribute, Double>>> t : dataSet) {
			if (TrustOntologyUtil.instance().sameURI(t.getT1(), agent))
				return t.getT2();
		}
		// this should never happen
		getLogger().error("Data for an agent {} are not contained in a dataSet given to {}", agent.getUri(), this.getClass());
		return null;
	}
	
	protected abstract Logger getLogger();

	/**
	 * 
	 * @param agentDataSet
	 * @param attribute
	 * @return
	 */
	protected Tuple2<TrustAttribute, Double> obtainTuple(final List<Tuple2<TrustAttribute, Double>> agentDataSet, final TrustAttribute attribute) {
		for (Tuple2<TrustAttribute, Double> tuple : agentDataSet) {
			TrustAttribute trustAttribute = tuple.getT1();
//			if (TrustOntologyUtil.ofSameTypeAsR1(attribute, trustAttribute)){
			if (TrustOntologyUtil.instance().sameURI(attribute, trustAttribute)){
				return tuple;
			}
		}
		return null;
	}

}
