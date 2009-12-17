/**
 * 
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;

/**
 * @author elitau
 * 
 */
public class SemanticFlickr {
    static String defaultNameSpace = "http://www.neuezone.org/2009/11/17/flickr#";

    Model friends = null;
    Model schema = null;

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	SemanticFlickr hello = new SemanticFlickr();

	hello.createModel();
	hello.populateNewFriendsSchema();
	hello.populateNewFriends();
	hello.mySelf(hello.friends);
    }

    private void mySelf(Model model) {
	// Hello to Me - focused search
	runQuery(
		" select DISTINCT ?name where { flickr:CorleoneImage flickr:takenIn ?place. ?place flickr:hasName ?name }",
		model);

    }

    private void createModel() {
	friends = ModelFactory.createOntologyModel();
    }

    private void populateNewFriendsSchema() throws IOException {
	InputStream inFoafInstance = FileManager.get().open(
		"Ontologies/flickrSchema.owl");
	friends.read(inFoafInstance, defaultNameSpace);
	inFoafInstance.close();
    }

    private void runQuery(String queryRequest, Model model) {

	StringBuffer queryStr = new StringBuffer();
	// Establish Prefixes
	// Set default Name space first
	queryStr.append("PREFIX flickr" + ": <" + defaultNameSpace + "> ");
	queryStr.append("PREFIX rdfs" + ": <"
		+ "http://www.w3.org/2000/01/rdf-schema#" + "> ");
	queryStr.append("PREFIX rdf" + ": <"
		+ "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
	queryStr.append("PREFIX foaf" + ": <" + "http://xmlns.com/foaf/0.1/"
		+ "> ");

	// Now add query
	queryStr.append(queryRequest);
	Query query = QueryFactory.create(queryStr.toString());
	QueryExecution qexec = QueryExecutionFactory.create(query, model);
	try {
	    ResultSet response = qexec.execSelect();

	    while (response.hasNext()) {
		QuerySolution soln = response.nextSolution();
		RDFNode name = soln.get("?name");
		System.out.println(soln.toString());

		if (name != null) {
		    System.out.println("Hello to " + name.toString());
		} else
		    System.out.println("No Friends found!");
	    }
	} finally {
	    qexec.close();
	}
    }

    private void populateNewFriends() throws IOException {
	// friends = ModelFactory.createOntologyModel();
	InputStream inFoafInstance = FileManager.get().open(
		"Ontologies/flickrInstances.owl");
	friends.read(inFoafInstance, defaultNameSpace);
	inFoafInstance.close();

    }

}
