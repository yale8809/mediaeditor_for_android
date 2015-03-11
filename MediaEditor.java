package com.gexing.tutu.jni;

import java.lang.ref.WeakReference;
import java.util.Vector;


import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

/**
 * MediaMixer class: objects of Media editor, make sure there is only a single instance. 
 * @author Administrator
 *
 */
public class MediaEditor {
	
	public interface OnCompleteListener {
		public void onComplete();
	};
	public interface OnErrorListener {
		public void onError(int arg1, int arg2);
	};
	public interface OnProgressListener {
		public void onUpdateProgress(int percent);
	}

	static {
		System.loadLibrary("mediaprocess");
	} 
	
	public MediaEditor() {
	}
	
	public static final int FMT_IMG_RGBA = 0;
	public static final int FMT_IMG_YUV420P = 1;
	
	private static final int MSG_MIX_COMPLETED = 1;
	private static final int MSG_MIX_PROGRESS = 2;
	private static final int MSG_MIX_ERROR_OPENOUTFILEFORBIDDEN = 101;
	private static final int MSG_MIX_ERROR_FORMATNOTSUPPORTCODEC = 102;
	private static final int MSG_MIX_ERROR = 1000;
	
	//native video decoder interface
	private static native int nativeStartDecodeVideo(String inFilePath, int outWidth, int outHeight, int outPix);
	private static native int nativeGetDecWidth();
	private static native int nativeGetDecHeight();
	private static native float nativeGetDecDuration();
	private static native int nativeGetDecRotation();
	private static native float nativeGetDecFps();
	private static native double nativeGetOneDecodeImage(byte[] imgBuf);
	private static native int nativeStopDecodeVideo();
	private static native int nativeSetDecodePos(float pos);
	
	//native audio mix interface
	private static native int nativeStartMixMediaAudio(String inMediaFilePath, String outMediaFilePath, 
			String inSoundFilePath, String inMusciFilePath, int srcMediaRatio, int srcSoundRatio, int bgMusicRatio);
	//native clip video interface
	private static native int nativeStartMediaClip(String inMeidaFilePath, String outMediaFilePath,
			int outWidth, int outHeight, float startTime, float endTime);
	
	//native convert images to video interface
	private static native int nativeStartPicturesToVideo(String outMeidaFilePath,
			int outWidth, int outHeight, int outBitrate, float duration);
	private static native int nativeAddPictureToList(String imageFilePath);
	
	private static native void nativeStartRecord(String outMediaFilePath, int imgWidth,
			int imgHeight, int bitrate, int frameRate);
	private static native void nativePauseRecord();
	private static native void nativeResumeRecord();
	private static native void nativestopRecord();
	private static native void nativeputImage(byte[] image);
	private static native void nativeputAudio(byte[] audio, int size);
	private static native void nativeSetRotation(int rotation);
	private static native void nativeSetPicFmt(int fmt);
	private static native int nativeGetAudioFrameSize();
	
	//stop interface
	private static native void nativeStopMix();
	private static native void nativeDestroy();
	
	//---------------------------- for decoder -----------------------------
	/**
	 * 
	 * @param inFilePath	输入视频文件
	 * @param outWidth		输出图像宽    	设置为0，则表示按视频文件的宽输出，否则按设置值 输出
	 * @param outHeight		输出图像高     设置为0，则表示按视频文件的高输出，否则按设置值 输出
	 * @param outPix		图像图像类型   设置为0，则按rgba格式输出
	 * @return 0成功，负值代表失败
	 */
	public int startDecodeFile(String inFilePath, int outWidth, int outHeight, int outPix){
		return nativeStartDecodeVideo(inFilePath, outWidth, outHeight, outPix);
	}
	
	public int getDecodeFileWidth(){
		return nativeGetDecWidth();
	}
	
	public int getDecodeFileHeight(){
		return nativeGetDecHeight();
	}
	
	public float getDecodeFileDuration(){
		return nativeGetDecDuration();
	}
	public int getDecodeFileRotation(){
		return nativeGetDecRotation();
	}
	
	public float getDecodeFileFps(){
		return nativeGetDecFps();
	}
	
	public double getDecodeOneImage(byte[] imageBuf){
		return nativeGetOneDecodeImage(imageBuf);
	}
	
	public int StopDecodeVideo(){
		return nativeStopDecodeVideo();
	}
	public int setDecodeVideoPos(float pos){
		return nativeSetDecodePos(pos);
	}
	//---------------------------- for audio mix -----------------------------
	/**
	 * 声音的合成接口
	 * @param inMediaFilePath    原视频文件 路径
	 * @param outMediaFilePath   输出视频文件
	 * @param inSoundFilePath    输入录音文件
	 * @param inMusciFilePath    输入音乐文件
	 * @param srcMediaRatio      原视频音量    0-1.0 
	 * @param srcSoundRatio      录音音量    0-1.0
	 * @param bgMusicRatio       音乐音量  0-1.0
	 * @return 0 为成功，负值为失败
	 */
	public int StartMixMediaAudio(String inMediaFilePath, String outMediaFilePath, 
			String inSoundFilePath, String inMusciFilePath, float srcMediaRatio, float srcSoundRatio, float bgMusicRatio){
		int ratio1 = (int) (srcMediaRatio*256);
		int ratio2 = (int) (srcSoundRatio*256);
		int ratio3 = (int) (bgMusicRatio*256);
		return nativeStartMixMediaAudio(inMediaFilePath, outMediaFilePath, inSoundFilePath, inMusciFilePath, ratio1, ratio2, ratio3);
	}
	
	//---------------------------- for media clip -----------------------------
	/**
	 * 
	 * @param inMeidaFilePath      原视频文件
	 * @param outMediaFilePath     输出视频文件
	 * @param outWidth             输出视频的宽
	 * @param outHeight            图像视频的高
	 * @param startTime			   截取开始时间 单位ms
	 * @param endTime			截取结束时间 单位 ms
	 * @return 0为成功，负值为失败
	 */
	public int StartMediaClip(String inMeidaFilePath, String outMediaFilePath,
			int outWidth, int outHeight, float startTime, float endTime){
		return nativeStartMediaClip(inMeidaFilePath, outMediaFilePath, outWidth, outHeight, startTime, endTime);
	}
	
	//---------------------------- for images to video -----------------------------
	/**
	 * 图片合视频掊，在调用之前需要调用addPictureList接口
	 * @param outMeidaFilePath		输出视频文件路径
	 * @param outWidth			输出视频宽
	 * @param outHeight			输出视频高
	 * @param outBitrate		输出视频码率(暂不起作用)
	 * @param duration			输出视频的总时长，单位ms
	 * @return	0为成功，负值为失败
	 */
	public int startPicturesToVideo(String outMeidaFilePath,
			int outWidth, int outHeight, int outBitrate, float duration){
		return nativeStartPicturesToVideo(outMeidaFilePath, outWidth, outHeight, outBitrate, duration);
	}
	/**
	 * 
	 * @param imageFileList		图像文件路径列表
	 * @return
	 */
	public int addPictureList(Vector<String> imageFileList){
		for(int i = 0; i < imageFileList.size(); i++){
		  nativeAddPictureToList(imageFileList.get(i));
		}
		return 0;
	}
	
	/**
	 * 
	 * @param outPath 输出视频路径及名称
	 * @param imgWidth
	 * @param imgHeight
	 * @param bitrate
	 * @param frameRate
	 */
	public void startMediaRecord(String outPath, int imgWidth, int imgHeight, 
			int bitrate, int frameRate){
		nativeStartRecord(outPath, imgWidth, imgHeight, bitrate, frameRate);
	}
	public void pauseMediaRecord(){
		nativePauseRecord();
	}
	public void resumeMediaRecord(){
		nativeResumeRecord();
	}
	public void stopMediaRecord(){
		nativestopRecord();
	}
	public void putMediaRecordImage(byte[] image){
		nativeputImage(image);
	}
	public void putMediaRecordAudio(byte[] audio, int size){
		nativeputAudio(audio, size);
	}
	public void setMediaRecordRation(int rotation){
		nativeSetRotation(rotation);
	}
	public int getMediaRecordAudioFrameSize(){
		return nativeGetAudioFrameSize();
	}
	public void setMediaRecordPixFormate(int fmt){
		nativeSetPicFmt(fmt);
	}
	//---------------------------- for end -----------------------------

	public void stopMix() {
//		Log.i("BGMUSIC", "Java: StopMix enter");
		nativeStopMix();
	}
	
	public void destroy() {
//		Log.i("BGMUSIC", "Java: Destroy enter");
		nativeDestroy();
	}
	
}
