/**
 * 
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
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
    static String flickrNameSpace = "http://www.neuezone.org/2009/11/17/flickr#";
    static String dbPediaNameSpace = "http://dbpedia.org/#";
    static String foafNameSpace = "http://xmlns.com/foaf/0.1/";

    static String owlNameSpace = "http://www.w3.org/2002/07/owl#";
    static String rdfsNameSpace = "http://www.w3.org/2000/01/rdf-schema#";
    static String rdfNameSpace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static String dbpPropertyNameSpace = "http://dbpedia.org/property/";
    static String dbpOntologyNameSpace = "http://dbpedia.org/ontology/";
    static String dbpResourceNameSpace = "http://dbpedia.org/resource/";

    static String dbpediaSparqlEndpoint = "http://dbpedia.org/sparql?default-graph-uri=http%3A//dbpedia.org&query=";

    Model images = null;
    Model schema = null;
    Model inferredModel = null;

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	SemanticFlickr hello = new SemanticFlickr();

	hello.createModel();
	hello.populateNewFlickrSchema();
	hello.populateLocalFlickrImages();
	// hello.mySelf(hello.images);

	hello.populateDbPediaSchema();
	hello.addAlignment();
	hello.bindReasoner();
	// hello.queryDbPedia(hello.images);
	hello.queryInferredModel(hello.inferredModel);
    }

    private void queryDbPedia(Model model) {
	System.out.println("Query for DbPedia");
	runQuery(" SELECT ?name WHERE { "
	// +
		// "<http://dbpedia.org/resource/Corleone> <http://dbpedia.org/property/abstract> ?name } "
		+ "dbpr:Corleone rdf:type ?name " + "} "
	// +
		// "skos:subject <http://dbpedia.org/resource/Category:Cities%2C_towns_and_villages_in_Slovenia> ; "
		// + "dbpprop:officialName ?name ; "
		// + "dbpprop:abstract ?abs ." + " }"
		, model, false);
    }

    private void queryInferredModel(Model model) {
	System.out.println("Query on inferredModel");
	String response;
	response = runQuery(
		" SELECT ?name WHERE "
			+ "{ flickr:CorleoneImage flickr:takenIn ?place. ?place flickr:hasName ?name } ",
		model, true);
	// String place = this.extractFromResult(response, "?name");
	response = runQuery(" SELECT ?name WHERE " + "{ " + "dbpr:" + response
		+ " dbpp:abstract ?name  } ", model, false);

	System.out.println(response);

    }

    private void mySelf(Model model) {
	// Hello to Me - focused search
	runQuery(
		" select DISTINCT ?name where { flickr:CorleoneImage flickr:takenIn ?place. ?place flickr:hasName ?name }",
		model, true);

    }

    private void createModel() {
	images = ModelFactory.createOntologyModel();
	schema = ModelFactory.createOntologyModel();
    }

    private String runQuery(String queryRequest, Model model, boolean local) {

	StringBuffer queryStr = new StringBuffer();
	// Establish Prefixes
	// Set default Name space first
	appendNameSpacePrefix("flickr", flickrNameSpace, queryStr);
	appendNameSpacePrefix("rdf", rdfNameSpace, queryStr);
	appendNameSpacePrefix("rdfs", rdfsNameSpace, queryStr);
	appendNameSpacePrefix("owl", owlNameSpace, queryStr);
	appendNameSpacePrefix("dbpp", dbpPropertyNameSpace, queryStr);
	appendNameSpacePrefix("dbpo", dbpOntologyNameSpace, queryStr);
	appendNameSpacePrefix("dbpr", dbpResourceNameSpace, queryStr);
	appendNameSpacePrefix("foaf", foafNameSpace, queryStr);

	// Now add query
	queryStr.append(queryRequest);
	Query query = QueryFactory.create(queryStr.toString());

	QueryExecution qexec = null;
	if (local) {
	    // Build dataset
	    DataSource dataSource = DatasetFactory.create();
	    dataSource.setDefaultModel(model);
	    // dataSource.addNamedModel("http://example/named-1", modelX) ;
	    // dataSource.addNamedModel("http://example/named-2", modelY) ;
	    qexec = QueryExecutionFactory.create(query, dataSource);
	} else {
	    qexec = QueryExecutionFactory.sparqlService(
		    "http://dbpedia.org/sparql", query);
	}

	ResultSet response = null;
	String res = null;
	try {
	    response = qexec.execSelect();

	    if (!response.hasNext()) {
		System.out.println("oohh, nix Ergebnisse");
	    }

	    while (response.hasNext()) {
		QuerySolution soln = response.nextSolution();
		RDFNode name = soln.get("?name");
		System.out.println(soln.toString());

		if (name != null) {
		    // if (name.toString().contains("de")) {
		    res = name.toString();
		    // }

		    // System.out.println("Hello to " + name.toString());
		} else
		    System.out.println("No Friends found!");
	    }
	} finally {
	    qexec.close();
	}

	return res;
    }

    private String extractFromResult(ResultSet result, String variable) {
	if (!result.hasNext()) {
	    System.out.println("oohh, nix Ergebnisse");
	} else {
	    RDFNode extractedResult = null;
	    while (result.hasNext()) {
		QuerySolution soln = result.nextSolution();
		extractedResult = soln.get(variable);
		System.out.println(soln.toString());

		// if (name != null) {
		// System.out.println("Hello to " + name.toString());
		// } else
		// System.out.println("No Friends found!");
	    }

	    return extractedResult.toString();
	}

	return "";

    }

    private void bindReasoner() {
	Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
	reasoner = reasoner.bindSchema(schema);
	inferredModel = ModelFactory.createInfModel(reasoner, images);
    }

    private static void appendNameSpacePrefix(String name, String nameSpace,
	    StringBuffer queryString) {
	queryString.append("PREFIX " + name + ": <" + nameSpace + "> ");
    }

    private void addAlignment() {

	// State that :individual is equivalentClass of foaf:Person
	Resource resource = schema.createResource(flickrNameSpace + "Image");
	Property prop = schema.createProperty(owlNameSpace + "equivalentClass");
	Resource obj = schema.createResource(dbpOntologyNameSpace + "Place");
	schema.add(resource, prop, obj);
    }

    private void populateDbPediaSchema() throws IOException {
	InputStream inFoafInstance = FileManager.get().open(
		"Ontologies/additionalFriendsSchema.owl");
	schema.read(inFoafInstance, dbPediaNameSpace);
	inFoafInstance.close();
    }

    private void populateNewFlickrSchema() throws IOException {
	InputStream inFoafInstance = FileManager.get().open(
		"Ontologies/flickrSchema.owl");
	schema.read(inFoafInstance, flickrNameSpace);
	inFoafInstance.close();
    }

    private void populateLocalFlickrImages() throws IOException {
	// friends = ModelFactory.createOntologyModel();
	InputStream inFoafInstance = FileManager.get().open(
		"Ontologies/flickrInstances.owl");
	images.read(inFoafInstance, flickrNameSpace);
	inFoafInstance.close();
    }

    // Utility methods
    private void renderOutput(ResultSet results, String query) {
	// Instead of a loop to deal with each row in the result set, the
	// application can call an operation of the ResultSetFormatter. This is
	// what the command line applications do.
	// Example: processing results to produce a simple text presentation:

	// ResultSetFormatter fmt = new ResultSetFormatter();
	// fmt.
	// fmt.printAll(System.out);
	// // or simply:
	// ResultSetFormatter.out(System.out, results, query);
    }

    private void saveToFile(Model model) {
	FileOutputStream outFoafInstance;
	try {
	    outFoafInstance = new FileOutputStream("Ontologies/schema.owl");
	    model.write(outFoafInstance);
	    model.write(outFoafInstance, "TURTLE");
	    outFoafInstance.close();
	} catch (Exception e) { // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
