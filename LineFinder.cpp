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
	// ֱ�߶�Ӧ�ĵ��������
	vector<Vec4i> lines;
	//����
	double deltaRho;
	double deltaTheta;
	// �ж���ֱ�ߵ���СͶƱ��
	int minVote;
	// �ж���ֱ�ߵ���С����
	double minLength;
	// ͬһ��ֱ���ϵ�֮��ľ������̶�
	double maxGap;
public:
	//��ʼ��
	LineFinder() : deltaRho(1), deltaTheta(PI / 180),
		minVote(10), minLength(0.), maxGap(0.) {}
	// ���ò���
	void setAccResolution(double dRho, double dTheta) {
		deltaRho = dRho;
		deltaTheta = dTheta;
	}
	// ������СͶƱ��
	void setMinVote(int minv) {
		minVote = minv;
	}
	// ������С�߶γ��Ⱥ��߶μ�����̶�
	void setLineLengthAndGap(double length, double gap) {
		minLength = length;
		maxGap = gap;
	}

	//Ѱ���߶�
	vector<Vec4i> findLines(Mat& binary) {
		lines.clear();
		HoughLinesP(binary, lines, deltaRho, deltaTheta, minVote, minLength, maxGap);
		return lines;
	}

	// ���߶�, ֱ�ӻ������ҵ����߶�
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
		map<int, int> ymap; // ˮƽֱ��Y������ֵĴ���
		while (it2 != lines.end()) {

			cv::Point pt1((*it2)[0], (*it2)[1]);
			cv::Point pt2((*it2)[2], (*it2)[3]);
			//cv::line(image, pt1, pt2, cv::Scalar(100, 10, 100), 2);
			// ����ˮƽֱ��
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
		map<int, int> xmap; // ˮƽֱ��Y������ֵĴ���
		while (it2 != lines.end()) {

			cv::Point pt1((*it2)[0], (*it2)[1]);
			cv::Point pt2((*it2)[2], (*it2)[3]);
			//cv::line(image, pt1, pt2, cv::Scalar(100, 10, 100), 2);
			// ���Ҵ�ֱ��
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

	// ���߶Σ� �ҵ�����ˮƽ�߶ε�������ʹ�ֱ�߶εĺ����꣬�ڸ������껭ˮƽ�ߣ��ں����괦����ֱ��
	void drawDetectedLines1(Mat &image, Scalar color = Scalar(255, 0, 0)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap = findHLines();
		map<int, int> xmap = findVLines();
		drawHLines(image, ymap, 5);
		drawVLines(image, xmap, 2);
	}

	map<int, int> mergeHLines(map<int, int> ymap) {
		// �ϲ����ڵ�ˮƽֱ��
		std::map<int, int>::const_iterator it = ymap.begin();
		std::map<int, int> ymergemap; // ����ֱ�����ϲ��ߣ�����ֱ�����²���
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
				// ����µı߽�
				for (int y = (*it).first; y < (*it).second; y++) {

				}
				// ���¶�Ϊ�հ������
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
			//������ֳ�100�ݼ���ÿһ�ݵ����²�߽�
			int splitcount = 100;
			int w = image.cols / splitcount;
			vector<int> lineY1s, lineY2s;
			for (int i = 0; i < splitcount; i++) {
				int y1 = (*it).first;
				int y2 = (*it).second;
				map<int, int> y1map, y2map; // ÿС�εĸ߶�
				for (int col = w*i; col < w*(i + 1); col++)
				{
					
					bool foundBottom = false;
					for (int y = y2; y >= y1; y--) {
						// �����±߽�����,���������ҵ�һ���ڵ�
						if (foundBottom == false) {
							if (image.ptr(y)[col * 3] < 100) {
								y2 = y;
								foundBottom = true;
							}
						}
						// �ҵ��±߽磬�ҵ�һ���׵�
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
					// �����ϱ߽�����
				}
				// ����ƽ�����������ƽ����������
				
			
				lineY1s.push_back(voteValue(y1map));
				lineY2s.push_back(voteValue(y2map));
			}
			// ƽ��
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
	// ���߶�, �ϲ�ˮƽ�߶Σ��ų������
	void drawDetectedLines2(Mat &image, Scalar color = Scalar(0, 255, 0)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap = findHLines();
		map<int, int> xmap = findVLines();
		map<int, int> ymergemap=mergeHLines(ymap);
		drawMergeHLines(image, ymergemap, color);
		drawVLines(image, xmap, 2,color);

	}

	// ���߶�
	void drawDetectedLines3(Mat &image, Scalar color = Scalar(0, 255, 255)) {
		std::vector<cv::Vec4i>::const_iterator it2 = lines.begin();
		map<int, int> ymap = findHLines();
		map<int, int> xmap = findVLines();
		map<int, int> ymergemap = mergeHLines(ymap);
		drawMergeHLines2(image, ymergemap, color);
		drawVLines(image, xmap, 2, color);
	}
};





