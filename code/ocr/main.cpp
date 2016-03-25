// std.cpp : Defines the entry point for the console application.
//

//ReadBitMap
//
#include <string>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <iostream>

#define WIDTHBYTES(bits) (((bits)+31)/32*4)
typedef unsigned char BYTE;
typedef unsigned short WORD;
typedef unsigned long DWORD;
typedef long LONG;

//λͼ�ļ�ͷ��Ϣ�ṹ����
//���в������ļ�������Ϣ�����ڽṹ����ڴ�ṹ������Ҫ�Ǽ��˵Ļ���������ȷ��ȡ�ļ���Ϣ��
typedef struct tagBITMAPFILEHEADER {
 WORD bfType;//�̶�Ϊ0x4d42
 DWORD bfSize; //�ļ���С
 WORD bfReserved1; //�����֣�������
 WORD bfReserved2; //�����֣�ͬ��
 DWORD bfOffBits; //ʵ��λͼ���ݵ�ƫ���ֽ�������ǰ�������ֳ���֮��
} BITMAPFILEHEADER;

//��ϢͷBITMAPINFOHEADER��Ҳ��һ���ṹ���䶨�����£�
typedef struct tagBITMAPINFOHEADER{
 //public:
 DWORD biSize; //ָ���˽ṹ��ĳ��ȣ�Ϊ40
 LONG biWidth; //λͼ��
 LONG biHeight; //λͼ��
 WORD biPlanes; //ƽ������Ϊ1
 WORD biBitCount; //������ɫλ����������1��2��4��8��16��24���µĿ�����32
 DWORD biCompression; //ѹ����ʽ��������0��1��2������0��ʾ��ѹ��
 DWORD biSizeImage; //ʵ��λͼ����ռ�õ��ֽ���
 LONG biXPelsPerMeter; //X����ֱ���
 LONG biYPelsPerMeter; //Y����ֱ���
 DWORD biClrUsed; //ʹ�õ���ɫ�������Ϊ0�����ʾĬ��ֵ(2^��ɫλ��)
 DWORD biClrImportant; //��Ҫ��ɫ�������Ϊ0�����ʾ������ɫ������Ҫ��
} BITMAPINFOHEADER;

//��ɫ��Palette����Ȼ�������Ƕ���Щ��Ҫ��ɫ���λͼ�ļ����Եġ�24λ��32λ�ǲ���Ҫ��ɫ��ġ�
//���ƺ��ǵ�ɫ��ṹ���������ʹ�õ���ɫ������
typedef struct tagRGBQUAD {
 //public:
 BYTE rgbBlue; //����ɫ����ɫ����
 BYTE rgbGreen; //����ɫ����ɫ����
 BYTE rgbRed; //����ɫ�ĺ�ɫ����
 BYTE rgbReserved; //����ֵ
} RGBQUAD;




void genWordData(tagRGBQUAD* pRGB, int height, int width);

std::string findWord(tagRGBQUAD* dataOfBmp);









void showBmpHead(BITMAPFILEHEADER* pBmpHead)
{
 printf("λͼ�ļ�ͷ:\n");
 printf("bmp��ʽ��־bftype��0x%x\n",pBmpHead->bfType );
 printf("�ļ���С:%d\n",pBmpHead->bfSize);
 printf("������:%d\n",pBmpHead->bfReserved1);
 printf("������:%d\n",pBmpHead->bfReserved2);
 printf("ʵ��λͼ���ݵ�ƫ���ֽ���:%d\n",pBmpHead->bfOffBits);
}

void showBmpInforHead(tagBITMAPINFOHEADER* pBmpInforHead)
{
 printf("λͼ��Ϣͷ:\n");
 printf("�ṹ��ĳ���:%d\n",pBmpInforHead->biSize);
 printf("λͼ��:%d\n",pBmpInforHead->biWidth);
 printf("λͼ��:%d\n",pBmpInforHead->biHeight);
 printf("biPlanesƽ����:%d\n",pBmpInforHead->biPlanes);
 printf("biBitCount������ɫλ��:%d\n",pBmpInforHead->biBitCount);
 printf("ѹ����ʽ:%d\n",pBmpInforHead->biCompression);
 printf("biSizeImageʵ��λͼ����ռ�õ��ֽ���:%d\n",pBmpInforHead->biSizeImage);
 printf("X����ֱ���:%d\n",pBmpInforHead->biXPelsPerMeter);
 printf("Y����ֱ���:%d\n",pBmpInforHead->biYPelsPerMeter);
 printf("ʹ�õ���ɫ��:%d\n",pBmpInforHead->biClrUsed);
 printf("��Ҫ��ɫ��:%d\n",pBmpInforHead->biClrImportant);
}
void showRgbQuan(tagRGBQUAD* pRGB)
{
 //printf("(%-3d,%-3d,%-3d) ",pRGB->rgbRed,pRGB->rgbGreen,pRGB->rgbBlue);
	if(pRGB->rgbRed < 50) //black
		printf("1 ");
	else //white
		printf("  ");
}
 
void main()
{
 BITMAPFILEHEADER bitHead;
 BITMAPINFOHEADER bitInfoHead;
 FILE* pfile;
 char strFile[50];
 char *BmpFileHeader;
 WORD *temp_WORD;
 DWORD *temp_DWORD;
 printf("please input the .bmp file name:\n");
 scanf("%s",strFile);
 
 pfile = fopen(strFile,"rb");//���ļ�
    BmpFileHeader=(char *)calloc(14,sizeof(char));
 if(pfile!=NULL)
 {
  printf("file bkwood.bmp open success.\n");
  //��ȡλͼ�ļ�ͷ��Ϣ
  
  
  
  fread(BmpFileHeader,1,14,pfile);
  temp_WORD=(WORD* )(BmpFileHeader);
  bitHead.bfType=*temp_WORD;
  if(bitHead.bfType != 0x4d42)
  {
   printf("file is not .bmp file!");
   
   return;
  }
  temp_DWORD=(DWORD *)(BmpFileHeader+sizeof(bitHead.bfType));
  bitHead.bfSize=*temp_DWORD;
  temp_WORD=(WORD*)(BmpFileHeader+sizeof(bitHead.bfType)+sizeof(bitHead.bfSize));
  bitHead.bfReserved1=*temp_WORD;
  temp_WORD=(WORD*)(BmpFileHeader+sizeof(bitHead.bfType)+sizeof(bitHead.bfSize)+sizeof(bitHead.bfReserved1));
  bitHead.bfReserved2=*temp_WORD;
  temp_DWORD=(DWORD*)(BmpFileHeader+sizeof(bitHead.bfType)+sizeof(bitHead.bfSize)+sizeof(bitHead.bfReserved1)+sizeof(bitHead.bfReserved2));
  bitHead.bfOffBits=*temp_DWORD;
 
  
  
  showBmpHead(&bitHead);
  printf("\n\n");
 
  //��ȡλͼ��Ϣͷ��Ϣ
  fread(&bitInfoHead,1,sizeof(BITMAPINFOHEADER),pfile);
  showBmpInforHead(&bitInfoHead);
  printf("\n");
  
 }
 else
 {
  printf("file open fail!\n");
  return;
 }
 
 tagRGBQUAD *pRgb ;
 if(bitInfoHead.biBitCount < 24)//�е�ɫ��
 {
  //��ȡ��ɫ�̽���Ϣ
  long nPlantNum = long(pow(2,double(bitInfoHead.biBitCount))); // Mix color Plant Number;
  pRgb=(tagRGBQUAD *)malloc(nPlantNum*sizeof(tagRGBQUAD));
  memset(pRgb,0,nPlantNum*sizeof(tagRGBQUAD));
  int num = fread(pRgb,4,nPlantNum,pfile);
  printf("Color Plate Number: %d\n",nPlantNum);
  printf("��ɫ����Ϣ:\n");
  for (int i =0; i<nPlantNum;i++)
  {
   if (i%5==0)
   {
    printf("\n");
   }
   showRgbQuan(&pRgb[i]);
  }
  printf("\n");
 }

 int width = bitInfoHead.biWidth;
 int height = bitInfoHead.biHeight;
 //�����ڴ�ռ��Դͼ�����ڴ�
 int l_width = WIDTHBYTES(width* bitInfoHead.biBitCount);//����λͼ��ʵ�ʿ�Ȳ�ȷ����Ϊ32�ı���
 BYTE *pColorData=(BYTE *)malloc(height*l_width);
 memset(pColorData,0,height*l_width);
 long nData = height*l_width;
 //��λͼ������Ϣ����������
 fread(pColorData,1,nData,pfile);
 //��λͼ����ת��ΪRGB����
 tagRGBQUAD* dataOfBmp;
 dataOfBmp = (tagRGBQUAD *)malloc(width*height*sizeof(tagRGBQUAD));//���ڱ�������ض�Ӧ��RGB����
 memset(dataOfBmp,0,width*height*sizeof(tagRGBQUAD));
 if(bitInfoHead.biBitCount<24)//�е�ɫ�壬��λͼΪ�����ɫ
 {
  int k;
  int index = 0;
  if (bitInfoHead.biBitCount == 1)
  {
   for(int i=0;i<height;i++)
    for(int j=0;j<width;j++)
    {
     BYTE mixIndex= 0;
     k = i*l_width + j/8;//k:ȡ�ø�������ɫ������ʵ�����������е����
     //j:��ȡ��ǰ���ص���ɫ�ľ���ֵ
     mixIndex = pColorData[k];
     switch(j%8)
     {
     case 0:
      mixIndex = mixIndex<<7;
      mixIndex = mixIndex>>7;
      break;
     case 1:
      mixIndex = mixIndex<<6;
      mixIndex = mixIndex>>7;
      break;
     case 2:
      mixIndex = mixIndex<<5;
      mixIndex = mixIndex>>7;
      break;
     case 3:
      mixIndex = mixIndex<<4;
      mixIndex = mixIndex>>7;
      break;
     case 4:
      mixIndex = mixIndex<<3;
      mixIndex = mixIndex>>7;
      break;
     case 5:
      mixIndex = mixIndex<<2;
      mixIndex = mixIndex>>7;
      break;
     case 6:
      mixIndex = mixIndex<<1;
      mixIndex = mixIndex>>7;
      break;
     case 7:
      mixIndex = mixIndex>>7;
      break;
     }
     //���������ݱ��浽�����ж�Ӧ��λ��
     dataOfBmp[index].rgbRed = pRgb[mixIndex].rgbRed;
     dataOfBmp[index].rgbGreen = pRgb[mixIndex].rgbGreen;
     dataOfBmp[index].rgbBlue = pRgb[mixIndex].rgbBlue;
     dataOfBmp[index].rgbReserved = pRgb[mixIndex].rgbReserved;
     index++;
    }
  }
  if(bitInfoHead.biBitCount==2)
  {
   for(int i=0;i<height;i++)
    for(int j=0;j<width;j++)
    {
     BYTE mixIndex= 0;
     k = i*l_width + j/4;//k:ȡ�ø�������ɫ������ʵ�����������е����
     //j:��ȡ��ǰ���ص���ɫ�ľ���ֵ
     mixIndex = pColorData[k];
     switch(j%4)
     {
     case 0:
      mixIndex = mixIndex<<6;
      mixIndex = mixIndex>>6;
      break;
     case 1:
      mixIndex = mixIndex<<4;
      mixIndex = mixIndex>>6;
      break;
     case 2:
      mixIndex = mixIndex<<2;
      mixIndex = mixIndex>>6;
      break;
     case 3:
      mixIndex = mixIndex>>6;
      break;
     }
     //���������ݱ��浽�����ж�Ӧ��λ��
     dataOfBmp[index].rgbRed = pRgb[mixIndex].rgbRed;
     dataOfBmp[index].rgbGreen = pRgb[mixIndex].rgbGreen;
     dataOfBmp[index].rgbBlue = pRgb[mixIndex].rgbBlue;
     dataOfBmp[index].rgbReserved = pRgb[mixIndex].rgbReserved;
     index++;

    }
  }
  if(bitInfoHead.biBitCount == 4)
  {
   for(int i=0;i<height;i++)
    for(int j=0;j<width;j++)
    {
     BYTE mixIndex= 0;
     k = i*l_width + j/2;
     mixIndex = pColorData[k];
     if(j%2==0)
     {//��
      mixIndex = mixIndex<<4;
      mixIndex = mixIndex>>4;
     }
     else
     {//��
      mixIndex = mixIndex>>4;
     }
     dataOfBmp[index].rgbRed = pRgb[mixIndex].rgbRed;
     dataOfBmp[index].rgbGreen = pRgb[mixIndex].rgbGreen;
     dataOfBmp[index].rgbBlue = pRgb[mixIndex].rgbBlue;
     dataOfBmp[index].rgbReserved = pRgb[mixIndex].rgbReserved;
     index++;
    }
  }
  if(bitInfoHead.biBitCount == 8)
  {
   for(int i=0;i<height;i++)
    for(int j=0;j<width;j++)
    {
     BYTE mixIndex= 0;
     k = i*l_width + j;
     mixIndex = pColorData[k];
     dataOfBmp[index].rgbRed = pRgb[mixIndex].rgbRed;
     dataOfBmp[index].rgbGreen = pRgb[mixIndex].rgbGreen;
     dataOfBmp[index].rgbBlue = pRgb[mixIndex].rgbBlue;
     dataOfBmp[index].rgbReserved = pRgb[mixIndex].rgbReserved;
     index++;
 
    }
  }
  if(bitInfoHead.biBitCount == 16)
  {
   for(int i=0;i<height;i++)
    for(int j=0;j<width;j++)
    {
     WORD mixIndex= 0;
     k = i*l_width + j*2;
     WORD shortTemp;
     shortTemp = pColorData[k+1];
     shortTemp = shortTemp<<8;
     mixIndex = pColorData[k] + shortTemp;
     dataOfBmp[index].rgbRed = pRgb[mixIndex].rgbRed;
     dataOfBmp[index].rgbGreen = pRgb[mixIndex].rgbGreen;
     dataOfBmp[index].rgbBlue = pRgb[mixIndex].rgbBlue;
     dataOfBmp[index].rgbReserved = pRgb[mixIndex].rgbReserved;
     index++;
    }
  }
 }
 else//λͼΪ24λ���ɫ
 {
  int k;
  int index = 0;
  for(int i=0;i<height;i++)
   for(int j=0;j<width;j++)
   {
    k = i*l_width + j*3;
    dataOfBmp[index].rgbRed = pColorData[k+2];
    dataOfBmp[index].rgbGreen = pColorData[k+1];
    dataOfBmp[index].rgbBlue = pColorData[k];
    index++;
   }
 }

 printf("����������Ϣ:\n");

 /*
 for (int i=height-1; i>=0; i--){
	for(int j= 0; j< width; ++j){
		showRgbQuan(&dataOfBmp[i*width+j]);
	}
	printf("\n");
 }
 */


//genWordData(dataOfBmp, height, width);	




 std::string word = findWord(dataOfBmp);
 std::cout<<word<<"\n";



 fclose(pfile);
 
 if (bitInfoHead.biBitCount<24)
 {
  free(pRgb);
 }
 free(dataOfBmp);
 free(pColorData);
   free(BmpFileHeader);
 printf("done\n");
 system("pause");
} 

void genWordData(tagRGBQUAD* dataOfBmp, int height, int width){
	//"Ȯ��ʲ"
	int tmp = 0;
	FILE* outfp = fopen("word.dat", "wb+");
	int tmpHeight = 0;
	char words[16][2];
	for(int i= 0; i< 16; i++){
		words[i][0] = 0;
		words[i][1] = 0;
	}
	for (int i=height-1; i>=0; i--){
		for(int j= 0; j< width; ++j){
			showRgbQuan(&dataOfBmp[i*width+j]);
			if(dataOfBmp[i*width+j].rgbRed < 50){ //black
				words[tmpHeight][j/8] = words[tmpHeight][j/8] | (1<<(7-(j%8)));
			}
		}
		printf("\n");
		tmpHeight++;
		if(tmpHeight == 16){
			tmpHeight = 0;
			
			//testing
			/*
			for(int i= 0; i< 16; i++){
				for(int j= 0; j< 16; j++){
					if(((words[i][j/8] >> (7-(j%8))) & 1) == 0){ //white
						printf("  ");
					}else{
						printf("1 ");
					}
				}
				printf("\n");
			}
			printf("\n");
			for(int i= 0; i< 16; i++){
				words[i][0] = 0;
				words[i][1] = 0;
			}
			*/


			//output to data
			/*
			if(tmp == 0)
				fprintf(outfp, "Ȯ");
			else if(tmp == 1)
				fprintf(outfp, "��");
			else if(tmp == 2)
				fprintf(outfp, "ʲ");
			tmp++;
			for(int i= 0; i< 16; i++){
				fprintf(outfp, "%c", words[i][0]);
				fprintf(outfp, "%c", words[i][1]);
			}
			*/
		}
	}
	fclose(outfp);
}


std::string findWord(tagRGBQUAD* dataOfBmp){
	std::string rslt;
	char tmp[4];
	char words[16][2];
	char input[16][2];

	//deal with input ... rgb to array

	for(int i= 0; i< 16; i++){
		input[i][0] = 0;
		input[i][1] = 0;
	}
	for (int i=0; i< 16; i++){
		for(int j= 0; j< 16; ++j){
			if(dataOfBmp[i*16+j].rgbRed < 50){ //black
				input[15-i][j/8] = input[15-i][j/8] | (1<<(7-(j%8)));
			}
		}
	}

	//testing
	/*for(int i= 0; i< 16; i++){
		for(int j= 0; j< 16; j++){
			if(((input[i][j/8] >> (7-(j%8))) & 1) == 0){ //white
				printf("  ");
			}else{
				printf("1 ");
			}
		}
		printf("\n");
	}
	*/
	

	//read data
	FILE* fp = fopen("word.dat", "rb");
	

	
	while(fread(tmp, 2, 1, fp)){
		tmp[2] = 0;
		rslt = std::string(tmp);
	

		for(int i= 0; i< 16; i++){
			fread(words[i], 1, 1, fp);
			fread(words[i]+1, 1, 1, fp);
		}
	
		char diff;
		int diffNum = 0;
		for(int i= 0; i< 16; i++){
			diff = words[i][0] ^ input[i][0];
			for(int i= 0; i< 8; i++){
				diffNum += (diff>>i)%2;
			}
		}
		std::cout<<rslt<<" "<<diffNum<<std::endl;

	}

	return rslt;
}