int main(int argc, char *argv[])
{
	TextRotation txtRotation;
	LineFinder finder;
	finder.setMinVote(1);
	finder.setLineLengthAndGap(7, 1);
	
	const char* filename = argv[1];
	char outfile[256] = { 0 };
	strcat(outfile, filename);
	strcat(outfile, ".output.jpg");
	//const char* outfile = filename "output.jpg";
	Mat srcImg = imread(filename, CV_LOAD_IMAGE_GRAYSCALE);

	if (srcImg.empty())
		return -1;


	cout << "ÎÄ¼þ"<<filename<<"¶ÁÈ¡³É¹¦" << endl;
	//imshow("source", srcImg);
	txtRotation.rotatioin(srcImg, outfile);
	
	cout << "ÇãÐ±½ÃÕý³É¹¦" << endl;
	// È¥ºáÏß
	Mat image = imread(outfile);
	Mat result;
	cvtColor(image, result, CV_BGRA2GRAY);
	Mat contours;
	Canny(result, contours, 20, 200,3);

	finder.findLines(contours);
	//finder.drawDetectedLines(image);
	//finder.drawDetectedLines1(image);
	finder.drawDetectedLines2(image);
	//finder.drawDetectedLines3(image);
	//cv::namedWindow("aa");
	//cv::imshow("aa", contours);

	cv::namedWindow("hough");
	cv::imshow("hough", image);
	cv::imwrite(outfile, image);
	cout << "È¥ºáÏß´¦Àí³É¹¦¡£±£´æÎª"<<outfile << endl;
	cv::waitKey(0);
	return 0;
}
