package datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Node enthaelt Informationen zu einem Knoten im Baum.
 * Er beinhaltet den Namen des Knotens, die Kantenlaenge zum Elternknoten, den Elternknoten und die Knoten der Kinder.
 * 
 * @author Katrin Fischer
 *
 */
public class Node {
	
	private int id;
	private Map<Integer,Set<String>> label = new HashMap<Integer, Set<String>>();
	private String name;
	private String posSNP = "";
	private double length;
	private Node parent;
	private List<Node> children = new ArrayList<Node>();

	
	/**
	 * Konstruktor fuer einen Knoten
	 * @param nodename Name des Knotens
	 * @param branchlength Kantenlaenge zum Elternknoten
	 */
	public Node(String nodename, double branchlength) {
		name = nodename;
		length = branchlength;
	}
	


	/**
	 * Gibt einen String zurueck, mit dem Knoten und dessen Kindern im Newickformat
	 * @return String im Newickformat
	 */
	public String toNewickString() {
		//Stellt die Laenge in Englischer Schreibweise dar
		Locale.setDefault(Locale.ENGLISH);
		String childrenToString = "";
		// Kindknoten in Newickformat hinzufuegen, falls vorhanden
		if (!children.isEmpty()) {
			for (Node i : children) {
				//Kinder werden durch Komma getrennt
				if (childrenToString.equals("")) {
					childrenToString = i.toNewickString();
				} else {
					childrenToString = childrenToString + "," + i.toNewickString();
				}
			}
			// Daten des Knotens mit Kindern ausgeben
			return ("(" + childrenToString + ")" + name + posSNP + ":" + String.format( "%.3f", length ));
		} else {
			// Daten des Knotens ohne Kinder ausgeben
			return (name + ":" + String.format( "%.3f", length ));
		}
	}

	/**
	 * Gibt nur die Informationen des Knotens zur�ck
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return (name + ":" + String.format( "%.8f", length ) + "-" + id /*+ label.toString()*/);
	}

	public List<Node> getChildren() {
		return children;
	}

	public String getName() {
		return name;
	}

	public double getLength() {
		return length;
	}

	public Node getParent() {
		return parent;
	}
	
	/**
	 * Fuegt der Liste der Kindknoten einen Knoten hinzu
	 * @param child der neue Kindknoten
	 */
	public void addChild(Node child) {
		this.children.add(child);
	}

	/**
	 * Setzt den Elternknoten des Knoten
	 * @param parent der Elternknoten
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setPosSNP(String posSNP) {
		this.posSNP = posSNP;
	}

	public Map<Integer, Set<String>> getLabel() {
		return label;
	}
	
	public void setLabel(int pos, String snp) {
		if(label.containsKey(pos)) {
			label.get(pos).add(snp);
		}else {
			Set<String> set = new HashSet<String>();
			set.add(snp);
			label.put(pos, set);
		}
		if(parent != null) {
			parent.setLabel(pos, snp);
		}
	}

}
