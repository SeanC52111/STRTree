import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.*;
import Tool.Hasher;

//class of return object of vo
class VOreturn{
	public String hash="";
	public Rect MBR=null;
}

class NNEntry{
	STRNode node;
	double dist;
	public NNEntry(STRNode node,Double dist) {
		this.node = node;
		this.dist = dist;
	}
}
public class STRTree {
	//nodelist stores the STRNode objects from the input file
	//file input: double double double double
	private ArrayList<STRNode> nodelist;
	private ArrayList<STRNode> rootnodes;
	public STRNode root;
	private int totalnodenum = 0;
	public STRTree(String filename,int nodec) {
		try {
			nodelist = new ArrayList<STRNode>();
			FileInputStream fis=new FileInputStream(filename);
	        InputStreamReader isr=new InputStreamReader(fis);
	        BufferedReader br = new BufferedReader(isr);
	        String line="";
	        String[] arrs=null;
	        while ((line=br.readLine())!=null) {
	        	//System.out.println(line);
	            arrs=line.split(" ");
	            //create a new Rect object from the 4 double numbers
	            Rect r = new Rect(new Point(Double.valueOf(arrs[1]),Double.valueOf(arrs[2])));
	            nodelist.add(new STRNode(r,true,null,""));
	            //System.out.println("successfully");
	        }
	        
	        br.close();
	        isr.close();
	        fis.close();
		}
		catch(Exception e) {
			
		}
		//use the nodelist to create a strtree
		//input: nodec,nodelist
		//output: root nodes which size is less than nodec
		rootnodes = createTree(nodec);
		System.out.println(totalnodenum);
		//combine the root nodes list to one node:root
		root = mergeRoot(rootnodes);
	}
	
	//input: rootnodes which size smaller than nodec
	//output: single root strnode object
	private STRNode mergeRoot(ArrayList<STRNode> rootnodes) {
		STRNode root = null;
		Rect mbr = getMBR(rootnodes);
		String hash = "";
		for(STRNode r : rootnodes) {
			hash += r.MBR.toString()+r.hashvalue;
		}
		//System.out.println(hash);
		Hasher hasher = new Hasher();
		hash = hasher.stringSHA(hash);
		root = new STRNode(mbr,false,rootnodes,hash);
		return root;
	}
	
	
	//use nodelist to construct a strtree
	private ArrayList<STRNode> createTree(int nodec){
		//x axid comparator for sorting
				Comparator xcomp = new Comparator<STRNode>() {
					public int compare(STRNode r1,STRNode r2) {
						if(r1.MBR.getCenter().x < r2.MBR.getCenter().x)
							return -1;
						else if(r1.MBR.getCenter().x > r2.MBR.getCenter().x)
							return 1;
						else
							return 0;
					}
				};
				//y axid comparator for sorting
				Comparator ycomp = new Comparator<STRNode>() {
					public int compare(STRNode r1,STRNode r2) {
						if(r1.MBR.getCenter().y < r2.MBR.getCenter().y)
							return -1;
						else if(r1.MBR.getCenter().y > r2.MBR.getCenter().y)
							return 1;
						else
							return 0;
					}
				};
				//temporary variable of current layer nodes
				ArrayList<STRNode> current = nodelist;
				totalnodenum += current.size();
				//build the STRTree in a bottom-up manner
				do {
					ArrayList<STRNode> cur = new ArrayList<STRNode>();
					int leafnodecount = (int) Math.ceil(current.size()/(double)nodec);
					int xsliceCapacity = (int) Math.ceil(Math.sqrt(leafnodecount));
					ArrayList[] slices = stripPartition(current,xsliceCapacity*nodec,xcomp);
					for(int j=0;j<slices.length;j++) {
						ArrayList<STRNode> temp = slices[j];
				    	ArrayList[] yslices = stripPartition(temp,nodec,ycomp);
				    	
				    	//construct higher level nodes
				    	for(ArrayList<STRNode> arr : yslices) {
				    		if(arr.get(0).isleaf==true) {
				    			String hash = "";
				    			for(STRNode a:arr) {
				    				hash += a.MBR.toString();
				    			}
				    			hash = new Hasher().stringSHA(hash);
				    			STRNode strnode = new STRNode(getMBR(arr),false,arr,hash);
					    		cur.add(strnode);
				    		}
				    		else {
				    			String hash = "";
				    			for(STRNode a:arr) {
				    				hash += a.MBR.toString()+a.hashvalue;
				    			}
				    			hash = new Hasher().stringSHA(hash);
				    			STRNode strnode = new STRNode(getMBR(arr),false,arr,hash);
					    		cur.add(strnode);
				    		}
				    		
				    	}
					}
					//update the input of nodes on current layer
					current = cur;
					totalnodenum += current.size();
				}
				while(current.size()>nodec);
				return current;
	}
	
	//BFStraverse function
	public void BFStraverse() {
		Queue<STRNode> st = new LinkedList<STRNode>();
		st.offer(root);
		while(!st.isEmpty()) {
			STRNode n = st.poll();
			if(n.isleaf) {
				System.out.println("data point: "+n.MBR+" data hash: ");
			}
			else {
				System.out.println("internal node:"+n.MBR+" internal hash: "+n.hashvalue);
				for(int i = 0;i<n.child.size();i++) {
					st.offer(n.child.get(i));
				}
			}
		}
	}
	
	public void DFStraverse() {
		Stack<STRNode> st = new Stack<STRNode>();
		st.push(root);
		while(!st.isEmpty()) {
			STRNode n = st.pop();
			if(n.isleaf) {
				System.out.println("data point: "+n.MBR+" data hash: "+n.hashvalue);
			}
			else {
				System.out.println("internal node:"+n.MBR+" internal hash: "+n.hashvalue);
				for(int i = 0;i<n.child.size();i++) {
					st.push(n.child.get(i));
				}
			}
		}
	}
	
	//input: STRNode, query range, empty VO list
	//process in recurrent format
	public void secureRangeQuery(STRNode n,Rect query,LinkedList<String> VO)
	{
		
		if(query.isIntersects(n.MBR)) {
			VO.add("[");
			for(int i =0;i<n.child.size();i++) {
				if(query.isIntersects(n.child.get(i).MBR) && !n.child.get(i).isleaf) {
					secureRangeQuery(n.child.get(i),query,VO);
				}
				else {
					if(n.child.get(i).isleaf) {
						VO.add(n.child.get(i).MBR.toString());
						if(n.child.get(i).MBR.isIntersects(query)) {
							System.out.println("result: "+n.child.get(i).MBR.toString());
						}
					}
					else
						VO.add("("+n.child.get(i).MBR.toString()+" "+n.child.get(i).hashvalue+")");
					}
				}
			VO.add("]");
		}
		else {
			VO.add("{"+n.MBR.toString()+" "+n.hashvalue+"}");
		}
	}
	
	public void securekNN(int k,Point query,LinkedList<String> result,LinkedList VO) {
		STRNode n = this.root;
		VO.add(new NNEntry(n,0.0));
		int minindex = 0;
		int counter = 0;
		double knearest = 0.0;
		while(counter < k  ) {
			double min = 9999999;
			//first find the nearest distance and index
			NNEntry t = null;
			for(int i=0;i<VO.size();i++) {
				Object item = VO.get(i);
				if(!(item instanceof String)) {
					t = (NNEntry)item;
					double mindis = t.dist;
					if(mindis<min) {
						min = mindis;
						minindex = i;
					}
				}
			}
			if(t == null)
				break;
			//t is the closest entry
			t = (NNEntry)VO.get(minindex);
			if(!t.node.isleaf) {
				VO.remove(minindex);
				n = (STRNode)t.node;
				VO.add(minindex, "[");
				//expand the children of t and add them to the linkedlist of VO
				for(int i =0;i<n.child.size();i++) {
					STRNode tmp = n.child.get(i);
					NNEntry nn = new NNEntry(tmp,getMinimumDist(query,tmp.MBR));
					VO.add(minindex+i+1, nn);
				}
				VO.add(minindex+n.child.size()+1,"]");
			}
			else {
				VO.remove(minindex);
				VO.add(minindex, "#"+counter);
				counter++;
				t.dist = 9999999.0;
				result.add(t.node.MBR.toString());
			}
		}
		
		for(int it=0;it<VO.size();it++) {
			Object item = VO.get(it);
			if(item instanceof NNEntry) {
				VO.remove(it);
				NNEntry entry = (NNEntry)item;
				if(!entry.node.isleaf) {
					STRNode tmpnode = entry.node;
					VO.add(it,"("+tmpnode.MBR.toString()+" "+tmpnode.hashvalue+")");//non expanded internal node
				}
				else {
					VO.add(it,entry.node.MBR.toString());
				}
			}
		}
		
	}
	
	private double getMinimumDist(Point q,Rect r) {
		double ret = 0.0;
		if(q.x < r.x1)
			ret += Math.pow(r.x1-q.x, 2);
		else if(q.x > r.x2)
			ret += Math.pow(q.x-r.x2, 2);
		if(q.y < r.y1)
			ret += Math.pow(r.y1-q.y, 2);
		else if(q.y > r.y2)
			ret += Math.pow(q.y-r.y2, 2);
		return ret;
	}
	//input: VO list
	//output: the root VOreturn object including the corresponding MBR and hash value
	public VOreturn RootHash(LinkedList<String> VO) {
		String str = "";
		Rect MBR = null;
		VOreturn ret = new VOreturn();
		while(!VO.isEmpty()) {
			String f = VO.poll();
			if(f.charAt(0)>='0' && f.charAt(0)<='9') {
				str = str + f;
				Rect MBR_c = StringtoMBR(f);
				MBR = enLargeMBR(MBR_c,MBR);
			}
			if(f.charAt(0)=='(') {
				VOreturn n = transform(f);
				MBR  = enLargeMBR(n.MBR,MBR);
				str = str + MBRtoString(n.MBR)+ n.hash;
				//System.out.println(str);
			}
			if(f == "[") {
				ret = RootHash(VO);
				MBR = enLargeMBR(ret.MBR,MBR);
				str = str + MBRtoString(ret.MBR)+ret.hash;
				//System.out.println(str);
			}
			if(f == "]") {
				ret.hash = new Hasher().stringSHA(str);
				ret.MBR=MBR;
				return ret;
			}
			if(f.charAt(0)=='{')
			{
				String ms = f.substring(1, f.length()-1);
				String []result = ms.split(" ");
				
				Rect mmbr = new Rect(Double.valueOf(result[0]),Double.valueOf(result[1]),Double.valueOf(result[2]),Double.valueOf(result[3]));
				ret.MBR = mmbr;
				ret.hash = mmbr.toString()+result[4];
				return ret;
				
			}
			
		}
		ret.hash =str;
		ret.MBR=MBR;
		return ret;
	}
	
	
	private Rect enLargeMBR(Rect MBR_c,Rect MBR){
		if(MBR==null) {
			MBR = new Rect(0,0,0,0);
			MBR.x1 = MBR_c.x1;
			MBR.x2 = MBR_c.x2;
			MBR.y1 = MBR_c.y1;
			MBR.y2 = MBR_c.y2;
		}
		else {
			if(MBR_c.x1<MBR.x1)
				MBR.x1=MBR_c.x1;
			if(MBR_c.x2>MBR.x2)
				MBR.x2=MBR_c.x2;
			if(MBR_c.y1<MBR.y1)
				MBR.y1=MBR_c.y1;
			if(MBR_c.y2>MBR.y2)
				MBR.y2=MBR_c.y2;
		}
		
		return MBR;
	}
	
	private VOreturn transform(String str) {
		VOreturn ret = new VOreturn();
		str = str.substring(1,str.length()-1);
		String [] result = str.split(" ");
		ret.hash = result[4];
		Rect MBR = new Rect(Double.valueOf(result[0]),Double.valueOf(result[1]),Double.valueOf(result[2]),Double.valueOf(result[3]));
		ret.MBR = MBR;
		return ret;
	}
	
	private Rect StringtoMBR(String str) {
		String [] s = str.split(" ");
		Rect mbr = new Rect(Double.valueOf(s[0]),Double.valueOf(s[1]),Double.valueOf(s[2]),Double.valueOf(s[3]));
		return mbr;
	}
	
	//transform the double array MBR to String version
	private String MBRtoString(Rect MBR) {
		String str= "";
		str = str + String.valueOf(MBR.x1)+" ";
		str = str + String.valueOf(MBR.x2)+" ";
		str = str + String.valueOf(MBR.y1)+" ";
		str = str + String.valueOf(MBR.y2);
		return str;
	}
	private Rect getMBR(ArrayList<STRNode> array) {
		double xmin = 100000000;
		double xmax = 0;
		double ymin = 100000000;
		double ymax = 0;
		for(STRNode n : array) {
			if(n.MBR.x1<xmin)
				xmin = n.MBR.x1;
			if(n.MBR.x2>xmax)
				xmax = n.MBR.x2;
			if(n.MBR.y1<ymin)
				ymin = n.MBR.y1;
			if(n.MBR.y2>ymax)
				ymax = n.MBR.y2;
		}
		return new Rect(xmin,xmax,ymin,ymax);
	}
	private ArrayList[] stripPartition(ArrayList<STRNode> plist,int sliceCapacity,Comparator comp) {
		int datasize = plist.size();
		int sliceCount = (int) Math.ceil(datasize / (double) sliceCapacity);
		plist.sort(comp);
		ArrayList[] slices = new ArrayList[sliceCount];
		Iterator i = plist.iterator();
	    for (int j = 0; j < sliceCount; j++) {
	    	slices[j] = new ArrayList();
	    	int boundablesAddedToSlice = 0;
	    	while (i.hasNext() && boundablesAddedToSlice < sliceCapacity) {
	    		STRNode t = (STRNode)i.next();
	    		slices[j].add(t);
	    		boundablesAddedToSlice++;
	    	}
	    }
	    return slices;
	}
}
