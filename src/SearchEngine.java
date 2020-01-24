import java.util.HashMap;
import java.util.ArrayList;

public class SearchEngine {
	public HashMap<String, ArrayList<String> > wordIndex;   // this will contain a set of pairs (String, LinkedList of Strings)	
	public MyWebGraph internet;
	public XmlParser parser;

	public SearchEngine(String filename) throws Exception{
		this.wordIndex = new HashMap<String, ArrayList<String>>();
		this.internet = new MyWebGraph();
		this.parser = new XmlParser(filename);
	}
	
	/* 
	 * This does a graph traversal of the web, starting at the given url.
	 * For each new page seen, it updates the wordIndex, the web graph,
	 * and the set of visited vertices.
	 * 
	 * 	This method will fit in about 30-50 lines (or less)
	 */
	public void crawlAndIndex(String url) throws Exception {
		// TODO : Add code here
		internet.addVertex(url);
		internet.setVisited(url, true);
		ArrayList<String> links = parser.getLinks(url);
		ArrayList<String> words = parser.getContent(url);
		for(String word : words){
			addUrl(word.toLowerCase(), url);
		}
		for (String link : links) {
			if(!internet.getVisited(link)){
				crawlAndIndex(link);
			}
			internet.addEdge(url,link);
		}

	}

	/*
	 * Helper method to add url to to wordIndex
	 */
	public void addUrl(String word, String url){
		ArrayList<String> urls = wordIndex.get(word);
		if(urls==null){
			urls = new ArrayList<String>();
			urls.add(url);
			wordIndex.put(word, urls);
		}
		else{
			if(!urls.contains(url)){
				urls.add(url);
				wordIndex.replace(word,urls);
			}
		}
	}
	
	/* 
	 * This computes the pageRanks for every vertex in the web graph.
	 * It will only be called after the graph has been constructed using
	 * crawlAndIndex(). 
	 * To implement this method, refer to the algorithm described in the 
	 * assignment pdf. 
	 * 
	 * This method will probably fit in about 30 lines.
	 */
	public void assignPageRanks(double epsilon) {
		// TODO : Add code here
		ArrayList<String> vertices = internet.getVertices();
		for(String vertex : vertices){
			internet.setPageRank(vertex,1.0);
		}
        ArrayList<Double> newRanks=computeRanks(vertices);
		while(true) {
			boolean hasDiffThroughout = true;
			for (int i = 0; i < vertices.size(); i++) {
				double difference = Math.abs(internet.getPageRank(vertices.get(i)) - newRanks.get(i));
				if (difference >= epsilon) {
					hasDiffThroughout = false;
					for(int j=0;j<newRanks.size();j++){
						internet.setPageRank(vertices.get(j),newRanks.get(j));
					}
                    newRanks=computeRanks(vertices);
					break;
				}
			}
			if(hasDiffThroughout){
				break;
			}
		}
		for(int j=0;j<newRanks.size();j++){
			internet.setPageRank(vertices.get(j),newRanks.get(j));
		}
	}

	/*
	 * The method takes as input an ArrayList<String> representing the urls in the web graph 
	 * and returns an ArrayList<double> representing the newly computed ranks for those urls. 
	 * Note that the double in the output list is matched to the url in the input list using 
	 * their position in the list.
	 */
	public ArrayList<Double> computeRanks(ArrayList<String> vertices) {
		// TODO : Add code here
		ArrayList<Double> pageRanks = new ArrayList<Double>();
		for(int i=0;i<vertices.size();i++){
			ArrayList<String> inVertices = internet.getEdgesInto(vertices.get(i));
			double tmpRank=0.0;
			for (String inVertex : inVertices) {
				tmpRank += ((internet.getPageRank(inVertex)) / internet.getOutDegree(inVertex));
			}
			double rank=(0.5)+(0.5*tmpRank);
			pageRanks.add(i,rank);
		}
		return pageRanks;
	}

	
	/* Returns a list of urls containing the query, ordered by rank
	 * Returns an empty list if no web site contains the query.
	 * 
	 * This method should take about 25 lines of code.
	 */
	public ArrayList<String> getResults(String query) {
		// TODO: Add code here
		ArrayList<String> urls = wordIndex.get(query.toLowerCase());
		HashMap<String, Double> mappedRanks = new HashMap<>();
		if(urls!=null){
			for(String url : urls){
				mappedRanks.put(url,internet.getPageRank(url));
			}
		}
		return Sorting.fastSort(mappedRanks);
	}
}
