package opencvTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class LineFinder {
	public static double PI = 3.1415926;
	public static int GRAY_THRESH = 150;
	public static int HOUGH_VOTE = 100;
	// 直线对应的点参数向量
    private Mat lines;
	//步长
    private double deltaRho;	
    private double deltaTheta;
    // 判断是直线的最小投票数
    private int minVote;
    // 判断是直线的最小长度
    private double minLength;
    // 同一条直线上点之间的距离容忍度
    private  double maxGap;
    
    //初始化
	public LineFinder() {
		super();
		this.lines = new Mat();
		this.deltaRho = 1;
		this.deltaTheta = PI / 180;
		this.minVote = 10;
		this.minLength = 0.;
		this.maxGap = 0.;
	}
	// 设置步长
	void setAccResolution(double dRho, double dTheta) {
		deltaRho = dRho;
		deltaTheta = dTheta;
	}
	// 设置最小投票数
	void setMinVote(int minv) {
		minVote = minv;
	}
	// 设置最小线段长度和线段间距容忍度
	void setLineLengthAndGap(double length, double gap) {
		minLength = length;
		maxGap = gap;
	}
	//寻找线段
	 Mat findLines(Mat binary) {
		lines.release();
		Imgproc.HoughLinesP(binary, lines, deltaRho, deltaTheta, minVote, minLength, maxGap);
		return lines;
	}
	 
	// 1号 画线段, 直接画所有找到的线段
	void drawDetectedLines(Mat image, Scalar color ) {
		if(color == null) color = new Scalar(0, 255, 0); 
		for(int i = 0; i < lines.rows(); i++)
		{
			double[] vec = lines.get(i, 0);
            Point pt1 = new Point(vec[0],vec[1]);
            Point pt2 = new Point(vec[2],vec[3]);
            //设置输出直线宽度
            if (Math.abs(pt1.y - pt2.y)<2)
            {
            	Imgproc.line(image, pt1, pt2, color,2);
            }
		}
	}
	
	//找出水平线
	HashMap<Integer, Integer>  findHLines() {
		HashMap<Integer, Integer> ymap = new HashMap<Integer, Integer>() ; // 水平直线Y坐标出现的次数
		//int j = lines.rows();
		for(int i = 0; i < lines.rows(); i++)
		{
			double[] vec = lines.get(i, 0);
            Point pt1 = new Point(vec[0],vec[1]);
            Point pt2 = new Point(vec[2],vec[3]);
            // 查找水平直线
            if (Math.abs(pt1.y - pt2.y)<2 && Math.abs(pt1.x - pt2.x) > 5)
            {
    
            	if(!ymap.containsKey(pt1.y))
            	{ 
            		ymap.put((int) pt1.y, 1);
            		//ymap.insert(make_pair(pt1.y, 1));
            	}else
            	{
            		ymap.put((int) pt1.y, ymap.get(pt1.y) + 1);
            		//ymap.get(pt1.y) = ymap.get(pt1.y) + 1;
            	}
            }
		}
		return ymap;
	}
	//找出垂直线
	HashMap<Integer, Integer>  findVLines() {
		HashMap<Integer, Integer> xmap = new HashMap<Integer, Integer>() ; // 水平直线Y坐标出现的次数
		for(int i = 0; i < lines.rows(); i++)
		{
			double[] vec = lines.get(i, 0);
			
            Point pt1 = new Point(vec[0],vec[1]);
            Point pt2 = new Point(vec[2],vec[3]);
            // 查找垂直线
            if (Math.abs(pt1.y - pt2.y) > 10 && Math.abs(pt1.x - pt2.x) < 2)
            {            	
            	if(!xmap.containsKey(pt1.y))
            	{ 
            		xmap.put((int) pt1.y, 1);
            		//xmap.insert(make_pair(pt1.y, 1));
            	}else
            	{
            		int key = xmap.get(pt1.y);
            		xmap.put((int) pt1.y, xmap.get(pt1.y) + 1);
            		//xmap.get(pt1.y) = xmap.get(pt1.y) + 1;
            	}
            }
		}
		return xmap;
	}
	
	//画出水平线
	void drawHLines(Mat image, HashMap<Integer, Integer> map, int thred, Scalar color) {
		//std::HashMap<Integer, Integer>::const_iterator it = map.begin();
		if(color == null)	color = new Scalar(255, 0, 0);
		for(Map.Entry<Integer, Integer> entry : map.entrySet())
		{
			if(entry.getValue() > thred)
			{
				Point pt1 = new Point(0,entry.getKey());
				Point pt2 = new Point(image.cols(),entry.getKey());
				Imgproc.line(image, pt1, pt2, color, 1);
			}
		} 
	}
	//画出垂直线
	void drawVLines(Mat image, Map<Integer, Integer> map, int thred, Scalar color) {
		if(color == null)	color = new Scalar(255, 0, 0);
		for(Map.Entry<Integer, Integer> entry : map.entrySet())
		{
			if(entry.getValue() > thred)
			{
				Point pt1 = new Point(entry.getKey(),0);
				Point pt2 = new Point(entry.getKey(),image.rows());
				Imgproc.line(image, pt1, pt2, color,1);
			}
		} 
	}
	
	
	// 2号画线段， 找到所有水平线段的纵坐标和垂直线段的横坐标，在该纵坐标画水平线，在横坐标处画垂直线
	void drawDetectedLines1(Mat image, Scalar color) {
		if(color == null)	color = new Scalar(255, 0, 0);
		HashMap<Integer, Integer> ymap = findHLines();
		HashMap<Integer, Integer> xmap = findVLines();
		drawHLines(image, ymap, 5, color);
		drawVLines(image, xmap, 1, color);
	}
	
	// 合并相邻的水平直线
	HashMap<Integer, Integer> mergeHLines(Map<Integer, Integer> ymap) {
		// 合并相邻的水平直线
		//HashMap<int, int>::const_iterator it = ymap.begin();
		Boolean IsOne = true;
		HashMap<Integer, Integer> ymergemap = new HashMap<Integer, Integer>(); // 相邻直线最上侧线，相邻直线最下测线
		int y1 = 0;
		int y2 = 0;
		if(ymap.size() == 1) 
		{
			y1 = y2 = ymap.entrySet().iterator().next().getKey();		 
		}
		for(Map.Entry<Integer, Integer> entry : ymap.entrySet())
		{
			if(!IsOne)
			{
				if (y2 == entry.getKey() - 1) {
					y2 = entry.getKey();
				}
				else {
					if (Math.abs(y2 - y1)>3 && Math.abs(y2 - y1)<15) {
						ymergemap.put(y1, y2);
						//ymergemap.insert(std::make_pair(y1, y2));
					}
					y2 = y1 = entry.getKey();
				}
			}
			else
			{
				IsOne = false;
			}
			
		}
		return ymergemap;
	}
	
	// 1号 合并并画出相邻的垂直线
	void drawMergeHLines(Mat image, Map<Integer, Integer> ymergemap, Scalar color) {
		if(color == null) color = new  Scalar(0, 255, 0);
		for(Map.Entry<Integer, Integer> entry : ymergemap.entrySet())
		{
			//std::cout << (*it).first << "," << (*it).second << std::endl;
			int y1 = (entry.getKey() - 1);
			int y2 = (entry.getValue() + 1);
			//uchar* data1 = image.ptr(y1);
			//uchar* data2 = image.ptr(y2);
			for (int col = 0; col < image.cols(); col++) {
				// 检测新的边界
				// 上下都为空白则清除
				double[] data1 = image.get(y1, col);
				double[] data2 = image.get(y2, col);
				if (data1[0] >100 && data2[0] >100) {
					Point pt1 = new Point(col, entry.getKey());
					Point pt2 = new Point(col, entry.getValue());
					Imgproc.line(image, pt1, pt2, color, 1);
				}
				else {
					//col += 5;
				}	
			}
			//std::cout << std::endl;
		}
	}
	//投票
	int voteValue(Map<Integer, Integer> map) {
		int votecount = map.entrySet().iterator().next().getValue();
		int ret = map.entrySet().iterator().next().getKey();
		for(Map.Entry<Integer, Integer> entry : map.entrySet())
		{
			if (entry.getValue() > votecount) {
				votecount = entry.getValue();
				ret = entry.getKey();
			}
		}
		return ret;
	}
	
	//2号合并并画出相邻的水平线
	void drawMergeHLines2(Mat image, Map<Integer, Integer> ymergemap, Scalar color)
	{
		if(color == null) color = new Scalar(255, 255, 255);	
		Iterator<Entry<Integer, Integer>> it = ymergemap.entrySet().iterator();  
		while(it.hasNext())
		{
			 int y1 = 0;
			 int y2 = 0;
			 Entry<Integer, Integer> itEntry = it.next();
			//std::cout << (*it).first << "," << (*it).second << std::endl;
			//int y1 = ((*it).first);
			//int y2 = ((*it).second);
			//uchar* data1 = image.ptr(y1);
			//uchar* data2 = image.ptr(y2);
			//将宽带分成100份计算每一份的上下测边界
			int splitcount = 100;
			int w = image.cols() / splitcount;
			Vector<Integer> lineY1s = new Vector<Integer>();
			Vector<Integer> lineY2s = new Vector<Integer>();
			for (int i = 0; i < splitcount; i++) {
				//int y1 = (*it).first;
				y1 = itEntry.getKey();
				//int y2 = (*it).second;
				y2 = itEntry.getValue();
				Map<Integer, Integer> y1map = new HashMap<Integer, Integer>();
				Map<Integer, Integer> y2map = new HashMap<Integer, Integer>();
				//, y2map; // 每小段的高度
				for (int col = w*i; col < w*(i + 1); col++)
				{
					
					Boolean foundBottom = false;
					for (int y = y2; y >= y1; y--) {
						// 查找下边界坐标,从下往上找第一个黑点
						if (foundBottom == false) {
							//image.ptr(y)[col * 3] < 100
							if (image.get(y, col)[0] < 100) {
								y2 = y;
								foundBottom = true;
							}
						}
						// 找到下边界，找第一个白点
						else {
							//image.get(y, 0)
							//image.ptr(y)[col * 3] > 100
							if(image.get(y, col)[0]>100) {
//							if (image.get(y, 0)[col * 3] > 100) {
								y1 = y;
								break;
							}
						}
					}
					//c++ y1map[y1] != NULL
					if (y2map.containsKey(y1)) {
						//c ++ y1map[y1] = y1map[y1] + 1;
						y1map.put(y1,y1map.get(y1) + 1);
					}
					else {
						// c++ y1map[y1] = 1;
						y1map.put(y1, 1);
					}
					// c++y2map[y2] != NULL
					
					//y2map.get(y2)!= null
					if (y2map.containsKey(y2)) {
					//c++	y2map[y2] = y2map[y2] + 1;
						y2map.put(y2,y2map.get(y2) + 1);
					}
					else {
						//c ++ y2map[y2] = 1;
						y2map.put(y2, 1);
					}
					// 查找上边界坐标
				}
				// 查找平均上限坐标和平均下限坐标
				
				
				//c++ lineY1s.push_back(voteValue(y1map));
				lineY1s.add(voteValue(y1map));
				//c++ lineY2s.push_back(voteValue(y2map));
				lineY2s.add(voteValue(y2map));
			}
			// 平滑
			for (int i = 1; i < lineY1s.size()-1; i++) {
				if (Math.abs(lineY1s.get(i - 1) - lineY1s.get(i + 1)) < Math.abs(lineY1s.get(i - 1) - lineY1s.get(i))){
					lineY1s.set(i, (lineY1s.get(i - 1)+ lineY1s.get(i + 1)) / 2);  
				}
				if (Math.abs(lineY2s.get(i - 1) - lineY2s.get(i + 1)) < Math.abs(lineY2s.get(i - 1) - lineY2s.get(i))) {
					lineY2s.set(i, (lineY2s.get(i - 1)+ lineY2s.get(i + 1)) / 2);  
				}
			}
			
	
			for (int i = 0; i < lineY1s.size(); i++) {
				for (int col = w*i; col < w*(i+1); col++)
				{
					// image.ptr(lineY1s[i] - 2)[col * 3] >100 
					if(image.get(lineY2s.get(i) + 2, col)[0]>100) {
//					if ( image.ptr(lineY2s[i] + 2)[col * 3] >100) {
						//c++ Point pt1 = new Point(col, lineY1s[i]);
						Point pt1 = new Point(col, lineY1s.get(i));
						//c++ Point pt2 = new Point(col, lineY2s[i]+1);
						Point pt2 = new Point(col, lineY1s.get(i)+1);
						Imgproc.line(image, pt1, pt2, color, 1);
					}
				
				}
			
			}
		}
	}
	
	  // 3号画线段, 合并水平线段，排除交叉点
	void drawDetectedLines2(Mat image, Scalar color) {
		if(color == null) color = new Scalar(255, 255, 255);
		//ymap 即水平直线y坐标出现的次数
		Map<Integer, Integer> ymap = findHLines();
		//xmap 即垂直x坐标出现的次数
		Map<Integer, Integer> xmap = findVLines();
		Map<Integer, Integer> ymergemap = mergeHLines(ymap); 
		drawMergeHLines(image, ymergemap, color);
		drawVLines(image, xmap, 2, color);
	}
	   // 4号画线段
		void drawDetectedLines3(Mat image, Scalar color) {
			if(color == null) color = new Scalar(0, 255, 255);
			Map<Integer, Integer> ymap = findHLines();
			Map<Integer, Integer> xmap = findVLines();
			Map<Integer, Integer> ymergemap = mergeHLines(ymap);
			drawMergeHLines2(image, ymergemap, color);
			drawVLines(image, xmap, 1, color);
		}	
}
