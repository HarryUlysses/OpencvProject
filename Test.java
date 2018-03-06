package opencvTest;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Test {
	 	static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
	 	/*
	    public static void main(String[] args){
	        
	    	Mat m = Imgcodecs.imread("/home/wangzehong/imageText.png");
	    	//Mat m = Imgcodecs.imread((getClass().getResource("dzjym01.png").getPath().substring(1));
	        System.out.println(m);
	        Imgcodecs.imwrite("/home/wangzehong/imageText1.jpg", m);
	        //System.out.println(getClass());
	    }*/
	 	public void ShowMatrix(Mat a)
	 	{
	 		//Mat a = Mat.ones(2,2, CvType.CV_8U);
	 
	 		for(int i = 0; i < a.rows(); i++)
	 		{
	 			for(int j = 0; j < a.cols(); j++)
	 			{
//	 				a.put(i, j, 0);
	 				double[] x = a.get(i, j);
	 				for(int m=0;m<x.length;m++)
	 					System.out.print(x[m]+",");
	 			}
	 			System.out.print("\n");
	 		}
	 			
	 	}
	    public static void main(String [] args)
	    {
	    	Mat RotationImage = new Mat();
	    	TextRotation txtRotation = new TextRotation();
	    	LineFinder finder = new LineFinder();
	    	// 设置最小投票数
	    	finder.setMinVote(1);
	    	//设置最小线段长度和线段间距容忍度
	    	finder.setLineLengthAndGap(7, 1);
	    	// 倾斜矫正
	    	//const char* filename = argv[1];
	    	//char outfile[256] = { 0 };
	    	//读入图像
	    	String fileName = "/home/wangzehong/imageText.png";
	    	//输出图像
	    	String  outFile = "/home/wangzehong/imageTextoutput.jpg";
	    	// outFile += fileName + "";
	    	// Mat srcImg = imread(filename, CV_LOAD_IMAGE_GRAYSCALE);
	    	Mat srcImg = Imgcodecs.imread(fileName,Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
	    	//Imgcodecs.imwrite(outFile, srcImg);
	    	if (srcImg.empty())
	    	 return ;
	    	
	    	//cout << "文件"<<filename<<"读取成功" << endl;
	    	 System.out.println("文件"+fileName+"读取成功");
	    	 String rotationImg = "/home/wangzehong/rotationImg.jpg";
	    	//imshow("source", srcImg);
	    	RotationImage = txtRotation.rotatioin(srcImg, rotationImg);
	    	//Imgcodecs.imwrite(outFile, srcImg);
	    	
	    	//cout << "倾斜矫正成功" << endl;
	    	System.out.println("倾斜矫正成功");
	    	
	    	// 去横线
	    	Mat image = Imgcodecs.imread(rotationImg);
	    	Mat result = new Mat();
	    	//Imgcodecs
	    	Imgproc.cvtColor(image, result,Imgproc.COLOR_BGRA2GRAY);
	    	Mat contours = new Mat();
	    	//边缘检测
	    	Imgproc.Canny(result, contours, 20, 200,3,true);
	    	//画出边缘检测图片
	    	System.out.println("边缘检测图片保存在"+"/home/wangzehong/contours.jpg");
	    	Imgcodecs.imwrite("/home/wangzehong/contours.jpg", contours);
	    	//通过边缘检测找出直线
	    	finder.findLines(contours);
	    	
	    	//方式一  在原图上画出所有直线线
	    	//finder.drawDetectedLines(image, null);
	    	//outFile = "/home/wangzehong/1imageTextoutput.jpg";
	    	//方式二  
	    	//finder.drawDetectedLines1(image, null);
	    	//outFile = "/home/wangzehong/2imageTextoutput.jpg";
	    	//方式三 : 画出所有
	    	finder.drawDetectedLines2(image,null);
	    	outFile = "/home/wangzehong/3imageTextoutput.jpg";
	    	//方式四：
	    	//finder.drawDetectedLines3(image,null);
	    	//outFile = "/home/wangzehong/4imageTextoutput.jpg";
	    	
	    	
	    	
	    	Imgcodecs.imwrite(outFile, image);
	    	System.out.println("去横线处理成功。保存为"+outFile);
	    	//cout << "去横线处理成功。保存为"<<outfile << endl;
	    }
}
