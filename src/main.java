import java.util.*;
import Tool.Hasher;

import java.io.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


class DrawFrame extends JFrame{
	public STRNode l;
    public DrawFrame(STRNode root){
        super();
        l = root;
        initialize();//调用初始化方法
    }
    //初始化方法
    public void initialize(){
        this.setSize(700, 700);//设置窗体大小
        //设置窗体的关闭方式
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(new CanvasPanel(l));//设置窗体面板为绘图面板对象
        this.setTitle("draw mbrs");//设置窗体标题
    }
    
    //创建内部类
    class CanvasPanel extends JPanel{
    	STRNode l;
    	public CanvasPanel(STRNode root) {
    		l = root;
    	}
        public void paint(Graphics g){
            super.paint(g);
            Graphics2D g2=(Graphics2D)g;//强制类型转换位Graphics2D类型
            //Graphics2D是推荐使用的绘图类，但是程序设计中提供的绘图对象大多是
            //Graphics类的实例，这时应该使用强制类型转换将其转换为Graphics
            ArrayList<Shape> shapes = new ArrayList<Shape>();
            //创建矩形对象
            Queue<STRNode> st = new LinkedList<STRNode>();
    		st.offer(l);
    		while(!st.isEmpty()) {
    			STRNode n = st.poll();
    			if(n.isleaf) {
    				//System.out.println("data point: "+n.MBR+" data hash: "+n.hashvalue);
    				shapes.add(new Rectangle2D.Double(n.MBR.x1,n.MBR.y1,n.MBR.x2-n.MBR.x1+3,n.MBR.y2-n.MBR.y1+3));
    			}
    			else {
    				//System.out.println("internal node:"+n.MBR+" internal hash: "+n.hashvalue);
    				shapes.add(new Rectangle2D.Double(n.MBR.x1,n.MBR.y1,n.MBR.x2-n.MBR.x1,n.MBR.y2-n.MBR.y1));
    				for(int i = 0;i<n.child.size();i++) {
    					st.offer(n.child.get(i));
    				}
    			}
    		}
            for(Shape shape:shapes){//遍历图型数组
                if(shape.getBounds2D().getWidth()==3 && shape.getBounds2D().getHeight()==3) {
                	g2.setColor(Color.RED);
                	g2.draw(shape);
                }
                else {
                	g2.setColor(Color.BLUE);
                	g2.draw(shape);
                }
            }
        }
    }
}

public class main {
	public static void main(String [] args) {
		ArrayList<STRNode> nodelist = new ArrayList<STRNode>();
		/*
		Random random = new Random();
		for(int i = 0;i<15;i++) {
			Rect r = new Rect(new Point(random.nextInt(100),random.nextInt(100)));
			STRNode n = new STRNode(r,true,null,"");
			nodelist.add(n);
			System.out.println(n.MBR);
		}
		*/
		STRTree strtree = new STRTree("datapoints.txt",3);
		strtree.DFStraverse();
		//strtree.DFStraverse();
		DrawFrame df=new DrawFrame(strtree.root);
        df.setVisible(true);
        
        LinkedList<String> VO = new LinkedList<String>();
        /*
        strtree.secureRangeQuery(strtree.root, new Rect(1,7,-1,5), VO);
        System.out.println("VOs:");
        for(String s:VO) {
        	System.out.println(s);
        }
        VOreturn voreturn = strtree.RootHash(VO);
        System.out.println(voreturn.hash);
        System.out.println(strtree.root.MBR.toString()+strtree.root.hashvalue);
        System.out.println(voreturn.hash.equals(strtree.root.MBR.toString()+strtree.root.hashvalue));
        
		LinkedList<String> VO = new LinkedList<String>();
		VO.add("{11.0 14.0 0.0 13.0 82770e371050978d9b77afbd30eb5e3aa949d9af134e0bd8d75d70b2d6a6e6e3}");
		VOreturn voreturn = RootHash(VO);
        System.out.println(voreturn.hash);
        */
        LinkedList<String> result = new LinkedList<String>();
        strtree.securekNN(3, new Point(3,15), result, VO);
        System.out.println("results");
        for(String r:result) {
        	System.out.println(r);
        }
        System.out.println("VOs:");
        for(String s:VO) {
        	System.out.println(s);
        }
        String verifyhash = strtree.root.MBR.toString()+strtree.root.hashvalue;
        System.out.println(Verify(VO,result,verifyhash));
        /*
        VOreturn voreturn = strtree.RootHash(VO);
        System.out.println(voreturn.hash);
        System.out.println(strtree.root.MBR.toString()+strtree.root.hashvalue);
        System.out.println(voreturn.hash.equals(strtree.root.MBR.toString()+strtree.root.hashvalue));
        */
        
        /*
        String s = "0-r-00000";
        String[] split = s.split("-");
        for(String str: split)
        	System.out.println(str);
        */
	}
	
	public static boolean Verify(LinkedList<String> VO,LinkedList<String> RS,String verifyhash) {
		for(int i=0;i<VO.size();i++) {
			if(VO.get(i).charAt(0)=='#') {
				int index = Integer.valueOf(VO.get(i).substring(1));
				String tmp = RS.get(index);
				VO.remove(i);
				VO.add(i, tmp);
			}
		}
		
		VOreturn voreturn = RootHash(VO);
        System.out.println(voreturn.hash.equals(verifyhash));
		return voreturn.hash.equals(verifyhash);
	}
	public static VOreturn RootHash(LinkedList<String> VO) {
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
	
	private static Rect enLargeMBR(Rect MBR_c,Rect MBR){
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
	
	private static VOreturn transform(String str) {
		VOreturn ret = new VOreturn();
		str = str.substring(1,str.length()-1);
		String [] result = str.split(" ");
		ret.hash = result[4];
		Rect MBR = new Rect(Double.valueOf(result[0]),Double.valueOf(result[1]),Double.valueOf(result[2]),Double.valueOf(result[3]));
		ret.MBR = MBR;
		return ret;
	}
	
	private static Rect StringtoMBR(String str) {
		String [] s = str.split(" ");
		Rect mbr = new Rect(Double.valueOf(s[0]),Double.valueOf(s[1]),Double.valueOf(s[2]),Double.valueOf(s[3]));
		return mbr;
	}
	
	//transform the double array MBR to String version
	private static String MBRtoString(Rect MBR) {
		String str= "";
		str = str + String.valueOf(MBR.x1)+" ";
		str = str + String.valueOf(MBR.x2)+" ";
		str = str + String.valueOf(MBR.y1)+" ";
		str = str + String.valueOf(MBR.y2);
		return str;
	}
}
