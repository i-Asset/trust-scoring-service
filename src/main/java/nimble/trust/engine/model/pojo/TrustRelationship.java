package nimble.trust.engine.model.pojo;



/**
 * Trust is a 5-tuple <Trustor, Trustee, Context, Set<Attribute>>
 * Within a Context, Trustor trusts to a Trustee who has Attributes.
 * 
 * @author marko
 *
 */

import java.net.URI;
import java.util.Date;

public class TrustRelationship extends TResource {

	public TrustRelationship(URI uri) {
		super(uri);
	}

	public Agent getTrustingParticipant() {
		return trustingParticipant;
	}
	public void setTrustingParticipant(Agent trustingParticipant) {
		this.trustingParticipant = trustingParticipant;
	}

	public Agent getTrustedParticipant() {
		return trustedParticipant;
	}

	public void setTrustedParticipant(Agent trustedParticipant) {
		this.trustedParticipant = trustedParticipant;
	}

	public TrustProfile getRequiredTrustProfile() {
		return requiredTrustProfile;
	}

	public void setRequiredTrustProfile(TrustProfile requiredTrustProfile) {
		this.requiredTrustProfile = requiredTrustProfile;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	private Agent trustingParticipant;
	private Agent trustedParticipant;
	private TrustProfile requiredTrustProfile;
	private double score;
	private Date timestamp;

	
}
