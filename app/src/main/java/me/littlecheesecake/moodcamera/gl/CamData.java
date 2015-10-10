package me.littlecheesecake.moodcamera.gl;

public class CamData {
	//preview aspect ration
	public final float mAspectRatioPreview[] = new float[2];
	//predefined filter
	public int mFilter;
	//device orientation degree
	public int mOrientationDevice;
	//camera orientation matrix
	public final float mOrientationM[] = new float[16];
}
