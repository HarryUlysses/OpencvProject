#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>


using namespace cv;
using namespace std;

#define PI 3.1415926
#define GRAY_THRESH 150
#define HOUGH_VOTE 100

#define DEGREE 25

class LineFinder{
private:
	// 直线对应的点参数向量
	vector<Vec4i> lines;
	//步长
	double deltaRho;
	double deltaTheta;
	// 判断是直线的最小投票数
	int minVote;
	// 判断是直线的最小长度
	double minLength;
	// 同一条直线上点之间的距离容忍度
	double maxGap;
public:
	//初始化
	LineFinder() : deltaRho(1), deltaTheta(PI / 180),
		minVote(10), minLength(0.), maxGap(0.) {}
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
	vector<Vec4i> findLines(Mat& binary) {
		lines.clear();
		HoughLinesP(binary, lines, deltaRho, deltaTheta, minVote, minLength, maxGap);
		return lines;
	}

	// 画线段, 直接画所有找到的线段
	void drawDetectedLines(Mat &image, Scalar color = Scalar(0, 0, 255)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		while (it2 != lines.end()) {
			cv::Point pt1((*it2)[0], (*it2)[1]);
			cv::Point pt2((*it2)[2], (*it2)[3]);
			if (abs(pt1.y - pt2.y) < 2) {
				cv::line(image, pt1, pt2, color, 2);
			}
			++it2;
		}
		
	}

	map<int, int> findHLines() {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap; // 水平直线Y坐标出现的次数
		while (it2 != lines.end()) {

			cv::Point pt1((*it2)[0], (*it2)[1]);
			cv::Point pt2((*it2)[2], (*it2)[3]);
			//cv::line(image, pt1, pt2, cv::Scalar(100, 10, 100), 2);
			// 查找水平直线
			if (abs(pt1.y - pt2.y) < 2 && abs(pt1.x - pt2.x) > 5){
				//cv::line(image, pt1, pt2, color, 2);

				std::map<int, int>::const_iterator it = ymap.find(pt1.y);
				if (it == ymap.end())  {
					ymap.insert(std::make_pair(pt1.y, 1));
				}
				else {
					ymap[pt1.y] = ymap[pt1.y] + 1;
				}

			}
			++it2;
		}
		return ymap;
	}

	map<int, int> findVLines() {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> xmap; // 水平直线Y坐标出现的次数
		while (it2 != lines.end()) {

			cv::Point pt1((*it2)[0], (*it2)[1]);
			cv::Point pt2((*it2)[2], (*it2)[3]);
			//cv::line(image, pt1, pt2, cv::Scalar(100, 10, 100), 2);
			// 查找垂直线
			if (abs(pt1.y - pt2.y) > 10 && abs(pt1.x - pt2.x)<2) {
				std::map<int, int>::const_iterator it = xmap.find(pt1.x);

				if (it == xmap.end())  {
					xmap.insert(std::make_pair(pt1.x, 1));
				}
				else {
					xmap[pt1.x] = xmap[pt1.x] + 1;
				}
			}
			++it2;
		}
		return xmap;
	}

	void drawHLines(Mat &image, map<int, int> map, int thred, Scalar color = Scalar(255, 0, 0)) {
		std::map<int, int>::const_iterator it = map.begin();
		while (it != map.end()) {
			//std::cout << (*it).first << "," << (*it).second << std::endl;
			if ((*it).second > thred) {
				cv::Point pt1(0, (*it).first);
				cv::Point pt2(image.cols, (*it).first);
				cv::line(image, pt1, pt2, color, 1);
			}

			++it;
		}
	}
	void drawVLines(Mat &image, map<int, int> map, int thred, Scalar color = Scalar(255, 0, 0)) {
		std::map<int, int>::const_iterator it = map.begin();
		while (it != map.end()) {
			//std::cout << (*it).first << "," << (*it).second << std::endl;
			if ((*it).second > thred) {
				cv::Point pt1((*it).first, 0);
				cv::Point pt2((*it).first, image.rows);
				cv::line(image, pt1, pt2, color, 1);
			}

			++it;
		}
	}

	// 画线段， 找到所有水平线段的纵坐标和垂直线段的横坐标，在该纵坐标画水平线，在横坐标处画垂直线
	void drawDetectedLines1(Mat &image, Scalar color = Scalar(255, 0, 0)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap = findHLines();
		map<int, int> xmap = findVLines();
		drawHLines(image, ymap, 5);
		drawVLines(image, xmap, 2);
	}

	map<int, int> mergeHLines(map<int, int> ymap) {
		// 合并相邻的水平直线
		std::map<int, int>::const_iterator it = ymap.begin();
		std::map<int, int> ymergemap; // 相邻直线最上侧线，相邻直线最下测线
		int y1 = 0;
		int y2 = 0;
		if (it != ymap.end()){
			y1 = y2 = (*it).first;
			++it;
		}
		while (it != ymap.end()) {

			if (y2 == (*it).first - 1) {
				y2 = (*it).first;
			}
			else {
				if (abs(y2 - y1)>3 && abs(y2 - y1)<15) {
					ymergemap.insert(std::make_pair(y1, y2));
				}

				y2 = y1 = (*it).first;
			}


			++it;
		}
		return ymergemap;
	}

	void drawMergeHLines(Mat &image, map<int, int> &ymergemap, Scalar color = Scalar(0, 255, 0)) {
		std::map<int, int>::const_iterator it = ymergemap.begin();
		while (it != ymergemap.end()) {
			//std::cout << (*it).first << "," << (*it).second << std::endl;
			int y1 = ((*it).first - 1);
			int y2 = ((*it).second + 1);
			uchar* data1 = image.ptr(y1);
			uchar* data2 = image.ptr(y2);
			for (int col = 0; col < image.cols; col++) {
				// 检测新的边界
				for (int y = (*it).first; y < (*it).second; y++) {

				}
				// 上下都为空白则清除
				if (data1[col * 3] >100 && data2[col * 3] >100) {
					cv::Point pt1(col, (*it).first);
					cv::Point pt2(col, (*it).second);
					cv::line(image, pt1, pt2, color, 1);
				}
				else {
					//col += 5;
				}
				//data[col * 3+1] = 255;
				//data[col * 3+2] = 255;
				//std::cout <<"("<< (int)data[col * 3] << "," << (int)data[col * 3 + 1] <<","<< (int)data[col * 3+2]<<")";

			}
			//std::cout << std::endl;
			++it;
		}
	}

	//void checkMergeHLines2(Mat &image, map<int, int> &ymergemap, Scalar color = Scalar(0, 255, 0)) {

	int voteValue(map<int, int> map) {

		std::map<int, int>::const_iterator it = map.begin();
		int votecount = (*it).second;
		int ret=(*it).first;
		while (it != map.end()) {
			
			if ((*it).second > votecount) {
				votecount = (*it).second;
				ret = (*it).first;
			}

			++it;
		}
		return ret;
	}
	void drawMergeHLines2(Mat &image, map<int, int> &ymergemap, Scalar color = Scalar(255, 255, 255)) {
		std::map<int, int>::const_iterator it = ymergemap.begin();
		while (it != ymergemap.end()) {
			//std::cout << (*it).first << "," << (*it).second << std::endl;
			//int y1 = ((*it).first);
			//int y2 = ((*it).second);
			//uchar* data1 = image.ptr(y1);
			//uchar* data2 = image.ptr(y2);
			//将宽带分成100份计算每一份的上下测边界
			int splitcount = 100;
			int w = image.cols / splitcount;
			vector<int> lineY1s, lineY2s;
			for (int i = 0; i < splitcount; i++) {
				int y1 = (*it).first;
				int y2 = (*it).second;
				map<int, int> y1map, y2map; // 每小段的高度
				for (int col = w*i; col < w*(i + 1); col++)
				{
					
					bool foundBottom = false;
					for (int y = y2; y >= y1; y--) {
						// 查找下边界坐标,从下往上找第一个黑点
						if (foundBottom == false) {
							if (image.ptr(y)[col * 3] < 100) {
								y2 = y;
								foundBottom = true;
							}
						}
						// 找到下边界，找第一个白点
						else {
							if (image.ptr(y)[col * 3] > 100) {
								y1 = y;
								break;
							}
						}
					}
					if (y1map[y1] != NULL) {
						y1map[y1] = y1map[y1] + 1;
					}
					else {
						y1map[y1] = 1;
					}
					if (y2map[y2] != NULL) {
						y2map[y2] = y2map[y2] + 1;
					}
					else {
						y2map[y2] = 1;
					}
					// 查找上边界坐标
				}
				// 查找平均上限坐标和平均下限坐标
				
			
				lineY1s.push_back(voteValue(y1map));
				lineY2s.push_back(voteValue(y2map));
			}
			// 平滑
			for (int i = 1; i < lineY1s.size()-1; i++) {
				if (abs(lineY1s[i - 1] - lineY1s[i + 1]) < abs(lineY1s[i - 1] - lineY1s[i])) {
					lineY1s[i] = (lineY1s[i - 1]+ lineY1s[i + 1]) / 2;
				}
				if (abs(lineY2s[i - 1] - lineY2s[i + 1]) < abs(lineY2s[i - 1] - lineY2s[i])) {
					lineY2s[i] = (lineY2s[i - 1]+ lineY2s[i + 1]) / 2;
				}
			}
			for (int i = 0; i < lineY1s.size(); i++) {
				for (int col = w*i; col < w*(i+1); col++)
				{
					// image.ptr(lineY1s[i] - 2)[col * 3] >100 &&
					if ( image.ptr(lineY2s[i] + 2)[col * 3] >100) {
						cv::Point pt1(col, lineY1s[i]);
						cv::Point pt2(col, lineY2s[i]+1);
						cv::line(image, pt1, pt2, color, 1);
					}
				
				}
			
			}

			//std::cout << std::endl;
			++it;
		}
	}
	// 画线段, 合并水平线段，排除交叉点
	void drawDetectedLines2(Mat &image, Scalar color = Scalar(0, 255, 0)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap = findHLines();
		map<int, int> xmap = findVLines();
		map<int, int> ymergemap=mergeHLines(ymap);
		drawMergeHLines(image, ymergemap, color);
		drawVLines(image, xmap, 2,color);

	}

	// 画线段
	void drawDetectedLines3(Mat &image, Scalar color = Scalar(0, 255, 255)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap = findHLines();
		map<int, int> xmap = findVLines();
		map<int, int> ymergemap = mergeHLines(ymap);
		drawMergeHLines2(image, ymergemap, color);
		drawVLines(image, xmap, 2, color);
	}
};





