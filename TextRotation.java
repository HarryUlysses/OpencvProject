package opencvTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.core.MatOfInt4;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TextRotation {

		private static final int BORDER_CONSTANT = 0;
		private static double PI = 3.1415926;
		private static int GRAY_THRESH = 150;
		private static int HOUGH_VOTE = 100;

		public Mat rotatioin(Mat srcImg, String dstImgFile) {
		Point center = new Point(srcImg.cols() / 2, srcImg.rows() / 2);
		
	/*
#ifdef DEGREE
		//Rotate source image
		Mat rotMatS = getRotationMatrix2D(center, DEGREE, 1.0);
		warpAffine(srcImg, srcImg, rotMatS, srcImg.size(), 1, 0, Scalar(255, 255, 255));
#endif
*/
		
		//Expand image to an optimal size, for faster processing speed
		//Set widths of borders in four directions
		//If borderType==BORDER_CONSTANT, fill the borders with (0,0,0)
		Mat padded = new Mat();

		int opWidth = Core.getOptimalDFTSize(srcImg.rows());
		int opHeight = Core.getOptimalDFTSize(srcImg.cols());
		Core.copyMakeBorder(srcImg, padded, 0, opWidth - srcImg.rows(), 0, opHeight - srcImg.cols(), BORDER_CONSTANT, Scalar.all(0));
		//Imgcodecs.imwrite("padded.jpg",padded);
		List<Mat> planes1=new ArrayList<Mat>();
		padded.assignTo(padded,CvType.CV_32F);
		planes1.add(padded);
		planes1.add(Mat.zeros(padded.size(), CvType.CV_32F));
		
		Mat comImg=new Mat();
		Core.merge(planes1, comImg);
		//Imgcodecs.imwrite("comImg.jpg",comImg);
		//Use the same image as input and output,
		//so that the results can fit in Mat well
		Core.dft(comImg, comImg);

		//Compute the magnitude
		//planes[0]=Re(DFT(I)), planes[1]=Im(DFT(I))
		//magnitude=sqrt(Re^2+Im^2)
		Core.split(comImg, planes1);
		Core.magnitude(planes1.get(0), planes1.get(1), planes1.get(0));

		//Switch to logarithmic scale, for better visual results
		//M2=log(1+M1)
		Mat magMat = planes1.get(0);
		for(int i=0;i<magMat.cols();i++) {
			for(int j=0;j<magMat.rows();j++) {
				for(int k=0;k<magMat.channels();k++) {
					magMat.get(j, i)[k]+=1;	
				}
			}
		}
		Core.log(magMat, magMat);

		//Crop the spectrum
		//Width and height of magMat should be even, so that they can be divided by 2
		//-2 is 11111110 in binary system, operator & make sure width and height are always even
//		magMat = magMat(new Rect(0, 0, magMat.cols() & -2, magMat.rows() & -2));
		magMat=magMat.submat(new Rect(0, 0, magMat.cols() & -2, magMat.rows() & -2));

		//Rearrange the quadrants of Fourier image,
		//so that the origin is at the center of image,
		//and move the high frequency to the corners
		int cx = magMat.cols() / 2;
		int cy = magMat.rows() / 2;

		Mat q0 = new Mat(magMat, new Rect(0, 0, cx, cy));
		Mat q1 = new Mat(magMat, new Rect(0, cy, cx, cy));
		Mat q2 = new Mat(magMat, new Rect(cx, cy, cx, cy));
		Mat q3 = new Mat(magMat, new Rect(cx, 0, cx, cy));

		Mat tmp=new Mat();
		q0.copyTo(tmp);
		q2.copyTo(q0);
		tmp.copyTo(q2);

		q1.copyTo(tmp);
		q3.copyTo(q1);
		tmp.copyTo(q3);

		//Normalize the magnitude to [0,1], then to[0,255]
		
		//CV_MINMAX
		//Core.NORM_MINMAX
		Core.normalize(magMat, magMat, 0, 1, Core.NORM_MINMAX);
		
		Mat magImg = new Mat(magMat.size(), CvType.CV_8UC1);
		magMat.convertTo(magImg, CvType.CV_8UC1, 255, 0);
		//imshow("magnitude", magImg);
		//Imgcodecs.imwrite("imageText_mag.jpg",magImg);

		//Turn into binary image
		//Core.
		Imgproc.threshold(magImg, magImg, GRAY_THRESH, 255, Imgproc.THRESH_BINARY);
		//imshow("mag_binary", magImg);
		//imwrite("imageText_bin.jpg",magImg);

		//Find lines with Hough Transformation
		//	Vec2f
		Mat lines = new Mat();;
		float pi180 = (float)Math.PI / 180;
		Mat linImg = new Mat(magImg.size(), CvType.CV_8UC3);
		//Imgproc.HoughLines(image, lines, rho, theta, threshold);
		Imgproc.HoughLines(magImg, lines, 1, pi180, HOUGH_VOTE, 0, 0, 0, Math.PI);
		//System.out.println("size = "+lines.cols());
		int numLines = lines.cols();
		for (int l = 0; l < numLines; l++)
		{
			double data[] = lines.get(0, l);
			double rho = data[0], theta = data[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a*rho, y0 = b*rho;
			Point pt1 = new Point(x0 + 1000 * (-b),y0 + 1000 * (a));
			//pt1.x = cvRound(x0 + 1000 * (-b));
			//pt1.y = cvRound(y0 + 1000 * (a));
			Point pt2 = new Point(x0 - 1000 * (-b),y0 - 1000 * (a));
			//pt2.x = cvRound(x0 - 1000 * (-b));
			//pt2.y = cvRound(y0 - 1000 * (a));
			Imgproc.line(linImg, pt1, pt2, new Scalar(255, 0, 0), 3, 8, 0);
		}
		//imshow("lines", linImg);
		//imwrite("imageText_line.jpg", linImg);
		if (lines.cols() == 3){
		  //	cout << "found three angels:" << endl;
			System.out.println("found three angels:");
			System.out.println(lines.get(0, 0)[1] * 180 /Math.PI);
			System.out.println(lines.get(0, 1)[1] * 180 /Math.PI);
			System.out.println(lines.get(0, 2)[1] * 180 /Math.PI);
		  //	cout << lines[0][1] * 180 / CV_PI << endl << lines[1][1] * 180 / CV_PI << endl << lines[2][1] * 180 / CV_PI << endl << endl;
		}

		//Find the proper angel from the three found angels
		double angel = 0;
		float piThresh = (float)Math.PI / 90;
		float pi2 = (float) (Math.PI / 2);
		for (int l = 0; l<numLines; l++)
		{
			//lines[l][1];
			double theta =  lines.get(0, l)[1]; 
			if (Math.abs(theta) < piThresh || Math.abs(theta - pi2) < piThresh)
				continue;
			else{
				angel = theta;
				break;
			}
		}

		//Calculate the rotation angel
		//The image has to be square,
		//so that the rotation angel can be calculate right
		//CV_PI->Math.PI
		angel = (float) (angel<pi2 ? angel : angel - Math.PI);
		if (angel != pi2){
			float angelT = (float) (srcImg.rows()*Math.tan(angel) / srcImg.cols());
			angel = (float) Math.atan(angelT);
		}
		double angelD = angel * 180 / (float)Math.PI;
		//cout << "the rotation angel to be applied:" << endl << angelD << endl << endl;

		//Rotate the image to recover
		Mat rotMat = Imgproc.getRotationMatrix2D(center, angelD, 1.0);
		Mat dstImg = Mat.ones(srcImg.size(), CvType.CV_8UC3);
		Imgproc.warpAffine(srcImg, dstImg, rotMat, srcImg.size(), 1, 0, new Scalar(255, 255, 255));

		//imshow("result", dstImg);
		
		Imgcodecs.imwrite(dstImgFile, dstImg);
		return dstImg;
	}

}
