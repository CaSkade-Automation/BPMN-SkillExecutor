package de.hsuhh.aut.skills.bpmn.delegates;

public enum StateTypeIri {
    Aborting ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Aborting"),
    Clearing ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Clearing"),
    Complete ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Complete"),
    Completing ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Completing"),
    Execute ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Execute"),
    Holding ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Holding"),
    Resetting ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Resetting"),
    Starting ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Starting"),
    Stopping ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Stopping"),
    Suspending("http://www.hsu-ifa.de/ontologies/ISA-TR88#Suspending"),
    Unholding("http://www.hsu-ifa.de/ontologies/ISA-TR88#Unholding"),
    Unsuspending("http://www.hsu-ifa.de/ontologies/ISA-TR88#Unsuspending"),

    Aborted ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Aborted"),
    Held ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Held"),
    Idle ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Idle"),
    Stopped ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Stopped"),
    Suspended ("http://www.hsu-ifa.de/ontologies/ISA-TR88#Suspended");
    
	private final String iri;
	
    StateTypeIri(String iri) {
    	this.iri = iri;
    }
    
    
    public String getIri() {
    	return this.iri;
    }
    
    public boolean equals(String otherIri) {
        return this.iri.equals(otherIri);
    }
    
    /**
     * Checks if a given IRI is the IRI of a final state type (i.e., Complete, Aborted, Stopped)
     * @param otherIri IRI of a state to check
     * @return true if the state is final, false if not
     */
    public static boolean isFinalState(String otherIri) {
    	return StateTypeIri.Complete.equals(otherIri) || StateTypeIri.Aborted.equals(otherIri) || StateTypeIri.Stopped.equals(otherIri);
    }
    
    /**
     * Util function to get an Enum value from the complete IRI
     * @param iri IRI of an Enum value
     * @return A matching Enum value if existing
     */
    public static StateTypeIri fromString(String iri) {
        for (StateTypeIri b : StateTypeIri.values()) {
            if (b.iri.equals(iri)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with IRI " + iri + " found");
    }

}
