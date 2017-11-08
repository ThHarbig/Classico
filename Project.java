package Project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datastructures.NewickTree;
import datastructures.Node;
import datastructures.SNPTable;

public class Project {
	private SNPTable snp;
	private NewickTree tree;
	private Map<Integer, HashMap<String, List<Node>>> claden = new HashMap<Integer, HashMap<String, List<Node>>>();
	private Map<Integer, HashMap<String, List<Node>>> smallCladen = new HashMap<Integer, HashMap<String, List<Node>>>();
	private Map<Integer, List<Integer>> splitKeys = new HashMap<Integer, List<Integer>>();

	public Project(String snpFile, String newickTreeFile) {
		snp = new SNPTable(snpFile);
		tree = new NewickTree(newickTreeFile);
	}

	public Project(SNPTable snp, NewickTree tree) {
		this.snp = snp;
		this.tree = tree;
	}

	public static void main(String[] args0) throws IOException {
		if (args0.length == 4) {
			Project comcla = new Project(args0[0], args0[1]);
			int key = 0;
			for (Integer pos : comcla.snp.getSNPs()) {
				key = pos;
				comcla.label(comcla.tree, pos);
				comcla.computeCladen(comcla.tree.getRoot(), pos, true);
				comcla.evaluateCladen(pos);
			}

			System.out.println(comcla.tree);
			for (Node n : comcla.tree.getNodeList()) {
				System.out.println(n.toString());
			}

			comcla.toFile(args0[2], comcla.claden);
			comcla.toFile(args0[3], comcla.smallCladen);
			comcla.splitKeys();
			
			HashMap<String, List<Node>> hm = comcla.smallCladen.get(key);
			for(String s : hm.keySet()) {
				for(Node nl : hm.get(s)) {
					for(Node nt : comcla.tree.getNodeList()) {
						if(nt.getId() == nl.getId()) {
							nt.setPosSNP("-" + key + "-" + s.substring(1, 2));
						}
					}
				}
			}
			System.out.println(comcla.tree.toString());

			System.out.println("ready");
		} else {
			System.err.println(
					"Geben Sie als ersten Dateipfad die SNP-Tabelle, als zweite eine Newick-Datei und als dritte eine leere Datei an");
		}
	}

	public void computeCladen(Node node, int key, boolean withoutN) {

		if (node.getLabel().containsKey(key)) {
			Set<String> base = node.getLabel().get(key);
			switch (base.size()) {
			case 0:
				System.out.println("Key enthalten aber keine Strings");
				break;
			case 1:
				setClade(key, node, base.toString(), claden);
				break;
			case 2:
				if (withoutN && node.getLabel().get(key).contains("N")) {
					base.remove("N");
					setClade(key, node, base.toString(), claden);
					break;
				}
			default:
				if (!node.getChildren().isEmpty()) {
					for (Node i : node.getChildren()) {
						// Durchsuche Kinder nach Claden
						computeCladen(i, key, withoutN);
					}
				} else {
					// Knoten hat keine Kinder aber zwei Basen (Darf nicht passieren)
					System.out.println("Blatt hat zwei Basen");
				}
				break;
			}
		} else {
			System.out.println("Key im Knoten nicht gefunden");
		}
	}

	public void evaluateCladen(int pos) {
		HashMap<String, List<Node>> l = claden.get(pos);
		List<Integer> size = new ArrayList<Integer>();
		for (String s : l.keySet()) {
			size.add(l.get(s).size());
		}
		Collections.sort(size);
		System.out.println(size.toString());
		int max = size.get(size.size() - 1);
		for (String s : l.keySet()) {
			List<Node> ln = l.get(s);
			if (ln.size() < max) {
				for (Node n : ln) {
					setClade(pos, n, s, smallCladen);
				}
			}
		}
	}

	public void undefNuc() {

	}

	public void splitKeys() {
		for (int key : smallCladen.keySet()) {
			for(String s : smallCladen.get(key).keySet()) {
				for(Node n : smallCladen.get(key).get(s)) {
					setInt(n.getId(), key);
				}
			}
		}
		System.out.println(splitKeys.toString());
	}

	public void label(NewickTree tree, Integer key) {
		for (String sample : snp.getSampleNames()) {
			String nuc = snp.getSnp(key, sample);
			String ref = snp.getReferenceSnp(key);
			if (nuc.equals(".")) {
				nuc = ref;
			}
			for (Node current : tree.getNodeList()) {
				if (current.getName().equals(sample)) {
					current.setLabel(key, nuc);
					break;
				}
			}
		}

	}

	public void setClade(int key, Node node, String label, Map<Integer, HashMap<String, List<Node>>> claden) {
		if (claden.containsKey(key)) {
			if (claden.get(key).containsKey(label)) {
				claden.get(key).get(label).add(node);
			} else {
				List<Node> split = new ArrayList<Node>();
				split.add(node);
				claden.get(key).put(label, split);
			}

		} else {
			HashMap<String, List<Node>> labeledNode = new HashMap<String, List<Node>>();
			List<Node> split = new ArrayList<Node>();
			split.add(node);
			labeledNode.put(label, split);
			claden.put(key, labeledNode);
		}
	}

	public void toFile(String filename, Map<Integer, HashMap<String, List<Node>>> claden) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i : claden.keySet()) {
				System.out.println(i + ":");
				bw.write(i + ":\n");
				for (String l : claden.get(i).keySet()) {
					System.out.println(l + ":");
					bw.write(l + ":\n");
					for (Node n : claden.get(i).get(l)) {
						System.out.println(
								n.getId() + "-" + n.getName() + "-" + n.getLabel().get(i) + ":" + n.toNewickString());
						bw.write(n.getId() + "-" + n.getName() + "-" + n.getLabel().get(i) + ":" + n.toNewickString()
								+ "\n");
					}
				}
				System.out.println();
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setInt(int id, int pos) {
		if(splitKeys.containsKey(id)) {
			splitKeys.get(id).add(pos);
		}else {
			List<Integer> set = new ArrayList<Integer>();
			set.add(pos);
			splitKeys.put(id, set);
		}
	}
	
	public void showCladeAtPosition(int pos) {
		
	}

}